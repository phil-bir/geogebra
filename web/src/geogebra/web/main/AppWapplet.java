package geogebra.web.main;

import geogebra.common.GeoGebraConstants;
import geogebra.common.gui.toolbar.ToolBar;
import geogebra.common.io.layout.DockPanelData;
import geogebra.common.io.layout.Perspective;
import geogebra.common.io.layout.PerspectiveDecoder;
import geogebra.common.main.App;
import geogebra.common.main.DialogManager;
import geogebra.common.util.debug.GeoGebraProfiler;
import geogebra.common.util.debug.Log;
import geogebra.html5.gui.laf.GLookAndFeel;
import geogebra.html5.main.HasAppletProperties;
import geogebra.html5.util.ArticleElement;
import geogebra.web.gui.GuiManagerInterfaceW;
import geogebra.web.gui.GuiManagerW;
import geogebra.web.gui.app.GGWCommandLine;
import geogebra.web.gui.app.GGWMenuBar;
import geogebra.web.gui.app.GGWToolBar;
import geogebra.web.gui.applet.GeoGebraFrame;
import geogebra.web.gui.dialog.DialogManagerW;
import geogebra.web.gui.layout.DockPanelW;
import geogebra.web.gui.layout.panels.EuclidianDockPanelW;
import geogebra.web.helper.ObjectPool;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AppWapplet extends AppW {

	private GeoGebraFrame frame = null;
	protected GuiManagerInterfaceW guiManager = null;

	//Event flow operations - are these needed in AppWapplet?
	
	//private LogInOperation loginOperation;
	private GGWMenuBar ggwMenuBar;
	private GGWToolBar ggwToolBar = null;
	private int spWidth;
	private int spHeight;
	private boolean menuVisible = false;
	private boolean menuInited = false;
	/******************************************************
	 * Constructs AppW for applets with undo enabled
	 * 
	 * @param ae
	 * @param gf
	 */
	public AppWapplet(ArticleElement ae, GeoGebraFrame gf) {
		this(ae, gf, true);
	}

	/******************************************************
	 * Constructs AppW for applets
	 * 
	 * @param undoActive
	 *            if true you can undo by CTRL+Z and redo by CTRL+Y
	 */
	public AppWapplet(ArticleElement ae, GeoGebraFrame gf, final boolean undoActive) {
		super(ae);
		this.frame = gf;
		this.objectPool = new ObjectPool();
		setAppletHeight(frame.getComputedHeight());
		setAppletWidth(frame.getComputedWidth());

		this.useFullGui = !isApplet() ||
				ae.getDataParamShowAlgebraInput() ||
				ae.getDataParamShowToolBar() ||
				ae.getDataParamShowMenuBar() ||
				ae.getDataParamEnableRightClick();


		Log.info("GeoGebra " + GeoGebraConstants.VERSION_STRING + " "
		        + GeoGebraConstants.BUILD_DATE + " "
		        + Window.Navigator.getUserAgent());
		initCommonObjects();
		initing = true;

		this.euclidianViewPanel = new EuclidianDockPanelW(this, getArticleElement().getDataParamShowMenuBar());
		//(EuclidianDockPanelW)getGuiManager().getLayout().getDockManager().getPanel(App.VIEW_EUCLIDIAN);
		this.canvas = this.euclidianViewPanel.getCanvas();
		canvas.setWidth("1px");
		canvas.setHeight("1px");
		canvas.setCoordinateSpaceHeight(1);
		canvas.setCoordinateSpaceWidth(1);
		initCoreObjects(undoActive, this);
		afterCoreObjectsInited();
		resetFonts();
		removeDefaultContextMenu(this.getArticleElement());
	}
	
	public GGWMenuBar getMenuBar() {
		if (ggwMenuBar == null) {
			ggwMenuBar = new GGWMenuBar();
		}
		return ggwMenuBar;
	}

	public GeoGebraFrame getGeoGebraFrame() {
		return frame;
	}
	
	@Override
    public HasAppletProperties getAppletFrame() {
		return frame;
	}

	@Override
	public GuiManagerInterfaceW getGuiManager() {
		return guiManager;
	}

	@Override
	public void initGuiManager() {
		// this should not be called from AppWsimple!
		setWaitCursor();
		guiManager = newGuiManager();
		getGuiManager().setLayout(new geogebra.web.gui.layout.LayoutW());
		getGuiManager().initialize();
		setDefaultCursor();
	}

	/**
	 * @return a GuiManager for GeoGebraWeb
	 */
	protected GuiManagerW newGuiManager() {
		return new GuiManagerW(AppWapplet.this);
	}

	@Override
	protected void afterCoreObjectsInited() {
		// Code to run before buildApplicationPanel
		initGuiManager();
		((EuclidianDockPanelW)euclidianViewPanel).addNavigationBar();
		//following lines were swapped before but for async file loading it does not matter
		//and for sync file loading this makes sure perspective setting is not blocked by initing flag
		initing = false;
		GeoGebraFrame.finishAsyncLoading(articleElement, frame, this);
		
	}

	public void buildSingleApplicationPanel() {
		if (frame != null) {
			frame.clear();
			frame.add((Widget)getEuclidianViewpanel());
			//we need to make sure trace works after this, see #4373 or #4236
			this.getEuclidianView1().createImage();
			((DockPanelW)getEuclidianViewpanel()).setVisible(true);
			((DockPanelW)getEuclidianViewpanel()).setEmbeddedSize(getSettings().getEuclidian(1).getPreferredSize().getWidth());
			((DockPanelW)getEuclidianViewpanel()).updatePanel();
			getEuclidianViewpanel().setPixelSize(
					getSettings().getEuclidian(1).getPreferredSize().getWidth(),
					getSettings().getEuclidian(1).getPreferredSize().getHeight());

			// FIXME: temporary hack until it is found what causes
			// the 1px difference
			//getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setLeft(1, Style.Unit.PX);
			//getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setTop(1, Style.Unit.PX);
			getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setBottom(-1, Style.Unit.PX);
			getEuclidianViewpanel().getAbsolutePanel().getElement().getStyle().setRight(-1, Style.Unit.PX);
			oldSplitLayoutPanel = null;
		}
	}

	private Widget oldSplitLayoutPanel = null;	// just a technical helper variable
	private HorizontalPanel splitPanelWrapper = null;
	@Override
    public void buildApplicationPanel() {

		if (!isUsingFullGui()) {
			if (showConsProtNavigation
					|| !isJustEuclidianVisible()) {
				useFullGui = true;
			}
		}

		if (!isUsingFullGui()) {
			buildSingleApplicationPanel();
			return;
		}

		frame.clear();

		// showMenuBar should come from data-param,
		// this is just a 'second line of defense'
		// otherwise it can be used for taking ggb settings into account too
		if (showMenuBar && articleElement.getDataParamShowMenuBarDefaultTrue() ||
			articleElement.getDataParamShowMenuBar()) {
			attachMenubar();
		}

		// showToolBar should come from data-param,
		// this is just a 'second line of defense'
		// otherwise it can be used for taking ggb settings into account too
		if (showToolBar && articleElement.getDataParamShowToolBarDefaultTrue() ||
			articleElement.getDataParamShowToolBar()) {
			attachToolbar();
		}

		attachSplitLayoutPanel();

		// showAlgebraInput should come from data-param,
		// this is just a 'second line of defense'
		// otherwise it can be used for taking ggb settings into account too
		if (showAlgebraInput && articleElement.getDataParamShowAlgebraInputDefaultTrue() ||
			articleElement.getDataParamShowAlgebraInput()) {
			attachAlgebraInput();
		}
		
		frame.attachGlass();
	}

	public void refreshSplitLayoutPanel() {
		if (frame != null && frame.getWidgetCount() != 0 &&
			frame.getWidgetIndex(getSplitLayoutPanel()) == -1 &&
			frame.getWidgetIndex(oldSplitLayoutPanel) != -1) {
			int wi = frame.getWidgetIndex(oldSplitLayoutPanel);
			frame.remove(oldSplitLayoutPanel);
			frame.insert(getSplitLayoutPanel(), wi); 
			oldSplitLayoutPanel = getSplitLayoutPanel();
			removeDefaultContextMenu(getSplitLayoutPanel().getElement());
		}
	}

	public void attachAlgebraInput() {
		// inputbar's width varies,
		// so it's probably good to regenerate every time
		GGWCommandLine inputbar = new GGWCommandLine();
		inputbar.attachApp(this);
		frame.add(inputbar);
		this.getGuiManager().getAlgebraInput().setInputFieldWidth(this.appletWidth);
	}

	public void attachMenubar() {
		if (ggwToolBar == null) {
			ggwToolBar = new GGWToolBar();
			ggwToolBar.init(this);
			frame.insert(ggwToolBar, 0);
		}
		ggwToolBar.attachMenubar();
	}

	

	public void attachToolbar() {
		// reusing old toolbar is probably a good decision
		if (ggwToolBar == null) {
			ggwToolBar = new GGWToolBar();
			ggwToolBar.init(this);
		}
		frame.insert(ggwToolBar, 0);
	}

	@Override
    public GGWToolBar getToolbar() {
		return ggwToolBar;
	}

	public void attachSplitLayoutPanel() {
		oldSplitLayoutPanel = getSplitLayoutPanel();
		if (oldSplitLayoutPanel != null) {
			if(getArticleElement().getDataParamShowMenuBar()){
				this.splitPanelWrapper =  new HorizontalPanel();
				splitPanelWrapper.add(oldSplitLayoutPanel);
				splitPanelWrapper.add(getMenuBar());
				frame.add(splitPanelWrapper);
			}else{
				frame.add(oldSplitLayoutPanel);
			}
			removeDefaultContextMenu(getSplitLayoutPanel().getElement());
		}
	}

	@Override
    public void afterLoadFileAppOrNot() {
		String perspective = getArticleElement().getDataParamPerspective();
		if (!isUsingFullGui()) {
			if (showConsProtNavigation
					|| !isJustEuclidianVisible() || perspective.length() > 0) {
				useFullGui = true;
			}
		}

		if (!isUsingFullGui()) {
			buildSingleApplicationPanel();
		} else {
			// a small thing to fix a rare bug
			getGuiManager().getLayout().getDockManager().kickstartRoot(frame);
			Perspective p = null;
			if(perspective != null){
				p = PerspectiveDecoder.decode(perspective, this.getKernel().getParser(), ToolBar.getAllToolsNoMacros(true, true));
			}
			getGuiManager().getLayout().setPerspectives(getTmpPerspectives(), p);
		}
		
		getScriptManager().ggbOnInit();	// put this here from Application constructor because we have to delay scripts until the EuclidianView is shown

		initUndoInfoSilent();

		getEuclidianView1().synCanvasSize();

		getAppletFrame().resetAutoSize();

		getEuclidianView1().doRepaint2();
		stopCollectingRepaints();
		frame.splash.canNowHide();
		if (!articleElement.preventFocus()) {
			requestFocusInWindow();
		}

		if (isUsingFullGui()) {
			if (needsSpreadsheetTableModel()) {
				getSpreadsheetTableModel();
			}
			refreshSplitLayoutPanel();

			// probably this method can be changed by more,
			// to be more like AppWapplication's method with the same name,
			// but preferring to change what is needed only to avoid new unknown bugs
			if (getGuiManager().hasSpreadsheetView()) {
				DockPanelW sp = getGuiManager().getLayout().getDockManager().getPanel(App.VIEW_SPREADSHEET);
				if (sp != null) {
					sp.deferredOnResize();
				}
			}
		}

		if (isUsingFullGui())
			this.getEuclidianViewpanel().updateNavigationBar();
		setDefaultCursor();
		GeoGebraFrame.useDataParamBorder(getArticleElement(), getGeoGebraFrame());
		GeoGebraProfiler.getInstance().profileEnd();
		onOpenFile();
    }

	@Override
	public void focusLost() {
		GeoGebraFrame.useDataParamBorder(
				getArticleElement(),
				getGeoGebraFrame());
	}

	@Override
	public void focusGained() {
		GeoGebraFrame.useFocusedBorder(
				getArticleElement(),
				getGeoGebraFrame());
	}

	@Override
	public void setCustomToolBar() {
		String customToolbar = articleElement.getDataParamCustomToolBar();
		if ((customToolbar != null) &&
			(customToolbar.length() > 0) &&
			(articleElement.getDataParamShowToolBar()) &&
			(getGuiManager() != null)) {
			getGuiManager().setGeneralToolBarDefinition(customToolbar);
		}
	}

	@Override
    public void syncAppletPanelSize(int widthDiff, int heightDiff, int evno) {
		if (evno == 1 && getEuclidianView1().isShowing()) {
			// this should follow the resizing of the EuclidianView
			if (getSplitLayoutPanel() != null)
				getSplitLayoutPanel().setPixelSize(
					getSplitLayoutPanel().getOffsetWidth() + widthDiff,
					getSplitLayoutPanel().getOffsetHeight() + heightDiff);
		} else if (evno == 2 && hasEuclidianView2() && getEuclidianView2().isShowing()) {// or the EuclidianView 2
			if (getSplitLayoutPanel() != null)
				getSplitLayoutPanel().setPixelSize(
					getSplitLayoutPanel().getOffsetWidth() + widthDiff,
					getSplitLayoutPanel().getOffsetHeight() + heightDiff);
		}
	}

	@Override
	public DialogManager getDialogManager() {
		if (dialogManager == null) {
			dialogManager = new DialogManagerW(this);
		}
		return dialogManager;
	}

	/**
	 * Check if just the euclidian view is visible in the document just loaded.
	 * 
	 * @return
	 */
	private boolean isJustEuclidianVisible() {
		if (tmpPerspectives == null) {
			return true; //throw new OperationNotSupportedException();
		}

		Perspective docPerspective = null;

		for (Perspective perspective : tmpPerspectives) {
			if (perspective.getId().equals("tmp")) {
				docPerspective = perspective;
			}
		}

		if (docPerspective == null) {
			return true; //throw new OperationNotSupportedException();
		}

		boolean justEuclidianVisible = false;

		for (DockPanelData panel : docPerspective.getDockPanelData()) {
			if ((panel.getViewId() == App.VIEW_EUCLIDIAN) && panel.isVisible()) {
				justEuclidianVisible = true;
			} else if (panel.isVisible()) {
				justEuclidianVisible = false;
				break;
			}
		}

		return justEuclidianVisible;
	}
	
	@Override
    public Element getFrameElement(){
		return  frame.getElement();
	}
	
	@Override
    public String getArticleId() {
		return articleElement.getId();
	}
	
	@Override
    public void updateCenterPanel(boolean b){
		
		//int left = this.oldSplitLayoutPanel.getAbsoluteLeft();
		//int top = this.oldSplitLayoutPanel.getAbsoluteLeft();
		buildApplicationPanel();
		this.oldSplitLayoutPanel.setPixelSize(spWidth, spHeight);
		//we need relative position to make sure the menubar / toolbar are not hiddn
		this.oldSplitLayoutPanel.getElement().getStyle().setPosition(Position.RELATIVE);
		//TODO
		
 	}
	
	@Override
    public void persistWidthAndHeight(){
		spWidth = this.oldSplitLayoutPanel.getOffsetWidth();
		spHeight = this.oldSplitLayoutPanel.getOffsetHeight();
	}
	
	@Override
    public int getWidthForSplitPanel(int fallback) {
		if(spWidth > 0){
			return spWidth;
		}
		return super.getWidthForSplitPanel(fallback);
    }

	@Override
    public int getHeightForSplitPanel(int fallback) {
		if(spHeight > 0){
			return spHeight;
		}
		return super.getHeightForSplitPanel(fallback);
    }
	
	@Override
    public void toggleMenu(){
		this.menuVisible = !this.menuVisible;
		if(this.menuVisible){
			if(!menuInited){
				this.getMenuBar().init(this);
				this.menuInited = true;
			}
			this.splitPanelWrapper.add(this.getMenuBar());
			this.oldSplitLayoutPanel.setPixelSize(
					this.oldSplitLayoutPanel.getOffsetWidth() - GLookAndFeel.MENUBAR_WIDTH_MAX,
			this.oldSplitLayoutPanel.getOffsetHeight());
			this.getMenuBar().setPixelSize(GLookAndFeel.MENUBAR_WIDTH_MAX,this.oldSplitLayoutPanel.getOffsetHeight());
		}else{
			this.oldSplitLayoutPanel.setPixelSize(
					this.oldSplitLayoutPanel.getOffsetWidth() + GLookAndFeel.MENUBAR_WIDTH_MAX,
			this.oldSplitLayoutPanel.getOffsetHeight());
			
			this.splitPanelWrapper.remove(this.getMenuBar());
			if(this.getGuiManager()!=null && this.getGuiManager().getLayout()!=null){
				this.getGuiManager().getLayout().getDockManager().resizePanels();
			}
		}
		
		if(!this.menuVisible && this.getGuiManager()!=null){
			this.getGuiManager().setDraggingViews(false, true);
		}
		if(this.menuVisible){
			this.getGuiManager().refreshDraggingViews();
		}
	}
	
	public Object getGlassPane(){
		return frame.getGlassPane();
	}
}

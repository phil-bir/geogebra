package geogebra.common.kernel.geos;

import geogebra.common.euclidian.draw.DrawTextField;
import geogebra.common.gui.inputfield.AutoCompleteTextField;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.algos.AlgoPointInRegion;
import geogebra.common.kernel.algos.AlgoPointOnPath;
import geogebra.common.kernel.arithmetic.FunctionalNVar;
import geogebra.common.kernel.arithmetic.ExpressionNodeConstants.StringType;
import geogebra.common.main.App;
import geogebra.common.plugin.GeoClass;
import geogebra.common.util.StringUtil;
import geogebra.common.util.TextObject;
import geogebra.common.util.Unicode;

/**
 * Input box for user input
 * @author Michael
 *
 */
public class GeoTextField extends GeoButton {
	private static int defaultLength = 20;
	private int length;
	/**
	 * Creates new text field
	 * @param c construction
	 */
	public GeoTextField(Construction c) {
		super(c);
		length = defaultLength;
	}
	/**
	 * @param cons construction
	 * @param labelOffsetX x offset
	 * @param labelOffsetY y offset
	 */
	public GeoTextField(Construction cons, int labelOffsetX, int labelOffsetY) {
		this(cons);
		this.labelOffsetX = labelOffsetX;
		this.labelOffsetY = labelOffsetY;
	}

	@Override
	public boolean isChangeable(){
		return true;
	}
    
    @Override
	public GeoClass getGeoClassType() {
    	return GeoClass.TEXTFIELD;
    }
    
	@Override
	public boolean isTextField() {
		return true;
	}
	
	/**
	 * @param geo new linked geo
	 */
	public void setLinkedGeo(GeoElement geo) {
		linkedGeo = geo;
		text = geo.getValueForInputBar();
		
		// remove quotes from start and end
		if (text.length() > 0 && text.charAt(0) == '"') {
			text = text.substring(1);
		}		
		if (text.length() > 0 && text.charAt(text.length() - 1) == '"') {
			text = text.substring(0, text.length() - 1);
		}
	}
	
	/**
	 * Returns the linked geo
	 * @return linked geo
	 */
	public GeoElement getLinkedGeo() {
		return linkedGeo;
	}

	private GeoElement linkedGeo = null;

	private String text = null;
	private AutoCompleteTextField textField, textField2;
	
	@Override
	public String toValueString(StringTemplate tpl1) {
		if (linkedGeo == null) return "";
		return text;
	}
	
	/**
	 * Set the text
	 * @param newText new text value
	 */
	public void setText(String newText) {
		text = newText;		
	}
	
	/**
	 * Get the text (used for scripting)
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	
	@Override
	public boolean isGeoTextField(){
		return true;
	}

	/**
	 * Sets length of the input box
	 * @param l new length
	 */
	public void setLength(int l){
		length = l;
		this.updateVisualStyle();
	}

	/**
	 * @return length of the input box
	 */
	public int getLength() {
		return length;
	}

	@Override
	protected void getXMLtags(StringBuilder sb) {

		super.getXMLtags(sb);
		if (linkedGeo != null) {
   	
			sb.append("\t<linkedGeo exp=\"");
			StringUtil.encodeXML(sb, linkedGeo.getLabel(StringTemplate.xmlTemplate));
			sb.append("\"");			    		    	
			sb.append("/>\n");

			// print decimals
			if (printDecimals >= 0 && !useSignificantFigures) {
				sb.append("\t<decimals val=\"");
				sb.append(printDecimals);
				sb.append("\"/>\n");
			}

			// print significant figures
			if (printFigures >= 0 && useSignificantFigures) {
				sb.append("\t<significantfigures val=\"");
				sb.append(printFigures);
				sb.append("\"/>\n");
			}
		}
		
		if (getLength() != defaultLength) {
			sb.append("\t<length val=\"");
			sb.append(getLength());
			sb.append("\"");			    		    	
			sb.append("/>\n");			
		}

	}
	@Override
	public GeoElement copy() {
		return new GeoTextField(cons, labelOffsetX, labelOffsetY);
	}
	/**
	 * @param inputText new value for linkedGeo
	 */
	public void updateLinkedGeo(String inputText) {
		String defineText = inputText;
		if (linkedGeo.isGeoLine()) {

			// not y=
			// and not Line[A,B]
			if ((defineText.indexOf('=') == -1)
					&& (defineText.indexOf('[') == -1)) {
				// x + 1 changed to
				// y = x + 1
				defineText = "y=" + defineText;
			}

			String prefix = linkedGeo.getLabel(tpl) + ":";
			// need a: in front of
			// X = (-0.69, 0) + \lambda (1, -2)
			if (!defineText.startsWith(prefix)) {
				defineText = prefix + defineText;
			}
		} else if (linkedGeo.isGeoText()) {
			defineText = "\"" + defineText + "\"";
		} else if (linkedGeo.isGeoPoint()) {
			if (((GeoPoint) linkedGeo).toStringMode == Kernel.COORD_COMPLEX) {
				// z=2 doesn't work for complex numbers (parses to
				// GeoNumeric)
				defineText = defineText + "+0" + Unicode.IMAGINARY;
			}
		} else if (linkedGeo instanceof FunctionalNVar) {
			// string like f(x,y)=x^2
			// or f(\theta) = \theta
			defineText = linkedGeo.getLabel(tpl) + "("
					+ ((FunctionalNVar) linkedGeo).getVarString(tpl)
					+ ")=" + defineText;
		}
		
		if ("".equals(defineText.trim())) {
			return;
		}
		
		double num = Double.NaN;
		
		// for a simple number, round it to the textfield setting (if set)
		if (linkedGeo.isGeoNumeric() && !linkedGeo.isGeoAngle() && (printDecimals > -1 || printFigures > -1)) {
			try {
				num = Double.parseDouble(inputText);
				defineText = kernel.format(num,  tpl);
				
			} catch (Exception e) {
				// user has entered eg 33deg, 4*3, 2^10, ?
				// do nothing
				e.printStackTrace();
			}
		}

		try {
			linkedGeo = kernel
					.getAlgebraProcessor()
					.changeGeoElementNoExceptionHandling(linkedGeo,
							defineText, linkedGeo.isIndependent(), true); 
			
		} catch (Exception e1) {
			app.showError(e1.getMessage());
			return;
		}
		this.setLinkedGeo(linkedGeo);

		
	}
	
	/**
	 * Called by a Drawable for this object when it is updated
	 * @param textFieldToUpdate the Drawable's text field
	 */
	public void updateText(TextObject textFieldToUpdate){
		
		if (linkedGeo != null) {

			String linkedText;

			if (linkedGeo.isGeoText()) {
				linkedText = ((GeoText) linkedGeo).getTextString();
			} else if (linkedGeo.getParentAlgorithm() instanceof AlgoPointOnPath || linkedGeo.getParentAlgorithm() instanceof AlgoPointInRegion) {
				linkedText = linkedGeo.toValueString(tpl);
			} else {

				// want just a number for eg a=3 but we want variables for eg
				// y=m x + c
				boolean substituteNos = linkedGeo.isGeoNumeric()
						&& linkedGeo.isIndependent();
				linkedText = linkedGeo.getFormulaString(tpl,
						substituteNos);
			}

			if (linkedGeo.isGeoText() && (linkedText.indexOf("\n") > -1)) {
				// replace linefeed with \\n
				while (linkedText.indexOf("\n") > -1) {
					linkedText = linkedText.replaceAll("\n", "\\\\\\\\n");
				}
			}
			if (!textFieldToUpdate.getText().equals(linkedText)) { // avoid redraw error
				textFieldToUpdate.setText(linkedText);
			}

		} else {
			textFieldToUpdate.setText(text);
		}

		setText(textFieldToUpdate.getText());
	
	}
	/**
	 * Called by a Drawable when its text object is updated
	 * @param textFieldToUpdate the Drawable's text field
	 */
	public void textObjectUpdated(TextObject textFieldToUpdate) {
		if (linkedGeo != null) {
			updateLinkedGeo(textFieldToUpdate.getText());
			updateText(textFieldToUpdate);
		} else {
			setText(textFieldToUpdate.getText());
		}
	}
	
	/**
	 * Called by a Drawable when the input is submitted
	 * (e.g. by pressing ENTER)
	 */
	public void textSubmitted() {
		runClickScripts(getText());
	}
	
	/**
	 * @param viewID view ID (AbstractApplication.VIEW_EUCLIDIAN2 or AbstractApplication.VIEW_EUCLIDIAN)
	 * @param drawTextField drawable
	 * @return autocomplete textfield
	 */
	public AutoCompleteTextField getTextField(int viewID, DrawTextField drawTextField) {
		
		if (textField == null) {
			textField = app.getSwingFactory().newAutoCompleteTextField(getLength(), app, drawTextField);
			textField.showPopupSymbolButton(true);
			textField.setAutoComplete(false);
			textField.enableColoring(false);		
					// we want to handle TAB ourselves
					textField.setFocusTraversalKeysEnabled(false);
					textField.setUsedForInputBox(this);
			
		}
		
		if (viewID != App.VIEW_EUCLIDIAN2) {
			return textField;			
		}
		
		if (textField2 == null) {
			textField2 = app.getSwingFactory().newAutoCompleteTextField(getLength(), app, drawTextField);
			textField2.showPopupSymbolButton(true);
			textField2.setAutoComplete(false);
			textField2.enableColoring(false);		
					// we want to handle TAB ourselves
					textField.setFocusTraversalKeysEnabled(false);
					textField.setUsedForInputBox(this);
			
		}
		
		return textField2;
		
	}
	
	@Override
	public void setSelected(boolean flag) {
		super.setSelected(flag);

		if (flag && !textField.hasFocus()) {
			textField.requestFocus();
		}
	}
	
	private void updateTemplate() {

		if (useSignificantFigures() && printFigures > -1) {
			tpl = StringTemplate.printFigures(StringType.GEOGEBRA, printFigures, false);
		} else if (!useSignificantFigures && printDecimals > -1) {
			tpl = StringTemplate.printDecimals(StringType.GEOGEBRA, printDecimals, false);
		} else {
			tpl = StringTemplate.get(StringType.GEOGEBRA);
		}
	}


	private int printDecimals = -1;
	private int printFigures = -1;
	private boolean useSignificantFigures = false;
	private StringTemplate tpl = StringTemplate.defaultTemplate;
	
	@Override
	public int getPrintDecimals() {
		return printDecimals;
	}

	@Override
	public int getPrintFigures() {
		return printFigures;
	}

	@Override
	public void setPrintDecimals(int printDecimals, boolean update) {
		this.printDecimals = printDecimals;
		printFigures = -1;
		useSignificantFigures = false;
		updateTemplate();
	}

	@Override
	public void setPrintFigures(int printFigures, boolean update) {
		this.printFigures = printFigures;
		printDecimals = -1;
		useSignificantFigures = true;
		updateTemplate();
	}

	@Override
	public boolean useSignificantFigures() {
		return useSignificantFigures;
	}




}

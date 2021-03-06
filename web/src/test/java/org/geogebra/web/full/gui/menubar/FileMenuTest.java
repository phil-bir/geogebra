package org.geogebra.web.full.gui.menubar;

import org.geogebra.web.full.gui.menubar.action.FileNewAction;
import org.geogebra.web.full.main.AppWFull;
import org.geogebra.web.html5.main.TestArticleElement;
import org.geogebra.web.test.MockApp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;

/**
 * Tests for Undo with multiple slides
 *
 * @author Zbynek
 *
 */
@RunWith(GwtMockitoTestRunner.class)
@WithClassesToStub({ TextAreaElement.class })
public class FileMenuTest {
	private static AppWFull app;

	/**
	 * Undo / redo with a single slide.
	 */
	@Test
	public void fileNew() {
		app = MockApp
				.mockApplet(new TestArticleElement("canary", "notes"));
		FileNewAction clear = new FileNewAction(app);
		addObject("x");
		clear.execute(null, app);
		app.getSaveController().cancel();
		Assert.assertEquals(0, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
	}

	/**
	 * Make sure asserts don't kill the tests
	 */
	@Before
	public void rootPanel() {
		this.getClass().getClassLoader().setDefaultAssertionStatus(false);
	}

	private static void addObject(String string) {
		app.getKernel().getAlgebraProcessor().processAlgebraCommand(string,
				true);

	}
}

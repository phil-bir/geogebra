package org.geogebra.keyboard.web;

import org.geogebra.common.main.Localization;
import org.geogebra.keyboard.base.Action;
import org.geogebra.web.resources.SVGResource;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.user.client.ui.Image;

/**
 * A KeyBoardButton with functional character. This button doesn't insert a
 * character or anything else, this button performs an action.
 * 
 */
public class KeyBoardButtonFunctionalBase extends KeyBoardButtonBase {

	/**
	 * the possible actions for a functional button
	 * 
	 */
	// public enum Action {
	// ENTER, BACKSPACE, SHIFT, ARROW_LEFT, ARROW_RIGHT, SWITCH_KEYBOARD;
	// }
	private Image image;
	private Action action;

	/**
	 * used for keyboardButtons with an image.
	 * 
	 * @param image
	 *            {@link ImageResource}
	 * @param handler
	 *            {@link ClickHandler}
	 * @param action
	 *            {@link Action}
	 * @param loc
	 *            localization
	 * @param altText
	 *            alternate text for icon
	 */
	public KeyBoardButtonFunctionalBase(ImageResource image,
			ButtonHandler handler,
			Action action, Localization loc, String altText) {
		super(handler);
		this.image = new Image(image);
		this.image.setAltText(loc.getMenu(altText));
		this.action = action;
		this.add(this.image);
		addStyleName("colored");
		this.addStyleName("waves-light");
	}

	/**
	 * @param image
	 *            {@link ImageResource}
	 * @param feedback
	 *            - inserted text
	 * @param handler
	 *            {@link ClickHandler}
	 */
	public KeyBoardButtonFunctionalBase(ImageResource image,
			String feedback, ButtonHandler handler) {
		super(handler);
		this.image = new Image(image);
		this.feedback = feedback;
		this.add(this.image);
	}

	/**
	 * @param svg
	 *            - svg resource
	 * @param feedback
	 *            - text is inserted
	 * @param handler
	 *            {@link ClickHandler}
	 * @param addSupSyle
	 *            - true if add sup style
	 * @param loc
	 *            localization
	 * @param altText
	 *            alternate text for icon
	 */
	public KeyBoardButtonFunctionalBase(SVGResource svg, String feedback,
			ButtonHandler handler, boolean addSupSyle, Localization loc,
			String altText) {
		super(handler);
		this.image = new Image(new ImageResourcePrototype(null,
				svg.getSafeUri(), 0, 0, 24, 24, false, false));
		this.image.setAltText(loc.getMenu(altText));
		this.feedback = feedback;
		this.add(this.image);
		if (addSupSyle) {
			this.addStyleName("sup");
		}
	}

	/**
	 * used for keyboardButtons with an image.
	 * 
	 * @param caption
	 *            String
	 * 
	 * @param handler
	 *            {@link ClickHandler}
	 * @param action
	 *            {@link Action}
	 */
	public KeyBoardButtonFunctionalBase(String caption, ButtonHandler handler,
	        Action action) {
		super(caption, caption, handler);
		this.action = action;
		addStyleName("colored");
	}

	/**
	 * sets the image of the button
	 * 
	 * @param keyboard_shiftDown
	 *            {@link ImageResource}
	 */
	public void setPicture(ImageResource keyboard_shiftDown) {
		if (this.image != null) {
			this.remove(this.image);
		}
		this.image = new Image(keyboard_shiftDown);
		this.add(this.image);
	}

	/**
	 * @return {@link Action}
	 */
	public Action getAction() {
		return this.action;
	}
}

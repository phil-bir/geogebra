package org.geogebra.common.geogebra3D.euclidian3D.draw;

import org.geogebra.common.geogebra3D.euclidian3D.EuclidianView3D;
import org.geogebra.common.geogebra3D.euclidian3D.openGL.Renderer;
import org.geogebra.common.kernel.Matrix.CoordMatrix4x4;
import org.geogebra.common.kernel.Matrix.Coords;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.plugin.EuclidianStyleConstants;

/**
 * Class for drawing decorations of points (altitude segment from the point to
 * xOy plane, ...)
 * 
 * @author matthieu
 *
 */
public class DrawPointDecorations extends DrawCoordSys1D {

	// private GgbMatrix4x4 segmentMatrix;
	private CoordMatrix4x4 planeMatrix;

	private Coords p1;
	private Coords p2;
	private boolean hasBeenUpdated = false;
	private GeoPointND point;

	/**
	 * common constructor
	 * 
	 * @param aView3d
	 *            view
	 */
	public DrawPointDecorations(EuclidianView3D aView3d) {
		super(aView3d);

		setDrawMinMax(0, 1);

		p1 = new Coords(4);
		p1.setW(1);

		p2 = new Coords(4);
		p2.set(p1);
		p2.set(3, 0);

		planeMatrix = CoordMatrix4x4.identity();
		planeMatrix.setVx(Coords.VX.mul(0.2));
		planeMatrix.setVy(Coords.VY.mul(0.2));

	}

	@Override
	public boolean isVisible() {
		if (!getView3D().getRenderer().getGeometryManager().packBuffers()) {
			return true; // no geo connected
		}
		return point != null;
	}

	/**
	 * 
	 * @return true if decorations should be drawn
	 */
	public boolean shouldBeDrawn() {
		return point != null && hasBeenUpdated;
	}

	/**
	 * set the point for which decorations are made
	 * 
	 * @param point0
	 *            decorated point
	 */
	public void setPoint(GeoPointND point0) {

		this.point = point0;
		hasBeenUpdated = false;

	}

	@Override
	public void setWaitForUpdate() {

		if (point != null) {
			super.setWaitForUpdate();
		}
	}

	@Override
	public void drawHidden(Renderer renderer) {
		if (!getView3D().getRenderer().getGeometryManager().packBuffers()) {
			renderer.getTextures()
					.setDashFromLineType(EuclidianStyleConstants.LINE_TYPE_DASHED_LONG);
			drawOutline(renderer);
		}
	}

	@Override
	public void drawOutline(Renderer renderer) {
		if (!getView3D().getRenderer().getGeometryManager().packBuffers()) {
			renderer.setColor(new Coords(0, 0, 0, 1)); // black
			drawGeometry(renderer);
		}
	}

	@Override
	protected boolean updateForItSelf() {

		if (point != null) {
			p1 = point.getInhomCoordsInD3();

			// set origin to projection of the point on xOy plane
			p2 = new Coords(4);
			p2.set(p1);
			p2.set(3, 0);

			planeMatrix.setOrigin(p2);
		}

		updateForItSelf(p1, p2);

		hasBeenUpdated = true;

		return true;
	}

	@Override
	protected void updateLabel() {
		// nothing to do : there's no label
	}

	@Override
	protected void updateLabelPosition() {
		// nothing to do : there's no label
	}

	@Override
	protected int getLineThickness() {
		if (point == null) {
			return 1;
		}

		return Math.max(1, point.getPointSize() / 2);
	}

	@Override
	protected void updateForView() {
		if (getView3D().viewChangedByZoom()) {
			updateForItSelf();
		}
	}

	// /////////////////////////////////////////
	// UNUSED METHODS
	// /////////////////////////////////////////

	@Override
	public int getPickOrder() {
		return 0;
	}

	@Override
	public boolean isTransparent() {
		return false;
	}

	@Override
	protected double getColorShift() {
		return COLOR_SHIFT_NONE;
	}

	@Override
	public void updateColors() {
		// no colors
	}

	@Override
	public boolean doHighlighting() {
		return false;
	}

	@Override
	public int getLineType() {
		return EuclidianStyleConstants.LINE_TYPE_DASHED_LONG;
	}

	@Override
	public int getLineTypeHidden() {
		return EuclidianStyleConstants.LINE_TYPE_HIDDEN_AS_NOT_HIDDEN;
	}

	@Override
	public int getLayer() {
		return Renderer.LAYER_DEFAULT;
	}
}

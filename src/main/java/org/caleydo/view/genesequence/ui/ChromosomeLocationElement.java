/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.ui;

import gleem.linalg.Vec2f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.picking.Pick;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class ChromosomeLocationElement extends PickableGLElement implements
		MultiSelectionManagerMixin.ISelectionMixinCallback, GLLocation.ILocator {

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	private final EDimension dim;
	private final List<Integer> data;
	/**
	 * convert a given id to a normalized range
	 */
	private final Function<Integer, Vec2f> id2range;

	private float start = Float.NaN;
	private float end = Float.NaN;

	public ChromosomeLocationElement(EDimension dim, List<Integer> data, IDType idType,
			Function<Integer, Vec2f> id2range) {
		this.dim = dim;
		this.data = data;
		if (idType != null)
			selections.add(new SelectionManager(idType));
		this.id2range = id2range;
	}

	/**
	 * @return the data, see {@link #data}
	 */
	public List<Integer> getData() {
		return data;
	}

	public IDType getIDType() {
		return noIDType() ? null : selections.get(0).getIDType();
	}

	@Override
	protected void onDragDetected(Pick pick) {
		if (noIDType())
			return;
		if (!pick.isAnyDragging())
			pick.setDoDragging(true);
		this.start = dim.select(toRelative(pick.getPickedPoint())) / dim.select(getSize());
	}

	/**
	 * @return
	 */
	private boolean noIDType() {
		return selections.isEmpty();
	}

	@Override
	protected void onDragged(Pick pick) {
		if (noIDType())
			return;
		this.end = dim.select(toRelative(pick.getPickedPoint())) / dim.select(getSize());

		updateSelection();

		repaint();
	}

	private void updateSelection() {
		SelectionManager m = selections.get(0);
		float a = Math.min(start, end);
		float b = Math.max(start, end);
		Set<Integer> toSelect = getIDs(a, b);
		m.clearSelection(SelectionType.SELECTION);
		m.addToType(SelectionType.SELECTION, toSelect);
		selections.fireSelectionDelta(m);
	}

	Set<Integer> getIDs(float a, float b) {
		Set<Integer> toSelect = new HashSet<>();
		for (Integer id : data) {
			Vec2f v = id2range.apply(id);
			if (v == null || Float.isNaN(v.x()) || Float.isNaN(v.y()))
				continue;
			if ((v.x() >= a && v.x() <= b) || (v.y() >= a && v.y() <= b) || (v.x() < a && v.y() > b))
				toSelect.add(id);
		}
		return toSelect;
	}

	@Override
	protected void onClicked(Pick pick) {
		if (noIDType())
			return;
		if (!Float.isNaN(start)) {
			selections.get(0).clearSelection(SelectionType.SELECTION);
			selections.fireSelectionDelta(selections.get(0));
			start = Float.NaN;
			end = Float.NaN;
			repaint();
		}
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		SelectionManager manager = selections.isEmpty() ? null : selections.get(0);
		final int n = data.size();
		float o = dim.opposite().select(w, h) * 0.1f;

		if (dim.isHorizontal()) {
			g.color(Color.BLACK).drawLine(0, h * 0.5f, w, h * 0.5f);
		} else {
			g.color(Color.BLACK).drawLine(w * 0.5f, 0, w * 0.5f, h);
		}
		for (int i = 0; i < n; ++i) {
			Vec2f v = id2range.apply(data.get(i));
			if (v == null || Float.isNaN(v.x()) || Float.isNaN(v.y()))
				continue;
			SelectionType t = manager == null ? null : manager.getHighestSelectionType(data.get(i));
			if (t == null) {
				g.color(0, 0, 0, 0.5f);
			} else {
				final Color c = t.getColor();
				g.color(c.r, c.g, c.b, 0.5f);
			}
			if (dim.isHorizontal()) {
				g.fillRect(w * v.x(), o, Math.max(w * v.y(), 1), h - o * 2);
			} else {
				g.fillRect(o, h * v.x(), w - o * 2, Math.max(h * v.y(), 1));
			}
		}

		renderSelectionRange(g, w, h, o);
		super.renderImpl(g, w, h);
	}

	private void renderSelectionRange(GLGraphics g, float w, float h, float o) {
		if (Float.isNaN(start) || Float.isNaN(end))
			return;
		final Color c = SelectionType.SELECTION.getColor();
		g.color(c.r, c.g, c.b, 0.2f);
		float a = Math.min(start, end);
		float b = Math.max(start, end);
		if (dim.isHorizontal()) {
			g.fillRect(a * w, o, (b - a) * w, h - o * 2);
		} else {
			g.fillRect(o, a * h, w - o * 2, (b - a) * h);
		}
	}

	/**
	 * @return the id2range, see {@link #id2range}
	 */
	public Function<Integer, Vec2f> getId2range() {
		return id2range;
	}

	/**
	 * @param dim
	 * @return
	 */
	public GLElementDimensionDesc getDesc(EDimension dim) {
		if (this.dim != dim)
			return GLElementDimensionDesc.newFix(20).minimum(10).build();
		return GLElementDimensionDesc.newFix(20).minimum(50).locateUsing(this).build();
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		repaint();
		this.start = Float.NaN;
	}

	@Override
	public GLLocation apply(int dataIndex) {
		float total = dim.select(getSize());
		Vec2f v = id2range.apply(data.get(dataIndex));
		if (v == null || Float.isNaN(v.x()) || Float.isNaN(v.y()))
			return GLLocation.UNKNOWN;
		return new GLLocation(total * v.x(), total * v.y());
	}

	@Override
	public Set<Integer> unapply(GLLocation location) {
		float total = dim.select(getSize());
		float a = (float) location.getOffset() / total;
		float b = (float) location.getOffset2() / total;
		return getIDs(a, b);
	}

	@Override
	public GLLocation apply(Integer input) {
		return GLLocation.applyPrimitive(this, input);
	}
}
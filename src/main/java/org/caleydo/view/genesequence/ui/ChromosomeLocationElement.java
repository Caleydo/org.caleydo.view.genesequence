/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.ui;

import gleem.linalg.Vec2f;

import java.util.List;

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
	private final Function<Integer, Vec2f> id2range;

	public ChromosomeLocationElement(EDimension dim, List<Integer> data, IDType idType,
			Function<Integer, Vec2f> id2range) {
		this.dim = dim;
		this.data = data;
		if (idType != null)
			selections.add(new SelectionManager(idType));
		this.id2range = id2range;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		SelectionManager manager = selections.isEmpty() ? null : selections.get(0);
		int n = data.size();
		float o = dim.opposite().select(w, h) * 0.1f;
		if (dim.isHorizontal()) {
			g.color(Color.BLACK).drawLine(0, h * 0.5f, h, h * 0.5f);

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
				g.fillRect(w * v.x(), o, w * v.y(), h - o);
			} else {
				g.fillRect(o, h * v.x(), w - o, h * v.y());
			}
		}
		super.renderImpl(g, w, h);
	}

	/**
	 * @return the id2range, see {@link #id2range}
	 */
	public Function<Integer, Vec2f> getId2range() {
		return id2range;
	}

	@Override
	protected void onMouseMoved(Pick pick) {

		super.onMouseMoved(pick);
	}

	@Override
	protected void onClicked(Pick pick) {

		super.onClicked(pick);
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
	public GLLocation apply(Integer input) {
		return GLLocation.applyPrimitive(this, input);
	}

	@Override
	public List<GLLocation> apply(Iterable<Integer> dataIndizes) {
		return GLLocation.apply(this, dataIndizes);
	}
}
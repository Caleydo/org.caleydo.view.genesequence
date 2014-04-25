/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal;

import gleem.linalg.open.Vec2i;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.view.genesequence.metadata.ID2ChromosomeLocation;
import org.caleydo.view.genesequence.ui.ChromosomeLocationElement;

import com.google.common.base.Function;

/**
 * @author Christian
 *
 */
public class MutationLocationFactory implements IGLElementFactory2 {

	@Override
	public String getId() {
		return "mutationlocation";
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		@SuppressWarnings("unchecked")
		List<Integer> ids = context.get(List.class, null);
		// and maybe their id type
		IDType idType = context.get(IDType.class, null);

		@SuppressWarnings("unchecked")
		Function<Integer, Vec2i> id2position = context.get("id2position", Function.class, null);
		String chromosome = context.get("chromosome", String.class, null);

		ID2ChromosomeLocation id2range = new ID2ChromosomeLocation(chromosome, id2position);
		ChromosomeLocationElement element = new ChromosomeLocationElement(context.get(EDimension.class,
				EDimension.DIMENSION), ids, idType, id2range);
		String tooltip = context.get("tooltip", String.class, null);
		if (tooltip != null)
			element.setTooltip(tooltip);
		return element;
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		if (context.get(List.class, null) == null || context.get("chromosome", String.class, null) == null
				|| context.get("id2position", Function.class, null) == null)
			// abort
			return false;
		return true;
	}

	@Override
	public GLElementDimensionDesc getDesc(EDimension dim, GLElement elem) {
		return ((ChromosomeLocationElement) elem).getDesc(dim);
	}

	@Override
	public GLElement createParameters(GLElement elem) {
		return null;
	}

}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal.metadata;

import gleem.linalg.Vec2f;
import gleem.linalg.open.Vec2i;

import com.google.common.base.Function;

/**
 * @author Christian
 *
 */
public class ID2ChromosomeLocation implements Function<Integer, Vec2f> {

	protected final Function<Integer, Vec2i> positionFunction;
	protected final int chromosomeTotalLength;

	public ID2ChromosomeLocation(String chromosome, Function<Integer, Vec2i> positionFunction) {
		this.positionFunction = positionFunction;
		chromosomeTotalLength = ChromosomeMetaData.getTotalLength(chromosome);
	}

	@Override
	public Vec2f apply(Integer input) {
		Vec2i position = positionFunction.apply(input);
		if (position == null)
			return null;
		// normalize
		float v = 1.f / chromosomeTotalLength;
		return new Vec2f(position.x() * v, (position.y() - position.x()) * v);
	}

}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal.metadata;

import gleem.linalg.open.Vec2i;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;

/**
 * @author Samuel Gratzl
 *
 */
public class GeneLocationMetaData {
	/**
	 * @param chromosome
	 * @return
	 */
	public static Vec2i getLocation(Integer geneLocationRecordID) {
		ATableBasedDataDomain d = MappingLoader.locationDataDomain;
		Object start = d.getTable().getRaw(1, geneLocationRecordID);
		Object end = d.getTable().getRaw(2, geneLocationRecordID);
		if (!(start instanceof Number) || !(end instanceof Number))
			return null;
		Vec2i v = new Vec2i();
		v.setX(((Number) start).intValue());
		v.setY(((Number) end).intValue());
		return v;
	}
}

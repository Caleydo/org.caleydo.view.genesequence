/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal.metadata;

import java.util.Set;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class ChromosomeMetaData {
	public static Set<String> getChromosomes() {
		IDType c = MappingLoader.chromosome;
		Set<?> chromosomes = IDMappingManagerRegistry.get().getIDMappingManager(c).getAllMappedIDs(c);
		return ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER)
				.addAll(Iterables.transform(chromosomes, Functions.toStringFunction())).build();
	}
	/**
	 * @param chromosome
	 * @return
	 */
	public static int getTotalLength(String chromosome) {
		ATableBasedDataDomain d = MappingLoader.chromoseDataDomain;
		IIDTypeMapper<String, Integer> mapper = d.getRecordIDMappingManager().getIDTypeMapper(MappingLoader.chromosome,
				d.getRecordIDType());
		Set<Integer> indices = mapper.apply(chromosome);
		if (indices == null || indices.size() != 1)
			return 0;
		Object total = d.getTable().getRaw(1, indices.iterator().next());
		return !(total instanceof Number) ? 0 : ((Number) total).intValue();
	}
}

/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;

/**
 * @author Samuel Gratzl
 *
 */
public class ChromosomeMetaData {
	static ATableBasedDataDomain chromoseDataDomain;
	public static IDType chromosome;

	/**
	 * return a list of all known chromosomes
	 *
	 * @return
	 */
	public static Set<String> getChromosomes() {
		IDType c = chromosome;
		Set<?> chromosomes = IDMappingManagerRegistry.get().getIDMappingManager(c).getAllMappedIDs(c);
		return ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER)
				.addAll(Iterables.transform(chromosomes, Functions.toStringFunction())).build();
	}

	/**
	 * return a set of all chromosomes, in which one of the given ids is located
	 *
	 * @param data
	 * @param idType
	 * @return
	 */
	public static final Set<String> getChromosomes(List<Integer> data, IDType idType) {
		IIDTypeMapper<Integer, String> m = getMapper(idType);

		return m.apply(data);
	}

	private static IIDTypeMapper<Integer, String> getMapper(IDType idType) {
		IDMappingManager mapper = IDMappingManagerRegistry.get().getIDMappingManager(idType);

		IIDTypeMapper<Integer, String> m = mapper.getIDTypeMapper(idType, chromosome);
		return m;
	}

	/**
	 * find the chromosome in which most the the genes are located
	 *
	 * @param data
	 * @param idType
	 * @return
	 */
	public static String determineDefaultChromosome(List<Integer> data, IDType idType) {
		IIDTypeMapper<Integer, String> m = getMapper(idType);

		Multiset<String> top = HashMultiset.create();
		Collection<Set<String>> r = m.applySeq(data);
		for (Set<String> s : r)
			top.addAll(s);
		String max = top.iterator().next();
		int count = top.count(max);
		for (String key : top.elementSet()) {
			int c = top.count(key);
			if (c > count) {
				max = key;
				count = c;
			}
		}
		return max;
	}

	/**
	 * @param chromosome
	 * @return the total length of this chromosome or 0 if unknown
	 */
	public static int getTotalLength(String chromosome) {
		ATableBasedDataDomain d = chromoseDataDomain;
		IIDTypeMapper<String, Integer> mapper = d.getRecordIDMappingManager().getIDTypeMapper(
				ChromosomeMetaData.chromosome,
				d.getRecordIDType());
		Set<Integer> indices = mapper.apply(chromosome);
		if (indices == null || indices.size() != 1)
			return 0;
		Object total = d.getTable().getRaw(1, indices.iterator().next());
		return !(total instanceof Number) ? 0 : ((Number) total).intValue();
	}

	/**
	 * @param idType
	 * @return
	 */
	public static boolean isCompatible(IDType idType) {
		return chromosome != null && chromosome.getIDCategory().isOfCategory(idType);
	}
}

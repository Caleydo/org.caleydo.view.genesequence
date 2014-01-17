/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal.metadata;

import gleem.linalg.Vec2f;
import gleem.linalg.open.Vec2i;

import java.util.Objects;
import java.util.Set;

import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class Gene2ChromosomeLocation implements Function<Integer, Vec2f> {
	private final IIDTypeMapper<Integer, Integer> gene2genelocation;
	private final IIDTypeMapper<Integer, String> genelocation2chromosome;

	private String chromosome;
	private int chromosomeTotalLength;

	public Gene2ChromosomeLocation(IDType in, String chromosome) {
		IDMappingManager mapper = IDMappingManagerRegistry.get().getIDMappingManager(in);

		IDType geneLocation = MappingLoader.locationDataDomain.getRecordIDType();
		gene2genelocation = mapper.getIDTypeMapper(in, geneLocation);
		genelocation2chromosome = mapper.getIDTypeMapper(geneLocation, MappingLoader.chromosome);

		this.chromosome = chromosome;
		chromosomeTotalLength = ChromosomeMetaData.getTotalLength(chromosome);
	}

	public void setChromosome(String chromosome) {
		if (Objects.equals(this.chromosome, chromosome))
			return;
		this.chromosome = chromosome;
		this.chromosomeTotalLength = ChromosomeMetaData.getTotalLength(chromosome);
	}

	/**
	 * @return the chromosome, see {@link #chromosome}
	 */
	public String getChromosome() {
		return chromosome;
	}

	@Override
	public Vec2f apply(Integer input) {
		if (chromosomeTotalLength <= 0)
			return null;
		Set<Integer> locations = gene2genelocation.apply(input);
		if (locations == null || locations.isEmpty())
			return null;
		Set<String> chromosomes = genelocation2chromosome.apply(locations);
		if (chromosomes.isEmpty() || !chromosomes.contains(chromosome)) // wrong chromosome
			return null;
		if (locations.size() == 1) { //hit
			return lookup(locations.iterator().next());
		}
		// return the first that is in the right chromosome
		for (Integer loc : locations) {
			chromosomes = genelocation2chromosome.apply(loc);
			if (chromosomes != null && chromosomes.contains(this.chromosome))
				return lookup(loc);
		}
		return null;
	}

	/**
	 * @param next
	 * @return
	 */
	private Vec2f lookup(Integer location) {
		Vec2i loc = GeneLocationMetaData.getLocation(location);
		if (loc == null)
			return null;
		// normalize
		float v = 1.f / chromosomeTotalLength;
		return new Vec2f(loc.x() * v, loc.y() * v);
	}
}

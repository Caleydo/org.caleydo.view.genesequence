package org.caleydo.view.genesequence.internal.util;
/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;


/**
 * @author Samuel Gratzl
 *
 */
public class GAFParser {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Set<Output> out = new LinkedHashSet<>();

		String file = "gene.genome.gaf";

		try (BufferedReader r = new BufferedReader(new FileReader(new File(file)))) {
			String line;
			while ((line = r.readLine()) != null) {
				out.addAll(parse(line));
			}
		}
		try (PrintWriter r = new PrintWriter(new File(file + ".csv"))) {
			Multiset<String> keys = HashMultiset.create();
			r.append("GeneLocation").append('\t').append("Gene").append('\t').append("Chromosome").append('\t');
			r.append("Start").append('\t').append("End").append('\t').append("Strand").println();

			for (Output o : out) {
				String pair = o.gene + "@" + o.chromosome;
				int count = keys.count(pair);
				keys.add(pair);
				if (count > 0)
					pair += "#" + count;
				r.append(pair).append('\t');
				r.append(o.gene).append('\t');
				r.append(o.chromosome).append('\t');
				r.append(String.valueOf(o.start)).append('\t');
				r.append(String.valueOf(o.end)).append('\t');
				r.append(o.strand);
				r.println();
			}
		}

		try (PrintWriter r = new PrintWriter(new File(file + ".gene2loc.csv"))) {
			Multiset<String> keys = HashMultiset.create();
			r.append("Gene").append('\t').append("GeneLocation").println();

			for (Output o : out) {
				String pair = o.gene + "@" + o.chromosome;
				int count = keys.count(pair);
				keys.add(pair);
				if (count > 0)
					pair += "#" + count;
				r.append(o.gene).append('\t');
				r.append(pair);
				r.println();
			}
		}

		try (PrintWriter r = new PrintWriter(new File(file + ".loc2chr.csv"))) {
			Multiset<String> keys = HashMultiset.create();
			r.append("GeneLocation").append('\t').append("Chromosome").println();

			for (Output o : out) {
				String pair = o.gene + "@" + o.chromosome;
				int count = keys.count(pair);
				keys.add(pair);
				if (count > 0)
					pair += "#" + count;
				r.append(pair).append('\t');
				r.append(o.chromosome);
				r.println();
			}
		}

	}

	/**
	 * @param line
	 * @return
	 */
	private static Collection<Output> parse(String line) {
		String[] lines = line.split("\t");
		// 4705 C9orf62|157927 gene calculated genomic GRCh37-lite genome NCBI GRCh37-lite pairwise
		// 1-223,224-655,656-1920 chr9:138235095-138235317,138235868-138236299,138237140-138238404:+ C9orf62|157927
		// chr9:138235095-138238404:+ Confidence=400
		// P
		// Q
		String gene = lines[15];
		String locs = lines[16];
		gene = gene.substring(0, gene.indexOf('|'));
		List<Output> r = new ArrayList<>();
		for (String loc : locs.split(";")) {
			// chr17:3900705-3910012:-;chr17:4481333-4481583:-
			String[] locSplitted = loc.split(":");
			if (locSplitted.length < 2)
				continue;
			String chromosome = locSplitted[0];
			// System.out.println(loc);
			String[] range = locSplitted[1].split("-");
			int start = Integer.parseInt(range[0]);
			int end = range.length == 1 ? start : Integer.parseInt(range[1]);
			String strand = (locSplitted.length > 2) ? locSplitted[2] : "";
			r.add(new Output(gene, chromosome, start, end, strand));
		}
		return r;
	}

	static class Output {
		String gene;
		String chromosome;
		int start;
		int end;
		String strand;

		public Output(String gene, String chromosome, int start, int end, String strand) {
			this.gene = gene;
			this.chromosome = chromosome;
			this.start = start;
			this.end = end;
			this.strand = strand;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((chromosome == null) ? 0 : chromosome.hashCode());
			result = prime * result + end;
			result = prime * result + ((gene == null) ? 0 : gene.hashCode());
			result = prime * result + start;
			result = prime * result + ((strand == null) ? 0 : strand.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Output other = (Output) obj;
			if (chromosome == null) {
				if (other.chromosome != null)
					return false;
			} else if (!chromosome.equals(other.chromosome))
				return false;
			if (end != other.end)
				return false;
			if (gene == null) {
				if (other.gene != null)
					return false;
			} else if (!gene.equals(other.gene))
				return false;
			if (start != other.start)
				return false;
			if (strand == null) {
				if (other.strand != null)
					return false;
			} else if (!strand.equals(other.strand))
				return false;
			return true;
		}

	}
}

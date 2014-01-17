package org.caleydo.view.genesequence.internal;

import gleem.linalg.Vec2f;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.genesequence.internal.metadata.Gene2ChromosomeLocation;
import org.caleydo.view.genesequence.internal.metadata.MappingLoader;
import org.caleydo.view.genesequence.ui.ChromosomeLocationElement;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

public class GeneSequenceFactory implements IGLElementFactory2 {

	@Override
	public String getId() {
		return "genesequence";
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		@SuppressWarnings("unchecked")
		List<Integer> data = context.get(List.class, null);
		IDType idType = context.get(IDType.class, null);

		@SuppressWarnings("unchecked")
		Function<Integer, Vec2f> id2range = context.get("id2range", Function.class, null);
		if (id2range == null) {
			String chromosome = context.get("chromosome", String.class, null);
			Set<String> chromosmes = getAllChromosomes(data, idType);
			if (!chromosmes.contains(chromosome))
				chromosome = findDefaultChromosome(data, idType);
			id2range = new Gene2ChromosomeLocation(idType, chromosome);
		}

		return new ChromosomeLocationElement(context.get(EDimension.class, EDimension.DIMENSION), data, idType,
				id2range);
	}

	/**
	 * find the chromosome in which most the the genes are located
	 *
	 * @param data
	 * @param idType
	 * @return
	 */
	private static String findDefaultChromosome(List<Integer> data, IDType idType) {
		IDMappingManager mapper = IDMappingManagerRegistry.get().getIDMappingManager(idType);

		IIDTypeMapper<Integer, String> m = mapper.getIDTypeMapper(idType, MappingLoader.chromosome);

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

	private static final Set<String> getAllChromosomes(List<Integer> data, IDType idType) {
		IDMappingManager mapper = IDMappingManagerRegistry.get().getIDMappingManager(idType);

		IIDTypeMapper<Integer, String> m = mapper.getIDTypeMapper(idType, MappingLoader.chromosome);

		return m.apply(data);
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		if(context.get(List.class, null) == null)
			return false;
		if (context.get("id2range", Function.class, null) == null) {
			IDType idType = context.get(IDType.class, null);
			if (idType == null || !MappingLoader.chromosome.getIDCategory().isOfCategory(idType))
				return false;
		}
		return true;
	}

	@Override
	public GLElementDimensionDesc getDesc(EDimension dim, GLElement elem) {
		return ((ChromosomeLocationElement) elem).getDesc(dim);
	}

	@Override
	public GLElement createParameters(GLElement elem) {
		ChromosomeLocationElement c = (ChromosomeLocationElement)elem;
		Function<Integer, Vec2f> f = c.getId2range();
		if (f instanceof Gene2ChromosomeLocation) {
			Set<String> chromosomes = getAllChromosomes(c.getData(), c.getIDType());
			return new ChromosomeSelector((Gene2ChromosomeLocation) f, elem, chromosomes);
		}
		return null;
	}

	private static final class ChromosomeSelector extends GLComboBox<String> implements
			GLComboBox.ISelectionCallback<String> {
		private Gene2ChromosomeLocation f;
		private GLElement elem;

		/**
		 * @param elem
		 * @param f
		 *
		 */
		public ChromosomeSelector(Gene2ChromosomeLocation f, GLElement elem, Set<String> chromosomes) {
			super(Lists.newArrayList(chromosomes), GLComboBox.DEFAULT, GLRenderers
					.fillRect(Color.WHITE));
			this.f = f;
			this.elem = elem;
			setCallback(this);
			setSelectedItem(f.getChromosome());
			setzDeltaList(2);
			setSize(100, -1);
		}

		@Override
		public void onSelectionChanged(GLComboBox<? extends String> widget, String item) {
			f.setChromosome(item);
			elem.repaintAll();
		}
	}
}

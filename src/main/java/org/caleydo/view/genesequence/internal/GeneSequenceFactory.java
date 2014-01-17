package org.caleydo.view.genesequence.internal;

import gleem.linalg.Vec2f;

import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.genesequence.internal.metadata.ChromosomeMetaData;
import org.caleydo.view.genesequence.internal.metadata.Gene2ChromosomeLocation;
import org.caleydo.view.genesequence.ui.ChromosomeLocationElement;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class GeneSequenceFactory implements IGLElementFactory2 {

	@Override
	public String getId() {
		return "genesequence";
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		// we need some ids
		@SuppressWarnings("unchecked")
		List<Integer> data = context.get(List.class, null);
		// and maybe their id type
		IDType idType = context.get(IDType.class, null);

		// check if we have a function that converts ids to a normalized range
		@SuppressWarnings("unchecked")
		Function<Integer, Vec2f> id2range = context.get("id2range", Function.class, null);

		if (id2range == null) {
			// no lets to a chromosome mapping
			String chromosome = context.get("chromosome", String.class, null);
			Set<String> chromosmes = ChromosomeMetaData.getChromosomes(data, idType);
			if (!chromosmes.contains(chromosome))
				chromosome = ChromosomeMetaData.determineDefaultChromosome(data, idType);
			id2range = new Gene2ChromosomeLocation(idType, chromosome);
		}

		return new ChromosomeLocationElement(context.get(EDimension.class, EDimension.DIMENSION), data, idType,
				id2range);
	}



	@Override
	public boolean apply(GLElementFactoryContext context) {
		if (context.get(List.class, null) == null) // no list abort
			return false;
		if (context.get("id2range", Function.class, null) == null) { // no function
			IDType idType = context.get(IDType.class, null);
			// need a gene id types
			if (idType == null || !ChromosomeMetaData.isCompatible(idType))
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
		// if we have a Gene2ChromosomeLocation mapping, create a selector for the chromosome
		if (f instanceof Gene2ChromosomeLocation) {
			Set<String> chromosomes = ChromosomeMetaData.getChromosomes(c.getData(), c.getIDType());
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

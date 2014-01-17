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
import org.caleydo.view.genesequence.internal.metadata.MappingLoader;
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
		@SuppressWarnings("unchecked")
		List<Integer> data = context.get(List.class, null);
		IDType idType = context.get(IDType.class, null);

		@SuppressWarnings("unchecked")
		Function<Integer, Vec2f> id2range = context.get("id2range", Function.class, null);
		if (id2range == null) {
			String chromosome = context.get("chromosome", String.class, null);
			Set<String> chromosmes = ChromosomeMetaData.getChromosomes();
			if (!chromosmes.contains(chromosome))
				chromosome = chromosmes.iterator().next();
			id2range = new Gene2ChromosomeLocation(idType, chromosome);
		}

		return new ChromosomeLocationElement(context.get(EDimension.class, EDimension.DIMENSION), data, idType,
				id2range);
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		if(context.get(List.class, null) == null)
			return false;
		if (context.get("id2range", Function.class, null) == null) {
			IDType idType = context.get(IDType.class, null);
			if (idType == null || MappingLoader.chromosome.getIDCategory().isOfCategory(idType))
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
			return new ChromosomeSelector((Gene2ChromosomeLocation) f, elem);
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
		public ChromosomeSelector(Gene2ChromosomeLocation f, GLElement elem) {
			super(Lists.newArrayList(ChromosomeMetaData.getChromosomes()), GLComboBox.DEFAULT, GLRenderers
					.fillRect(Color.WHITE));
			this.f = f;
			this.elem = elem;
			setCallback(this);
			setSelectedItem(f.getChromosome());

			setSize(100, -1);
		}

		@Override
		public void onSelectionChanged(GLComboBox<? extends String> widget, String item) {
			f.setChromosome(item);
			elem.repaintAll();
		}
	}
}

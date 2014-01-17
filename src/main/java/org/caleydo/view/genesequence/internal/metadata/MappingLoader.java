/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal.metadata;

import static org.caleydo.view.genesequence.internal.metadata.ChromosomeMetaData.chromoseDataDomain;
import static org.caleydo.view.genesequence.internal.metadata.ChromosomeMetaData.chromosome;
import static org.caleydo.view.genesequence.internal.metadata.GeneLocationMetaData.location;
import static org.caleydo.view.genesequence.internal.metadata.GeneLocationMetaData.locationDataDomain;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.data.collection.table.TableUtils;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.IDataDomainInitialization;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IDTypeInitializer;
import org.caleydo.core.io.ColumnDescription;
import org.caleydo.core.io.DataDescription;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.IDSpecification;
import org.caleydo.core.io.parser.ascii.IDMappingParser;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ZipUtils;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.util.system.RemoteFile;
import org.caleydo.datadomain.genetic.EGeneIDTypes;
import org.caleydo.datadomain.genetic.GeneticMetaData;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author Samuel Gratzl
 *
 */
public class MappingLoader implements IDataDomainInitialization, IRunnableWithProgress {
	private static final String URL_PATTERN = GeneralManager.DATA_URL_PREFIX + "mappings/%s_sequence.zip";
	private static final Logger log = Logger.create(MappingLoader.class);
	private static boolean isAlreadyInitialized = false;

	@Override
	public void createIDTypesAndMapping() {
		if (isAlreadyInitialized)
			return;

		if (loadMapping())
			isAlreadyInitialized = true;
	}

	private static boolean loadMapping() {
		File base = prepareFile(new NullProgressMonitor());
		if (base == null)
			return true;

		final IDType geneSymbol = IDType.getIDType(EGeneIDTypes.GENE_SYMBOL.name());
		if (geneSymbol == null) // other not initialized
			return false;

		// register new id types
		final IDCategory cat = geneSymbol.getIDCategory();
		chromosome = IDType.registerType("Chromosome", cat, EDataType.STRING);
		location = IDType.registerType("ChromosomeLocation", cat, EDataType.STRING);

		// load mappings
		IDMappingParser.loadMapping(toFile(base, "gene.genome.gaf.gene2loc.csv"), 1, -1, geneSymbol, location,
				"\t", cat, true, true, false, null, null);
		IDMappingParser.loadMapping(toFile(base, "gene.genome.gaf.loc2chr.csv"), 1, -1, location, chromosome, "\t",
				cat, true, true, false, null, null);

		// load meta data
		// load not just the mapping but also the data domain with the meta data
		chromoseDataDomain = loadData(createChromosomeDataDesc(chromosome, base));
		fixLabels(chromoseDataDomain, "Chromosome Name", "Chromosome Total Length");
		locationDataDomain = loadData(createLocationDataDesc(location, base));
		fixLabels(locationDataDomain, "Chromosome Location (Start)", "Chromosome Location (End)",
				"Chromosome Location (Strand)");
		return true;
	}

	/**
	 * @param createChromosomeDataDesc
	 * @param nullProgressMonitor
	 * @return
	 */
	private static ATableBasedDataDomain loadData(DataSetDescription d) {
		d = IDTypeInitializer.initIDs(d);
		GeneMetaDataDataDomain dataDomain = new GeneMetaDataDataDomain();
		dataDomain.setDataSetDescription(d);
		dataDomain.init();
		DataDomainManager.get().register(dataDomain);
		try {
			// the place the matrix is stored:
			TableUtils.loadData(dataDomain, d, true, true);
		} catch (Exception e) {
			log.error("Failed to load data for dataset " + d.getDataSetName(), e);
			DataDomainManager.get().unregister(dataDomain);
		}
		return dataDomain;
	}

	/**
	 * @param chromoseDataDomain2
	 * @param string
	 * @param string2
	 */
	private static void fixLabels(ATableBasedDataDomain d, String... labels) {
		for (TablePerspective tp : d.getAllTablePerspectives()) {
			Perspective p = tp.getDimensionPerspective();
			if (p.getVirtualArray().size() != 1)
				continue;
			Integer index = p.getVirtualArray().get(0);
			if (index != null && index >= 0 && index < labels.length) {
				p.setLabel(labels[index]);
				tp.setLabel(labels[index]);
			}
		}
	}

	/**
	 * @param chromosome
	 * @param base
	 * @return
	 */
	private static DataSetDescription createChromosomeDataDesc(IDType chromosome, File base) {
		DataSetDescription d = new DataSetDescription();
		d.setDataSetName("Chromosome_MetaData");
		d.setColor(Color.NEUTRAL_GREY);
		d.setDataSourcePath(toFile(base, "chromosomeMetaData.csv"));
		d.setTransposeMatrix(false);
		d.setDataDescription(null);
		d.setRowIDSpecification(new IDSpecification(chromosome.getIDCategory().getCategoryName(), chromosome
				.getTypeName()));

		{
			IDCategory idCategory = IDCategory.registerInternalCategory("chromosomeMetaData_column");
			IDType.registerInternalType("chromosomeMetaData_column", idCategory, EDataType.STRING);
			d.setColumnIDSpecification(new IDSpecification("chromosomeMetaData_column", "chromosomeMetaData_column"));
		}
		d.setContainsColumnIDs(false);
		d.addParsingPattern(new ColumnDescription(1, new DataDescription(EDataClass.UNIQUE_OBJECT, EDataType.STRING)));
		d.addParsingPattern(new ColumnDescription(2, new DataDescription(EDataClass.NATURAL_NUMBER, EDataType.INTEGER)));

		return d;
	}

	/**
	 * @param location
	 * @param base
	 * @return
	 */
	private static DataSetDescription createLocationDataDesc(IDType location, File base) {
		DataSetDescription d = new DataSetDescription();
		d.setDataSetName("GeneChromosomeLocation_MetaData");
		d.setColor(Color.NEUTRAL_GREY);
		d.setDataSourcePath(toFile(base, "gene.genome.gaf.csv"));
		d.setTransposeMatrix(false);
		d.setDataDescription(null);
		d.setRowIDSpecification(new IDSpecification(location.getIDCategory().getCategoryName(), location.getTypeName()));

		{
			IDCategory idCategory = IDCategory.registerInternalCategory("GeneChromosomeLocation_MetaData_column");
			IDType.registerInternalType("GeneChromosomeLocation_MetaData_column", idCategory, EDataType.STRING);
			d.setColumnIDSpecification(new IDSpecification("GeneChromosomeLocation_MetaData_column",
					"GeneChromosomeLocation_MetaData_column"));
		}
		d.setContainsColumnIDs(false);
		d.addParsingPattern(new ColumnDescription(3, new DataDescription(EDataClass.NATURAL_NUMBER, EDataType.INTEGER)));
		d.addParsingPattern(new ColumnDescription(4, new DataDescription(EDataClass.NATURAL_NUMBER, EDataType.INTEGER)));
		d.addParsingPattern(new ColumnDescription(5, new DataDescription(EDataClass.CATEGORICAL, EDataType.STRING)));

		return d;
	}

	private static String toFile(File base, String string) {
		return new File(base, string).getAbsolutePath();
	}

	private static File prepareFile(IProgressMonitor monitor) {
		URL url = null;
		try {
			url = new URL(String.format(URL_PATTERN, GeneticMetaData.getOrganism().name().toLowerCase()));
			RemoteFile zip = RemoteFile.of(url);
			File localZip = zip.getOrLoad(true, monitor, "Caching Mapping Data (this may take a while): Downloading "
					+ GeneticMetaData.getOrganism().getLabel() + " (%2$d MB)");
			if (localZip == null || !localZip.exists()) {
				log.error("can't download: " + url);
				return null;
			}
			File unpacked = new File(localZip.getParentFile(), localZip.getName().replaceAll("\\.zip", ""));
			if (unpacked.exists())
				return unpacked;
			ZipUtils.unzipToDirectory(localZip.getAbsolutePath(), unpacked.getAbsolutePath());
			return unpacked;
		} catch (MalformedURLException e) {
			log.error("can't download: " + url);
			return null;
		}

	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		// loading zip and extracting it during initialization
		prepareFile(monitor);
	}
}


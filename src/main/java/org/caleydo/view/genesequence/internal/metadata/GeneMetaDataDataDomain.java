/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.genesequence.internal.metadata;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.id.IDCreator;

/**
 * @author Samuel Gratzl
 *
 */
public class GeneMetaDataDataDomain extends ATableBasedDataDomain {

	public final static String DATA_DOMAIN_TYPE = "org.caleydo.datadomain.genetic.meta";

	public GeneMetaDataDataDomain() {
		super(DATA_DOMAIN_TYPE, DATA_DOMAIN_TYPE + DataDomainManager.DATA_DOMAIN_INSTANCE_DELIMITER
				+ IDCreator.createPersistentID(GeneMetaDataDataDomain.class));
		isSerializeable = false;
	}

}

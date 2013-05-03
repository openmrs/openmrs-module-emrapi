/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.metadata.MetadataPackageConfig;
import org.openmrs.module.emrapi.metadata.MetadataPackagesConfig;
import org.openmrs.module.metadatasharing.ImportConfig;
import org.openmrs.module.metadatasharing.ImportedPackage;
import org.openmrs.module.metadatasharing.MetadataSharing;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.openmrs.module.metadatasharing.wrapper.PackageImporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataUtil {
	
	protected static final Log log = LogFactory.getLog(MetadataUtil.class);
	
	public static final String PACKAGES_FILENAME = "packages.xml";
	
	/**
	 * Setup the standard metadata packages
	 * 
	 * @return
	 */
	public static boolean setupStandardMetadata(ClassLoader loader) throws Exception {
        MetadataPackagesConfig config = getMetadataPackagesForModule(loader);
        return loadPackages(config, loader);
    }

    private synchronized static boolean loadPackages(MetadataPackagesConfig config, ClassLoader loader) throws IOException {
        boolean anyChanges = false;

        for (MetadataPackageConfig pkg : config.getPackages()) {
            anyChanges |= installMetadataPackageIfNecessary(pkg, loader);
        }

        return anyChanges;
    }

    public static MetadataPackagesConfig getMetadataPackagesForModule(ClassLoader loader) {
        try {
            InputStream stream = loader.getResourceAsStream(PACKAGES_FILENAME);
            String xml = IOUtils.toString(stream);
            MetadataPackagesConfig config = Context.getSerializationService().getDefaultSerializer()
                    .deserialize(xml, MetadataPackagesConfig.class);
            return config;
        }
        catch (Exception ex) {
            throw new RuntimeException("Cannot find " + PACKAGES_FILENAME + ". Make sure it's in api/src/main/resources");
        }
    }
	
	/**
	 * Checks whether the given version of the MDS package has been installed yet, and if not,
	 * install it
	 * 
	 * @param config the metadata package configuration object
	 * @param loader the class loader to use for loading the packages
	 * @return whether any changes were made to the db
	 * @throws IOException
	 */
	private static boolean installMetadataPackageIfNecessary(MetadataPackageConfig config, ClassLoader loader)
	    throws IOException {
        String filename = config.getFilenameBase() + "-" + config.getVersion().toString() + ".zip";
        try {

			Matcher matcher = Pattern.compile("(?:.+/)?\\w+-(\\d+).zip").matcher(filename);
			if (!matcher.matches())
				throw new RuntimeException("Filename must match PackageNameWithNoSpaces-X.zip");
			Integer version = Integer.valueOf(matcher.group(1));
			
			ImportedPackage installed = Context.getService(MetadataSharingService.class).getImportedPackageByGroup(
			    config.getGroupUuid());
			if (installed != null && installed.getVersion() >= version) {
				log.info("Metadata package " + config.getFilenameBase() + " is already installed with version "
				        + installed.getVersion());
				return false;
			}
			
			if (loader.getResource(filename) == null) {
				throw new RuntimeException("Cannot find " + filename + " for group " + config.getGroupUuid());
			}
			
			PackageImporter metadataImporter = MetadataSharing.getInstance().newPackageImporter();
			metadataImporter.setImportConfig(ImportConfig.valueOf(config.getImportMode()));
			metadataImporter.loadSerializedPackageStream(loader.getResourceAsStream(filename));
			metadataImporter.importPackage();
			return true;
		}
		catch (Exception ex) {
			log.error("Failed to install metadata package " + filename, ex);
			return false;
		}
	}
}

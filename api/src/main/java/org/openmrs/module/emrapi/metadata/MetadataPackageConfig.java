/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.metadata;

import org.openmrs.module.metadatasharing.ImportMode;

public class MetadataPackageConfig {
	
	private String filenameBase;
	
	private String groupUuid;
	
	private Integer version;
	
	private ImportMode importMode;
	
	public MetadataPackageConfig() {
	}
	
	public MetadataPackageConfig(String filenameBase, String groupUuid, Integer version, ImportMode importMode) {
		this.filenameBase = filenameBase;
		this.groupUuid = groupUuid;
		this.version = version;
		this.importMode = importMode;
	}
	
	public String getFilenameBase() {
		return filenameBase;
	}
	
	public void setFilenameBase(String filenameBase) {
		this.filenameBase = filenameBase;
	}
	
	public String getGroupUuid() {
		return groupUuid;
	}
	
	public void setGroupUuid(String groupUuid) {
		this.groupUuid = groupUuid;
	}
	
	public ImportMode getImportMode() {
		return importMode;
	}
	
	public void setImportMode(ImportMode importMode) {
		this.importMode = importMode;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
}

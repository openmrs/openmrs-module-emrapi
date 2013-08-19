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

package org.openmrs.module.emrapi.adt;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides a mapping between encounter types and visit types from a comma delimited list of mappings of the following format
 *
 * 1. "3:4, 5:2, 1:2, 2:2" for encounterTypeId:visitTypeId
 * 2. encounterTypeUuid:visitTypeUuid
 * 3. A mixture of uuids and id
 * 4. default:visitTypeId or default:visitTypeUuid which maps all encounter types to the specified visitType
 *
 * Any specific mapping will override the default mapping
 *
 *
 */
@Component
public class EncounterTypetoVisitTypeMapper {
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private AdministrationService adminService;
	
	private String mappingString;
	
	public VisitType getVisitTypeForEncounter(Encounter encounter) {
		return getVisitTypeForEncounterType(encounter.getEncounterType());
	}
	
	public VisitType getVisitTypeForEncounterType(EncounterType encounterType) {
		updateMappings();
		
		if (StringUtils.isBlank(mappingString)) {
			return null;
		}
			
		String[] mappings = mappingString.split(",");
		VisitType defaultVisitType = null;
		VisitType visitType = null;
		for (String mapping : mappings) {
			int index = mapping.indexOf(':');
			if (index > 0) {
				String mappedEncounterTypeIdOrUuid = mapping.substring(0, index).trim();
				if ("default".equals(mappedEncounterTypeIdOrUuid) ||
						encounterType.getId().toString().equals(mappedEncounterTypeIdOrUuid)
						|| encounterType.getUuid().equals(mappedEncounterTypeIdOrUuid)) {
					
					String visitTypeIdOrUuid = mapping.substring(index + 1).trim();
					if (StringUtils.isNumeric(visitTypeIdOrUuid)) {
						if ("default".equals(mappedEncounterTypeIdOrUuid)) {
							defaultVisitType = visitService.getVisitType(Integer.parseInt(visitTypeIdOrUuid));
						} else {
							visitType = visitService.getVisitType(Integer.parseInt(visitTypeIdOrUuid));
						}
					} else {
						if ("default".equals(mappedEncounterTypeIdOrUuid)) {
							defaultVisitType = visitService.getVisitTypeByUuid(visitTypeIdOrUuid);
						} else {
							visitType = visitService.getVisitTypeByUuid(visitTypeIdOrUuid);
						}
					}
					
				}
			}
		}
		// Return any mapped visit type over the default
		if (visitType == null) {
			return defaultVisitType;
		}  else {
			return visitType;
		}
	}
	
	public String getMappingString() {
		return mappingString;
	}
	
	public void setMappingString(String mappingString) {
		this.mappingString = mappingString;
	}
	
	public VisitService getVisitService() {
		return visitService;
	}
	
	public void setVisitService(VisitService visitService) {
		this.visitService = visitService;
	}
	
	public AdministrationService getAdminService() {
		return adminService;
	}
	
	public void setAdminService(AdministrationService adminService) {
		this.adminService = adminService;
	}
	
	public void updateMappings() {
		if (StringUtils.isBlank(mappingString)) {
			mappingString = adminService.getGlobalProperty(EmrApiConstants.GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP);
		}
	}
}

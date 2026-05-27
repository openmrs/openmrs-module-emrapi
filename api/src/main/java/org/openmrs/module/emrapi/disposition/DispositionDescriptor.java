/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.disposition;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.descriptor.ConceptSetDescriptor;
import org.openmrs.module.emrapi.descriptor.ConceptSetDescriptorField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Describes the concepts necessary (and optional, like admission location, transfer location, and
 * date of death) for recording a Disposition concept set
 */
public class DispositionDescriptor extends ConceptSetDescriptor {
	
	private Concept dispositionSetConcept;
	
	private Concept dispositionConcept;
	
	private Concept admissionLocationConcept;
	
	private Concept internalTransferLocationConcept;
	
	private Concept dateOfDeathConcept;
	
	public DispositionDescriptor(ConceptService conceptService) {
		setup(conceptService, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME,
		    ConceptSetDescriptorField.required("dispositionSetConcept",
		        EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET),
		    ConceptSetDescriptorField.required("dispositionConcept", EmrApiConstants.CONCEPT_CODE_DISPOSITION),
		    ConceptSetDescriptorField.optional("admissionLocationConcept", EmrApiConstants.CONCEPT_CODE_ADMISSION_LOCATION),
		    ConceptSetDescriptorField.optional("internalTransferLocationConcept",
		        EmrApiConstants.CONCEPT_CODE_INTERNAL_TRANSFER_LOCATION),
		    ConceptSetDescriptorField.optional("dateOfDeathConcept", EmrApiConstants.CONCEPT_CODE_DATE_OF_DEATH));
	}
	
	/**
	 * Used for testing -- in production you'll use the constructor that takes ConceptService
	 */
	public DispositionDescriptor() {
	}
	
	public Concept getDispositionSetConcept() {
		return dispositionSetConcept;
	}
	
	public void setDispositionSetConcept(Concept dispositionSetConcept) {
		this.dispositionSetConcept = dispositionSetConcept;
	}
	
	public Concept getDispositionConcept() {
		return dispositionConcept;
	}
	
	public void setDispositionConcept(Concept dispositionConcept) {
		this.dispositionConcept = dispositionConcept;
	}
	
	public Concept getAdmissionLocationConcept() {
		return admissionLocationConcept;
	}
	
	public void setAdmissionLocationConcept(Concept admissionLocationConcept) {
		this.admissionLocationConcept = admissionLocationConcept;
	}
	
	public Concept getInternalTransferLocationConcept() {
		return internalTransferLocationConcept;
	}
	
	public void setInternalTransferLocationConcept(Concept internalTransferLocationConcept) {
		this.internalTransferLocationConcept = internalTransferLocationConcept;
	}
	
	public Concept getDateOfDeathConcept() {
		return dateOfDeathConcept;
	}
	
	public void setDateOfDeathConcept(Concept dateOfDeathConcept) {
		this.dateOfDeathConcept = dateOfDeathConcept;
	}
	
	public Obs buildObsGroup(Disposition disposition, EmrConceptService emrConceptService) {
		Obs dispoObs = new Obs();
		dispoObs.setConcept(dispositionConcept);
		dispoObs.setValueCoded(emrConceptService.getConcept(disposition.getConceptCode()));
		
		Obs group = new Obs();
		group.setConcept(dispositionSetConcept);
		group.addGroupMember(dispoObs);
		return group;
	}
	
	public boolean isDisposition(Obs obs) {
		return obs.getConcept().equals(dispositionSetConcept);
	}
	
	public Obs getDispositionObs(Obs obsGroup) {
		return findMember(obsGroup, dispositionConcept);
	}
	
	public Obs getAdmissionLocationObs(Obs obsGroup) {
		return findMember(obsGroup, admissionLocationConcept);
	}
	
	public Obs getInternalTransferLocationObs(Obs obsGroup) {
		return findMember(obsGroup, internalTransferLocationConcept);
	}
	
	public Obs getDateOfDeathObs(Obs obsGroup) {
		return findMember(obsGroup, dateOfDeathConcept);
	}
	
	public Location getAdmissionLocation(Obs obsGroup, LocationService locationService) {
		Obs admissionLocationObs = getAdmissionLocationObs(obsGroup);
		if (admissionLocationObs != null) {
			return locationService.getLocation(Integer.valueOf(admissionLocationObs.getValueText()));
		} else {
			return null;
		}
	}
	
	public Location getInternalTransferLocation(Obs obsGroup, LocationService locationService) {
		Obs transferLocationObs = getInternalTransferLocationObs(obsGroup);
		if (transferLocationObs != null) {
			return locationService.getLocation(Integer.valueOf(transferLocationObs.getValueText()));
		} else {
			return null;
		}
	}
	
	public Date getDateOfDeath(Obs obsGroup) {
		Obs dateOfDeathObs = getDateOfDeathObs(obsGroup);
		if (dateOfDeathObs != null) {
			return dateOfDeathObs.getValueDate();
		} else {
			return null;
		}
	}
	
	public List<Obs> getAdditionalObs(Obs obsGroup) {
		List<Obs> notDisposition = new ArrayList<Obs>();
		if (obsGroup.hasGroupMembers()) {
			for (Obs candidate : obsGroup.getGroupMembers()) {
				if (!candidate.getConcept().equals(dispositionConcept)
				        && !candidate.getConcept().equals(admissionLocationConcept)
				        && !candidate.getConcept().equals(internalTransferLocationConcept)
				        && !candidate.getConcept().equals(dateOfDeathConcept)) {
					notDisposition.add(candidate);
				}
			}
		}
		return notDisposition;
	}
}

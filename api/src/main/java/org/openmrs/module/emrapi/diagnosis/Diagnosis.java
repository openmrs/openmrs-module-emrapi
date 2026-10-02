/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.diagnosis;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Represents a recorded presumed/confirmed diagnosis, and whether it is primary/secondary. (It is
 * straightforward to extend this to include diagnosis certainty, date, and additional ordering.)
 */
public class Diagnosis {
	
	CodedOrFreeTextAnswer diagnosis;
	
	@JsonProperty
	Order order;
	
	@JsonProperty
	Certainty certainty = Certainty.PRESUMED;
	
	Obs existingObs;
	
	/**
	 * @since 1.25.0
	 */
	Integer existingDiagnosis;
	
	public Diagnosis() {
	}
	
	public Diagnosis(CodedOrFreeTextAnswer diagnosis) {
		this.diagnosis = diagnosis;
	}
	
	public Diagnosis(CodedOrFreeTextAnswer diagnosis, Order order) {
		this(diagnosis);
		this.order = order;
	}
	
	public Diagnosis(CodedOrFreeTextAnswer diagnosis, Order order, Certainty certainty) {
		this(diagnosis, order);
		this.certainty = certainty;
	}
	
	public CodedOrFreeTextAnswer getDiagnosis() {
		return diagnosis;
	}
	
	public void setDiagnosis(CodedOrFreeTextAnswer diagnosis) {
		this.diagnosis = diagnosis;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	public Certainty getCertainty() {
		return certainty;
	}
	
	public void setCertainty(Certainty certainty) {
		this.certainty = certainty;
	}
	
	public Obs getExistingObs() {
		return existingObs;
	}
	
	public void setExistingObs(Obs existingObs) {
		this.existingObs = existingObs;
	}
	
	/**
	 * @since 1.25.0
	 */
	public void setExistingDiagnosis(Integer existingDiagnosis) {
		this.existingDiagnosis = existingDiagnosis;
	}
	
	/**
	 * @since 1.25.0
	 */
	public Integer getExistingDiagnosis() {
		return existingDiagnosis;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Diagnosis)) {
			return false;
		}
		Diagnosis other = (Diagnosis) o;
		return OpenmrsUtil.nullSafeEquals(diagnosis, other.getDiagnosis())
		        && OpenmrsUtil.nullSafeEquals(order, other.getOrder())
		        && OpenmrsUtil.nullSafeEquals(certainty, other.getCertainty());
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(diagnosis).append(order).append(certainty).toHashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (diagnosis != null && diagnosis.getValue() != null) {
			s.append(diagnosis.getValue().toString());
		}
		if (certainty != null) {
			s.append(", ").append(certainty);
		}
		if (order != null) {
			s.append(", ").append(order);
		}
		return s.toString();
	}
	
	public enum Order {
		
		PRIMARY(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY),
		SECONDARY(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_SECONDARY);
		
		String codeInEmrConceptSource;
		
		Order(String codeInEmrConceptSource) {
			this.codeInEmrConceptSource = codeInEmrConceptSource;
		}
		
		String getCodeInEmrConceptSource() {
			return codeInEmrConceptSource;
		}
		
		public static Order parseConceptReferenceCode(String code) {
			for (Order candidate : values()) {
				if (candidate.getCodeInEmrConceptSource().equals(code)) {
					return candidate;
				}
			}
			return null;
		}
	}
	
	public enum Certainty {
		
		CONFIRMED(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED),
		PRESUMED(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_PRESUMED);
		
		String codeInEmrConceptSource;
		
		Certainty(String codeInEmrConceptSource) {
			this.codeInEmrConceptSource = codeInEmrConceptSource;
		}
		
		String getCodeInEmrConceptSource() {
			return codeInEmrConceptSource;
		}
		
		public static Certainty parseConceptReferenceCode(String code) {
			for (Certainty candidate : values()) {
				if (candidate.getCodeInEmrConceptSource().equals(code)) {
					return candidate;
				}
			}
			return null;
		}
	}
	
}

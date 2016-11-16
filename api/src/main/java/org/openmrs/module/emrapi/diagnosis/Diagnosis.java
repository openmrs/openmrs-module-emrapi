/*
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

package org.openmrs.module.emrapi.diagnosis;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Represents a recorded presumed/confirmed diagnosis, and whether it is primary/secondary.
 * (It is straightforward to extend this to include diagnosis certainty, date, and additional ordering.)
 */
public class Diagnosis {

    CodedOrFreeTextAnswer diagnosis;

    @JsonProperty
    Order order;

    @JsonProperty
    Certainty certainty = Certainty.PRESUMED;

    Obs existingObs;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Diagnosis)) {
            return false;
        }
        Diagnosis other = (Diagnosis) o;
        return OpenmrsUtil.nullSafeEquals(diagnosis, other.getDiagnosis()) &&
                OpenmrsUtil.nullSafeEquals(order, other.getOrder()) &&
				OpenmrsUtil.nullSafeEquals(certainty, other.getCertainty());
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
			s.append(", " + certainty);
		}
		if (order != null) {
			s.append(", " + order);
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

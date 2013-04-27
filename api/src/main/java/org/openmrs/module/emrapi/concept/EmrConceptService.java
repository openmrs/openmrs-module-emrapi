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

package org.openmrs.module.emrapi.concept;

import org.openmrs.Concept;
import org.openmrs.ConceptReferenceTerm;

import java.util.List;

/**
 * Additional useful methods not (yet) available via the core OpenMRS API
 */
public interface EmrConceptService {

    /**
     * @param term
     * @return all concepts with SAME-AS or NARROWER-THAN mappings to term
     */
    List<Concept> getConceptsSameOrNarrowerThan(ConceptReferenceTerm term);

}

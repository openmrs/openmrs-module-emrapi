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

package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.Obs;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;

/**
 * Helper for building Obs in unit tests
 */
public class ObsBuilder1_12 extends ObsBuilder{

    Obs obs = super.get();

    public ObsBuilder setFormField(String formNameSpace, String formFieldPath){
        obs.setFormField(formNameSpace, formFieldPath);
        return this;
    }
}

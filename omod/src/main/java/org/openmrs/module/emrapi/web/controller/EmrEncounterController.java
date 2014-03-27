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

package org.openmrs.module.emrapi.web.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.emrapi.encounter.ActiveEncounterParameters;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterSearchParameters;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.web.exception.InvalidInputException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/emrapi/encounter")
public class EmrEncounterController extends BaseRestController {

    @Autowired
    private EmrEncounterService emrEncounterService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public EncounterTransaction update(@RequestBody EncounterTransaction encounterTransaction) {
        return emrEncounterService.save(encounterTransaction);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/active")
    @ResponseBody
    public EncounterTransaction getActiveEncounter(ActiveEncounterParameters activeEncounterParameters) {
        return emrEncounterService.getActiveEncounter(activeEncounterParameters);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<EncounterTransaction> find(EncounterSearchParameters encounterSearchParameters) {
        checkForValidInput(encounterSearchParameters);
        return emrEncounterService.find(encounterSearchParameters);
    }

    private void checkForValidInput(EncounterSearchParameters encounterSearchParameters) {
        String visitUuid = encounterSearchParameters.getVisitUuid();
        if (StringUtils.isBlank(visitUuid))
            throw new InvalidInputException("Visit UUID cannot be empty.");

        String encounterDate = encounterSearchParameters.getEncounterDate();
        if (StringUtils.isNotBlank(encounterDate)){
            try {
                new SimpleDateFormat("yyyy-MM-dd").parse(encounterDate);
            } catch (ParseException e) {
                throw new InvalidInputException("Date format needs to be 'yyyy-MM-dd'. Incorrect Date:" + encounterDate + ".", e);
            }
        }
    }
}




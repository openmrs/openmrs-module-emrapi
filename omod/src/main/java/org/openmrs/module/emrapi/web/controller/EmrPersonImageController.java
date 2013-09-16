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

import org.apache.commons.io.IOUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.patient.image.EmrPersonImageService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Controller
@RequestMapping(value = "/rest/emrapi/personimage")
public class EmrPersonImageController extends BaseRestController {

    @Autowired
    private EmrPersonImageService emrPersonImageService;

    @RequestMapping(method = RequestMethod.POST, value = "/{personUuid}")
    @ResponseBody
    public Object update(@PathVariable("personUuid") String personUuid, @RequestBody SimpleObject post,
                         HttpServletResponse response) throws Exception {
        emrPersonImageService.save(personUuid, (String) post.get("image"));
        return RestUtil.noContent(response);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{personUuid}.jpeg")
    public void get(@PathVariable("personUuid") String personUuid, HttpServletResponse response) throws Exception {
        InputStream inputStream = emrPersonImageService.get(personUuid);
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }

}

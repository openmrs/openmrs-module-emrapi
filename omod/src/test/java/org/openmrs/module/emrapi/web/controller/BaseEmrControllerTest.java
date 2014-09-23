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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Ignore;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Ignore
public class BaseEmrControllerTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private AnnotationMethodHandlerAdapter handlerAdapter;

    @Autowired
    private List<DefaultAnnotationHandlerMapping> handlerMappings;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a request from the given parameters.
     * <p>
     * The requestURI is automatically preceded with "/rest/" + RestConstants.VERSION_1.
     *
     * @param method
     * @param requestURI
     * @return
     */
    public MockHttpServletRequest request(RequestMethod method, String requestURI) {
        MockHttpServletRequest request = new MockHttpServletRequest(method.toString(), requestURI);
        request.addHeader("content-type", "application/json");
        return request;
    }

    public static class Parameter {

        public String name;

        public String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public MockHttpServletRequest newRequest(RequestMethod method, String requestURI, Parameter... parameters) {
        MockHttpServletRequest request = request(method, requestURI);
        for (Parameter parameter : parameters) {
            request.addParameter(parameter.name, parameter.value);
        }
        return request;
    }

    public MockHttpServletRequest newDeleteRequest(String requestURI, Parameter... parameters) {
        return newRequest(RequestMethod.DELETE, requestURI, parameters);
    }

    public MockHttpServletRequest newGetRequest(String requestURI, Parameter... parameters) {
        return newRequest(RequestMethod.GET, requestURI, parameters);
    }

    public MockHttpServletRequest newPostRequest(String requestURI, Object content) {
        MockHttpServletRequest request = request(RequestMethod.POST, requestURI);
        try {
            String json = new ObjectMapper().writeValueAsString(content);
            request.setContent(json.getBytes("UTF-8"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    public MockHttpServletRequest newPostRequest(String requestURI, String content) {
        MockHttpServletRequest request = request(RequestMethod.POST, requestURI);
        try {
            request.setContent(content.getBytes("UTF-8"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    /**
     * Passes the given request to a proper controller.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public MockHttpServletResponse handle(HttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        HandlerExecutionChain handlerExecutionChain = null;
        for (DefaultAnnotationHandlerMapping handlerMapping : handlerMappings) {
            handlerExecutionChain = handlerMapping.getHandler(request);
            if (handlerExecutionChain != null) {
                break;
            }
        }
        Assert.assertNotNull("The request URI does not exist", handlerExecutionChain);

        handlerAdapter.handle(request, response, handlerExecutionChain.getHandler());

        return response;
    }

    /**
     * Deserializes the JSON response.
     *
     * @param response
     * @param type
     * @return
     * @throws Exception
     */
    public <T> T deserialize(MockHttpServletResponse response, Class<T> type) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), type);
    }

    /**
     * Deserializes the JSON response.
     *
     * @param response
     * @param typeReference
     * @return
     * @throws Exception
     */
    public <T> T deserialize(MockHttpServletResponse response, final TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), typeReference);
    }
}

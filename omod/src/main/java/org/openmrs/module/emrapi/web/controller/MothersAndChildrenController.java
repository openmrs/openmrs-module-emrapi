package org.openmrs.module.emrapi.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.emrapi.maternal.MaternalService;
import org.openmrs.module.emrapi.maternal.MotherAndChild;
import org.openmrs.module.emrapi.maternal.MothersAndChildrenSearchCriteria;
import org.openmrs.module.emrapi.rest.converter.SimpleBeanConverter;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MothersAndChildrenController {

    @Autowired
    private MaternalService maternalService;

    @RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/maternal/mothersAndChildren")
    @ResponseBody
    public SimpleObject getMothersAndChildren(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false, value = "motherUuids") String motherUuids,
            @RequestParam(required = false, value = "childUuids") String childUuids,
            @RequestParam(required = false, value = "requireMotherHasActiveVisit") boolean requireMotherHasActiveVisit,
            @RequestParam(required = false, value = "requireChildHasActiveVisit") boolean requireChildHasActiveVisit,
            @RequestParam(required = false, value = "requireChildBornDuringMothersActiveVisit") boolean requireChildBornDuringMothersActiveVisit
    ) {
        RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);
        MothersAndChildrenSearchCriteria criteria = new MothersAndChildrenSearchCriteria();
        criteria.setMotherUuids(StringUtils.isNotBlank(motherUuids) ? Arrays.asList(motherUuids.split(",")) : null);
        criteria.setChildUuids(StringUtils.isNotBlank(childUuids) ? Arrays.asList(childUuids.split(",")) : null);
        criteria.setMotherRequiredToHaveActiveVisit(requireMotherHasActiveVisit);
        criteria.setChildRequiredToHaveActiveVisit(requireChildHasActiveVisit);
        criteria.setChildRequiredToBeBornDuringMothersActiveVisit(requireChildBornDuringMothersActiveVisit);
        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(criteria);
        return new NeedsPaging<>(motherAndChildList, context).toSimpleObject(new SimpleBeanConverter<MotherAndChild>());
    }

}

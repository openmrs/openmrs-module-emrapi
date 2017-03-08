package org.openmrs.module.emrapi.rest.resource;

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.openmrs.module.emrapi.person.image.PersonImage;
import org.openmrs.module.emrapi.rest.exception.PersonNotFoundException;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/personimage", supportedClass = PersonImage.class, supportedOpenmrsVersions = {"1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*"})
public class PersonImageResource extends DelegatingCrudResource<PersonImage> {

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT, Representation.FULL);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        return new DelegatingResourceDescription();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("person", Representation.DEFAULT);
        description.addProperty("base64EncodedImage");
        return description;
    }

    @Override
    public PersonImage newDelegate() {
        return new PersonImage();
    }

    @Override
    public PersonImage save(PersonImage personImage) {
        return Context.getService(EmrPersonImageService.class).savePersonImage(personImage);
    }

    @Override
    public PersonImage getByUniqueId(String personUuid) {
        Person person = Context.getPersonService().getPersonByUuid(personUuid);
        if (person == null) {
            throw new PersonNotFoundException(String.format("Person with UUID:%s not found.", personUuid));
        }
        return Context.getService(EmrPersonImageService.class).getCurrentPersonImage(person);
    }

    @Override
    public Object retrieve(String uuid, RequestContext context) throws ResponseException {
        PersonImage personImage = getByUniqueId(uuid);
        InputStream inputStream;

        try {
            inputStream = new FileInputStream(personImage.getSavedImage());
            OpenmrsUtil.copyFile(inputStream, context.getResponse().getOutputStream());
            context.getResponse().flushBuffer();
        } catch (FileNotFoundException e) {
            throw new ObjectNotFoundException();
        } catch (IOException e) {
            throw new GenericRestException("Failure when loading the file for uuid: " + uuid, e.getCause());
        }
        return null;
    }

    @Override
    protected void delete(PersonImage delegate, String reason, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete of person image not supported");
    }

    @Override
    public void purge(PersonImage delegate, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete forever of person image not supported");
    }

}

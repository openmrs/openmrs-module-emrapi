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

package org.openmrs.module.emrapi.printer;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.printer.Printer;
import org.openmrs.module.emrapi.printer.PrinterService;
import org.openmrs.module.emrapi.printer.PrinterValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrinterValidatorTest {

    private PrinterValidator validator;

    private PrinterService printerService;

    private Printer printer;

    @Before
    public void setValidator() {
        validator = new PrinterValidator();
        validator.setMessageSourceService(mock(MessageSourceService.class));
        printerService = mock(PrinterService.class);
        validator.setPrinterService(printerService);
        when(printerService.isIpAddressAllocatedToAnotherPrinter(any(Printer.class))).thenReturn(false);

        printer = new Printer();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionIfNull() throws Exception {
        Errors errors = new BindException(printer, "printer");
        validator.validate(null, errors);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_shouldThrowExceptionIfNotPrinter() throws Exception {
        Errors errors = new BindException(printer, "printer");
        validator.validate(new Person(), errors);
    }

    @Test
    public void validate_shouldRejectAnEmptyName() throws Exception {
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("10.10.10.10");
        printer.setPort("8080");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("name"));
    }

    @Test
    public void validate_shouldRejectAnEmptyIpAddress() throws Exception {
        printer.setName("Test Printer");
        printer.setType(Printer.Type.ID_CARD);
        printer.setPort("8080");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("ipAddress"));
    }

    @Test
    public void validate_shouldRejectAnEmptyPort() throws Exception {
        printer.setName("Test Printer");
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("10.10.10.10");
        printer.setPort("");  // need to do this because port has a default value

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("port"));
    }

    @Test
    public void validate_shouldRejectAnEmptyType() throws Exception {
        printer.setName("Test Printer");
        printer.setIpAddress("10.10.10.10");
        printer.setPort("8080");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("type"));
    }

    @Test
    public void validate_shouldRejectNameGreaterThan255Characters() throws Exception {
        printer.setName("Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer " +
                "Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer " +
                "Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer Test Printer ");
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("10.10.10.10");
        printer.setPort("8080");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("name"));
    }

    @Test
    public void validate_shouldRejectIpAddressGreaterThan50Characters() throws Exception {
        printer.setName("Test Printer");
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10.10");
        printer.setPort("8080");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("ipAddress"));
    }

    @Test
    public void validate_validPrinterShouldPass() throws Exception {
        printer.setName("Test Printer");
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("192.1.1.1");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(!errors.hasErrors());
    }

    @Test
    public void validate_invalidIpAddressShouldFail() throws Exception {
        printer.setName("Test Printer");
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("10-ABC%");
        printer.setPort("8080");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("ipAddress"));
    }

    @Test
    public void validate_duplicateIpAddressShouldFail() throws Exception {

        when(printerService.isIpAddressAllocatedToAnotherPrinter(any(Printer.class))).thenReturn(true);

        printer.setName("Test Printer");
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("10.10.10.10");
        printer.setPort("8080");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("ipAddress"));
    }

    @Test
    public void validate_invalidPortAddressShouldFail() throws Exception {
        printer.setName("Test Printer");
        printer.setType(Printer.Type.ID_CARD);
        printer.setIpAddress("10-ABC%");
        printer.setPort("8ABC");

        Errors errors = new BindException(printer, "printer");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("port"));

        errors = new BindException(printer, "printer");
        printer.setPort("777777");
        validator.validate(printer, errors);
        assertTrue(errors.hasFieldErrors("port"));
    }

}

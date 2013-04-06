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

import org.apache.commons.lang.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.messagesource.MessageSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Handler(supports = {Printer.class}, order = 50)
public class PrinterValidator implements Validator {

    private static final String IPV4_PATTERN = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String IPV6_PATTERN = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    private Pattern ipV4Pattern = Pattern.compile(IPV4_PATTERN, Pattern.CASE_INSENSITIVE);
    private Pattern ipV6Pattern = Pattern.compile(IPV6_PATTERN, Pattern.CASE_INSENSITIVE);

    @Autowired
    @Qualifier("messageSourceService")
    private MessageSourceService messageSourceService;

    @Autowired
    @Qualifier("printerService")
    private PrinterService printerService;

    /**
     * @param messageSourceService the messageSourceService to set
     */
    public void setMessageSourceService(MessageSourceService messageSourceService) {
        this.messageSourceService = messageSourceService;
    }

    /**
     * @param printerService the printer service to set
     */
    public void setPrinterService(PrinterService printerService) {
        this.printerService = printerService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Printer.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        if (obj == null || !(obj instanceof Printer)) {
            throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + Printer.class);
        }

        Printer printer = (Printer) obj;

        if (StringUtils.isBlank(printer.getName())) {
            errors.rejectValue("name", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.printer.name")}, null);
        }

        if (StringUtils.isBlank(printer.getIpAddress())) {
            errors.rejectValue("ipAddress", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.printer.ipAddress")}, null);
        }

        if (StringUtils.isBlank(printer.getPort())) {
            errors.rejectValue("port", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.printer.port")}, null);
        }

        if (printer.getType() == null) {
            errors.rejectValue("type", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.printer.type")}, null);
        }

        if (printer.getName() != null && printer.getName().length() > 256) {
            errors.rejectValue("name", "emr.printer.error.nameTooLong", null, null);
        }

        if (printer.getIpAddress() != null && printer.getIpAddress().length() > 50) {
            errors.rejectValue("ipAddress", "emr.printer.error.ipAddressTooLong", null, null);
        }

        if (printerService.isNameAllocatedToAnotherPrinter(printer)) {
            errors.rejectValue("name", "emr.printer.error.nameDuplicate", null, null);
        }

        // verify ip address
        if (printer.getIpAddress() != null) {

            if (!(ipV4Pattern.matcher(printer.getIpAddress()).matches() || ipV6Pattern.matcher(printer.getIpAddress()).matches())) {
                errors.rejectValue("ipAddress", "emr.printer.error.ipAddressInvalid", null, null);
            }

            if (printerService.isIpAddressAllocatedToAnotherPrinter(printer)) {
                errors.rejectValue("ipAddress", "emr.printer.error.ipAddressInUse", null, null);
            }
        }


        // verify port
        if (printer.getPort() != null) {
            Integer portInteger;

            try {
                portInteger = Integer.parseInt(printer.getPort());
                if (portInteger < 0 || portInteger > 65536) {
                    errors.rejectValue("port", "emr.printer.error.portInvalid", null, null);
                }

            } catch (NumberFormatException e) {
                errors.rejectValue("port", "emr.printer.error.portInvalid", null, null);
            }

        }


    }


}

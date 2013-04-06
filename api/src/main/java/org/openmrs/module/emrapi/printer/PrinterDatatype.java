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
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * Datatype for serializing the Printer class
 */
@Component
public class PrinterDatatype extends SerializingCustomDatatype<Printer> {

    private Printer.Type supportedPrinterType;

    @Override
    public Printer deserialize(String serializedValue) {

        if (StringUtils.isBlank(serializedValue)) {
            return null;
        }
        try {
            // TODO: we can't autowire this because this isn't a bean--see TRUNK-3823
            return Context.getService(PrinterService.class).getPrinterById(Integer.parseInt(serializedValue));
        } catch (Exception ex) {
            throw new InvalidCustomValueException("Invalid Printer: " + serializedValue);
        }
    }

    @Override
    public String serialize(Printer typedValue) {
        return typedValue.getId().toString();
    }

    @Override
    public void setConfiguration(String config) {
        supportedPrinterType = Printer.Type.valueOf(config);
    }

    @Override
    public void validate(Printer printer) {
        if (!printer.getType().equals(supportedPrinterType)) {
            throw new InvalidCustomValueException("Printer of type " + printer.getType() + " not supported for this printer datatype");
        }
    }

}

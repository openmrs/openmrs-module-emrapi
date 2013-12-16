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
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PrinterServiceTest {

    private PrinterService printerService;

    private LocationService locationService;

    private LocationAttributeType defaultLabelPrinterAttributeType;

    private LocationAttributeType defaultIdCardPrinterAttributeType;

    @Before
    public void setup() {


        printerService = new PrinterServiceImpl();

        defaultLabelPrinterAttributeType = new LocationAttributeType();
        defaultIdCardPrinterAttributeType = new LocationAttributeType();

        locationService = mock(LocationService.class);
        when(locationService.getLocationAttributeTypeByUuid(eq("bd6c1c10-38d3-11e2-81c1-0800200c9a66"))).thenReturn(defaultLabelPrinterAttributeType);
        when(locationService.getLocationAttributeTypeByUuid(eq("b48ef9a0-38d3-11e2-81c1-0800200c9a66"))).thenReturn(defaultIdCardPrinterAttributeType);
        ((PrinterServiceImpl) printerService).setLocationService(locationService);
    }

    @Test
    public void shouldSetDefaultLabelPrinterForLocation() {

        Printer printer = new Printer();
        printer.setId(1);
        printer.setType(Printer.Type.LABEL);

        Location location = new Location(1);

        printerService.setDefaultPrinter(location, Printer.Type.LABEL, printer);

        assertThat((Printer) location.getActiveAttributes(defaultLabelPrinterAttributeType).get(0).getValue(), is(printer));
    }

    @Test
    public void shouldGetDefaultLabelPrinterForLocation() {

        Printer printer = new Printer();
        printer.setId(1);
        printer.setType(Printer.Type.LABEL);

        Location location = new Location(1);

        printerService.setDefaultPrinter(location, Printer.Type.LABEL, printer);

        Printer fetchedPrinter = printerService.getDefaultPrinter(location, Printer.Type.LABEL);
        assertThat(fetchedPrinter, is(printer));
    }

    @Test
    public void shouldSetDefaultIdCardPrinterForLocation() {

        Printer printer = new Printer();
        printer.setId(1);
        printer.setType(Printer.Type.ID_CARD);

        Location location = new Location(1);

        printerService.setDefaultPrinter(location, Printer.Type.ID_CARD, printer);

        assertThat((Printer) location.getActiveAttributes(defaultIdCardPrinterAttributeType).get(0).getValue(), is(printer));
    }

    @Test
    public void shouldGetDefaultIdCardPrinterForLocation() {

        Printer printer = new Printer();
        printer.setId(1);
        printer.setType(Printer.Type.ID_CARD);

        Location location = new Location(1);

        printerService.setDefaultPrinter(location, Printer.Type.ID_CARD, printer);

        Printer fetchedPrinter = printerService.getDefaultPrinter(location, Printer.Type.ID_CARD);
        assertThat(fetchedPrinter, is(printer));
    }

    @Test
    public void shouldRemoveDefaultPrinter() {

        // first set a default printer
        Printer printer = new Printer();
        printer.setId(1);
        printer.setType(Printer.Type.ID_CARD);

        Location location = new Location(1);

        printerService.setDefaultPrinter(location, Printer.Type.ID_CARD, printer);

        // now set it to back to null
        printerService.setDefaultPrinter(location, Printer.Type.ID_CARD, null);
        assertNull(printerService.getDefaultPrinter(location, Printer.Type.ID_CARD));

    }


    @Test
    public void shouldReturnNullIfNoDefaultPrinterDefinedForLocation() {

        Printer printer = new Printer();
        printer.setId(1);
        printer.setType(Printer.Type.ID_CARD);

        Location location = new Location(1);

        printerService.setDefaultPrinter(location, Printer.Type.ID_CARD, printer);

        // note that we set the default ID CARD printer, but then try to fetch the default label printer
        assertNull(printerService.getDefaultPrinter(location, Printer.Type.LABEL));
    }


}



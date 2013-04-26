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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.printer.Printer;
import org.openmrs.module.emrapi.printer.PrinterService;
import org.openmrs.module.emrapi.printer.PrinterServiceImpl;
import org.openmrs.module.emrapi.printer.UnableToPrintViaSocketException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest(IOUtils.class)
public class PrinterServiceTest {

    private PrinterService printerService;

    private LocationService locationService;

    private LocationAttributeType defaultLabelPrinterAttributeType;

    private LocationAttributeType defaultIdCardPrinterAttributeType;

    private Socket mockedSocket;

    @Before
    public void setup() {
        mockedSocket = mock(Socket.class);

        printerService = new PrinterServiceStub();

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

    @Test
    public void shouldPrintToSocket() throws IOException, UnableToPrintViaSocketException {

        mockStatic(IOUtils.class);

        Printer printer = new Printer();
        printer.setIpAddress("127.0.0.1") ;
        printer.setPort("9100");

        String testData = "test data";

        printerService.printViaSocket(testData, printer, "UTF-8");
        verify(mockedSocket).connect(argThat(new IsExpectedSocketAddress("127.0.0.1", "9100")), eq(1000));
        verify(mockedSocket).getOutputStream();

        verifyStatic();
        IOUtils.write(eq(testData), any(OutputStream.class), eq("UTF-8"));
    }

    @Test
    public void shouldPrintToSocketUsingWindowsEncoding() throws IOException, UnableToPrintViaSocketException {

        mockStatic(IOUtils.class);

        Printer printer = new Printer();
        printer.setIpAddress("127.0.0.1") ;
        printer.setPort("9100");

        String testData = "test data";

        printerService.printViaSocket(testData, printer, "Windows-1252");
        verify(mockedSocket).connect(argThat(new IsExpectedSocketAddress("127.0.0.1", "9100")), eq(1000));
        verify(mockedSocket).getOutputStream();

        verifyStatic();
        IOUtils.write(eq(testData.getBytes("Windows-1252")), any(OutputStream.class));
    }

    @Test(expected = UnableToPrintViaSocketException.class)
    public void shouldFailIfInvalidIpAddress() throws UnableToPrintViaSocketException {

        mockStatic(IOUtils.class);

        Printer printer = new Printer();
        printer.setIpAddress("999.999.999.999") ;
        printer.setPort("9100");

        String testData = "test data";

        printerService.printViaSocket(testData, printer, "UTF-8");
    }

    @Test(expected = UnableToPrintViaSocketException.class)
    public void shouldFailIfInvalidPort() throws UnableToPrintViaSocketException {

        mockStatic(IOUtils.class);

        Printer printer = new Printer();
        printer.setIpAddress("127.0.0.1") ;
        printer.setPort("bad_port");

        String testData = "test data";

        printerService.printViaSocket(testData, printer, "UTF-8");
    }


    public class PrinterServiceStub extends PrinterServiceImpl {

        // mock the socket
        @Override
        protected Socket createSocket() {
            return mockedSocket;
        }

    }

    private class IsExpectedSocketAddress extends ArgumentMatcher<SocketAddress> {

        private InetSocketAddress expectedSocketAddress;

        public IsExpectedSocketAddress(String ipAddress, String port) throws UnknownHostException {
            InetAddress addr = InetAddress.getByName(ipAddress);
            expectedSocketAddress = new InetSocketAddress(addr, Integer.valueOf(port));
        }

        @Override
        public boolean matches(Object o) {
            InetSocketAddress actualSocketAddress = (InetSocketAddress) o;

            assertThat(actualSocketAddress.getAddress(), is(expectedSocketAddress.getAddress()));
            assertThat(actualSocketAddress.getPort(), is(expectedSocketAddress.getPort()));

            return true;
        }
    }

}



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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.printer.db.PrinterDAO;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

public class PrinterServiceImpl extends BaseOpenmrsService implements PrinterService {

    private final Log log = LogFactory.getLog(getClass());

    private PrinterDAO printerDAO;

    private LocationService locationService;

    public void setPrinterDAO(PrinterDAO printerDAO) {
        this.printerDAO = printerDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Printer getPrinterById(Integer id) {
        return printerDAO.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Printer getPrinterByName(String name) {
        return printerDAO.getPrinterByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Printer> getPrintersByType(Printer.Type type) {
        return printerDAO.getPrintersByType(type);
    }

    @Override
    @Transactional
    public void savePrinter(Printer printer) {
        printerDAO.saveOrUpdate(printer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Printer> getAllPrinters() {
        return printerDAO.getAll();
    }

    @Override
    public void setDefaultPrinter(Location location, Printer.Type type, Printer printer) {

        LocationAttributeType locationAttributeType = getLocationAttributeTypeDefaultPrinter(type);

        // if no printer is specified, void any existing default printer
        if (printer == null) {
            for (LocationAttribute attr : location.getActiveAttributes(locationAttributeType)) {
                attr.setVoided(true);
            }
        } else {
            LocationAttribute defaultPrinter = new LocationAttribute();
            defaultPrinter.setAttributeType(locationAttributeType);
            defaultPrinter.setValue(printer);
            location.setAttribute(defaultPrinter);
        }

        locationService.saveLocation(location);
    }

    @Override
    public Printer getDefaultPrinter(Location location, Printer.Type type) {

        List<LocationAttribute> defaultPrinters = location.getActiveAttributes(getLocationAttributeTypeDefaultPrinter(type));

        if (defaultPrinters == null || defaultPrinters.size() == 0) {
            return null;
        }

        if (defaultPrinters.size() > 1) {
            throw new IllegalStateException("Multiple default printer of type " + type + " defined for " + location);
        }

        return (Printer) defaultPrinters.get(0).getValue();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isIpAddressAllocatedToAnotherPrinter(Printer printer) {

        // for testing purposes, we allow two printers to both be assigned the localhost ip
        if (printer.getIpAddress().equals("127.0.0.1")) {
            return false;
        }

        return printerDAO.isIpAddressAllocatedToAnotherPrinter(printer);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNameAllocatedToAnotherPrinter(Printer printer) {
        return printerDAO.isNameAllocatedToAnotherPrinter(printer);
    }

    @Override
    public void printViaSocket(String data, Printer.Type type, Location location, String encoding)
            throws UnableToPrintViaSocketException {
        Printer printer = getDefaultPrinter(location, type);

        if (printer == null) {
            throw new IllegalStateException("No default printer assigned for " + location.getDisplayString() + ". Please contact your system administrator");
        }

        printViaSocket(data, printer, encoding);
    }

    @Override
    public void printViaSocket(String data, Printer printer, String encoding)
            throws UnableToPrintViaSocketException {

        Socket socket = null;
        // Create a socket with a timeout
        try {
            InetAddress addr = InetAddress.getByName(printer.getIpAddress());
            SocketAddress sockaddr = new InetSocketAddress(addr, Integer.valueOf(printer.getPort()));

            // Create an unbound socket
            socket = createSocket();

            // This method will block no more than timeoutMs.
            // If the timeout occurs, SocketTimeoutException is thrown.
            int timeoutMs = 1000;   // 1s
            socket.connect(sockaddr, timeoutMs);

            if (encoding.equals("Windows-1252")) {
                IOUtils.write(data.toString().getBytes("Windows-1252"), socket.getOutputStream());
            } else {
                IOUtils.write(data.toString(), socket.getOutputStream(), encoding);
            }
        } catch (Exception e) {
            throw new UnableToPrintViaSocketException("Unable to print to printer " + printer.getName(), e);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                log.error("failed to close the socket to printer " + printer.getName(), e);
            }
        }
    }

    // do this is separate method so that we can override it for test purposes
    protected Socket createSocket() {
        return new Socket();
    }


    private LocationAttributeType getLocationAttributeTypeDefaultPrinter(Printer.Type type) {

        String locationAttributeTypeUuid = EmrApiConstants.LOCATION_ATTRIBUTE_TYPE_DEFAULT_PRINTER.get(type.name());
        LocationAttributeType locationAttributeType = locationService.getLocationAttributeTypeByUuid(locationAttributeTypeUuid);

        if (locationAttributeType == null) {
            throw new IllegalStateException("Unable to fetch location attribute type for default " + type + " printer");
        }

        return locationAttributeType;
    }
}

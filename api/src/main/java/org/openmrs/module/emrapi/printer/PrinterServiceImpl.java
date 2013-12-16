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
        printViaSocket(data, printer, encoding, false, null);
    }


    @Override
    public void printViaSocket(String data, Printer printer, String encoding, Boolean printInSeparateThread) throws UnableToPrintViaSocketException {
        printViaSocket(data, printer, encoding, printInSeparateThread, null);
    }

    @Override
    public void printViaSocket(String data, Printer printer, String encoding, Boolean printInSeparateThread, Integer wait)
            throws UnableToPrintViaSocketException {

        PrintViaSocket printViaSocket = new PrintViaSocket(data, printer, encoding, wait);

        if (printInSeparateThread) {
            new Thread(printViaSocket).start();
        }
        else {
            printViaSocket.printViaSocket();
        }

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

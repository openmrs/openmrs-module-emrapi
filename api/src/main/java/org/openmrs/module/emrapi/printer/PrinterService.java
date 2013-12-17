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

import org.openmrs.Location;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;

import java.util.List;

/**
 * TODO move this out of the emrapi module
 */
public interface PrinterService extends OpenmrsService {


    /**
     * Fetches a printer by id
     *
     * @param id
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    Printer getPrinterById(Integer id);

    /**
     * Fetches a printer by name
     *
     * @param name
     * @return
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    Printer getPrinterByName(String name);

    /**
     * Fetches all printers of the specified type
     *
     * @param type
     * @return
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    List<Printer> getPrintersByType(Printer.Type type);

    /**
     * Saves a printer
     *
     * @param printer
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_MANAGE_PRINTERS)
    void savePrinter(Printer printer);

    /**
     * Fetches all printers in the system
     *
     * @return all printers in the systesm
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    List<Printer> getAllPrinters();

    /**
     * Sets the specified printer as the default printer of the specified type
     * at the specified location; if printer = null, remove any default printer
     * of that type at that location
     *
     * @param location
     * @param type
     * @param printer
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_MANAGE_PRINTERS)
    void setDefaultPrinter(Location location, Printer.Type type, Printer printer);

    /**
     * Gets the printer of the specified type that is the default printer
     * for that location; returns null if no printer found
     *
     * @param location
     * @param type
     * @return
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    Printer getDefaultPrinter(Location location, Printer.Type type);


    /**
     * Given a printer, returns true/false if that ip address is in use
     * by *another* printer
     *
     * @return
     * @should always return false for localhost ip (127.0.0.1)
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_MANAGE_PRINTERS)
    boolean isIpAddressAllocatedToAnotherPrinter(Printer printer);

    /**
     * Given a printer, returns true/false if that name is in use
     * by *another* printer
     *
     * @return
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_MANAGE_PRINTERS)
    boolean isNameAllocatedToAnotherPrinter(Printer printer);

    /**
     * Prints the string data to the default printer of the specified type
     * at the specific location via socket
     *
     * @param data
     * @param location
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    void printViaSocket(String data, Printer.Type type, Location location, String encoding)
            throws UnableToPrintViaSocketException;


    /**
     * Prints the string data to the default printer of the specified type
     * at the specific location via socket
     *
     * @param data
     * @param location
     * @param printInSeparateThread true/false whether to print a separate thread (will not catch errors)
     * @param wait time in ms to wait after printing before allowing another job to be sent to same printer
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    void printViaSocket(String data, Printer.Type type, Location location, String encoding, Boolean printInSeparateThread, Integer wait)
            throws UnableToPrintViaSocketException;

    /**
     * Prints the string data to the specified printer (without using a separate thread, and with no wait-time)
     *
     * @param data the data to print
     * @param printer the printer to print to
     * @param encoding the encoding to use
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    void printViaSocket(String data, Printer printer, String encoding)
            throws UnableToPrintViaSocketException;

    /**
     * Prints the string data to the specified printer
     *
     * @param data the data to print
     * @param printer the printer to print to
     * @param encoding the encoding to use
     * @param printInSeparateThread true/false whether to print a separate thread (will not catch errors)
     * @param wait time in ms to wait after printing before allowing another job to be sent to same printer
     */
    @Authorized(EmrApiConstants.PRIVILEGE_PRINTERS_ACCESS_PRINTERS)
    void printViaSocket(String data, Printer printer, String encoding, Boolean printInSeparateThread, Integer wait)
            throws UnableToPrintViaSocketException;
}

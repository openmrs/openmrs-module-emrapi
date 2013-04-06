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

package org.openmrs.module.emrapi.printer.db;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.emrapi.db.HibernateSingleClassDAO;
import org.openmrs.module.emrapi.printer.Printer;

import java.util.List;

public class HibernatePrinterDAO extends HibernateSingleClassDAO<Printer> implements PrinterDAO {

    public HibernatePrinterDAO() {
        super(Printer.class);
    }

    @Override
    public Printer getPrinterByName(String name) {
        Criteria criteria = createPrinterCriteria();
        addNameRestriction(criteria, name);

        return (Printer) criteria.uniqueResult();
    }

    @Override
    public List<Printer> getPrintersByType(Printer.Type type) {
        Criteria criteria = createPrinterCriteria();
        addTypeRestriction(criteria, type);

        return (List<Printer>) criteria.list();
    }

    @Override
    public boolean isIpAddressAllocatedToAnotherPrinter(Printer printer) {
        Criteria criteria = createPrinterCriteria();
        addIpAddressRestriction(criteria, printer.getIpAddress());
        addUuidExclusionRestriction(criteria, printer.getUuid());
        Number count = (Number) criteria.setProjection(Projections.rowCount()).uniqueResult();

        return count.intValue() == 0 ? false : true;
    }

    @Override
    public boolean isNameAllocatedToAnotherPrinter(Printer printer) {
        Criteria criteria = createPrinterCriteria();
        addNameRestriction(criteria, printer.getName());
        addUuidExclusionRestriction(criteria, printer.getUuid());
        Number count = (Number) criteria.setProjection(Projections.rowCount()).uniqueResult();

        return count.intValue() == 0 ? false : true;
    }

    private Criteria createPrinterCriteria() {
        return sessionFactory.getCurrentSession().createCriteria(Printer.class);
    }

    private void addIpAddressRestriction(Criteria criteria, String ipAddress) {
        criteria.add(Restrictions.eq("ipAddress", ipAddress));
    }

    private void addNameRestriction(Criteria criteria, String name) {
        criteria.add(Restrictions.eq("name", name));
    }

    private void addUuidExclusionRestriction(Criteria criteria, String uuid) {
        criteria.add(Restrictions.not(Restrictions.eq("uuid", uuid)));
    }

    public void addTypeRestriction(Criteria criteria, Printer.Type type) {
        criteria.add(Restrictions.eq("type", type));
    }
}

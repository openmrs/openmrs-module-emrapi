/**
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
package org.openmrs.module.emrapi.bedmanagement;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.openmrs.module.emrapi.EmrApiConstants;

import java.util.List;

public class HibernateBedManagementDAO implements BedManagementDAO {
    SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AdmissionLocation> getAllLocationsBy(String locationTag) {

        String hql = "select ward as ward, count(layout.bed) as totalBeds , count(assignment.id) as occupiedBeds" +
                " from Location ward, BedLocationMapping layout" +
                " left outer join ward.childLocations physicalSpace " +
                "left outer join layout.bed.bedPatientAssignment as assignment" +
                " where exists (from ward.tags tag where tag.name = :locationTag) " +
                "and layout.location.locationId = physicalSpace.locationId" +
                " group by ward.name";

        Query query = sessionFactory.getCurrentSession().createQuery(hql).setParameter("locationTag", locationTag).setResultTransformer(Transformers.aliasToBean(AdmissionLocation.class));

        return query.list();
    }

    @Override
    public AdmissionLocation getLayoutForWard(String uuid) {
        String hql = "select layout.bed.id as bedId, layout.row as rowNumber, layout.column as columnNumber, assignment.id as bedPatientAssignmentId" +
                " from Location ward, BedLocationMapping layout" +
                " left outer join ward.childLocations physicalSpace " +
                " left outer join layout.bed.bedPatientAssignment as assignment" +
                " where exists (from ward.tags tag where tag.name = :tagName) " +
                " and layout.location.locationId = physicalSpace.locationId" +
                " and ward.uuid = :wardUuid";

        Query query = sessionFactory.getCurrentSession().createQuery(hql)
                .setParameter("tagName", EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION)
                .setParameter("wardUuid", uuid)
                .setResultTransformer(Transformers.aliasToBean(BedLayout.class));

        List<BedLayout> bedLayouts = query.list();

        AdmissionLocation admissionLocation = new AdmissionLocation();
        admissionLocation.setBedLayouts(bedLayouts);
        return admissionLocation;
    }
}

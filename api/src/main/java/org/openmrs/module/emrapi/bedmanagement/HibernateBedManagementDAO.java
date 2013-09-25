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
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.module.emrapi.bedmanagement.domain.AdmissionLocation;

import java.util.List;

public class HibernateBedManagementDAO implements BedManagementDAO {
    SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AdmissionLocation> getAllLocationsBy(String locationTag) {
        Query query = sessionFactory.getCurrentSession().createSQLQuery("select ward.name as name, ward.description as description, count(layout.bed_id) as totalBeds, count(assignment.bed_id) as occupiedBeds from location ward " +
                "inner join location_tag_map tag_map on tag_map.location_id = ward.location_id " +
                "inner join location_tag tag on tag.name='" + locationTag + "' and tag.location_tag_id=tag_map.location_tag_id " +
                "left outer join location physicalSpace on physicalSpace.parent_location = ward.location_id " +
                "left outer join bed_location_mapping layout on layout.location_id = physicalSpace.location_id and layout.bed_id is not null " +
                "left outer join bed_patient_assignment assignment on assignment.bed_id = layout.bed_id " +
                "where ward.parent_location is null group by ward.name, ward.description ")
                .addScalar("name", StandardBasicTypes.STRING)
                .addScalar("description", StandardBasicTypes.STRING)
                .addScalar("totalBeds", StandardBasicTypes.INTEGER)
                .addScalar("occupiedBeds", StandardBasicTypes.INTEGER)
                .setResultTransformer(Transformers.aliasToBean(AdmissionLocation.class));

        return query.list();
    }
}

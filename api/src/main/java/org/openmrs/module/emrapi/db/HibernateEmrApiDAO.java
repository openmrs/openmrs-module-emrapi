package org.openmrs.module.emrapi.db;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;

import java.util.List;

public class HibernateEmrApiDAO implements EmrApiDAO {

    private SessionFactory sessionFactory;


    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Encounter> getEncountersByObsValueText(Concept obsConcept, String valueText, EncounterType encounterType, boolean includeVoided) {

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);

        // we want to return an encounters (but not duplicate encounters)
        criteria.setProjection(Projections.groupProperty("encounter"));

        criteria.add(Restrictions.eq("valueText", valueText));

        if (!includeVoided) {
            criteria.add(Restrictions.eq("voided", false));
        }

        if (obsConcept != null) {
            criteria.add(Restrictions.eq("concept", obsConcept));
        }

        if (encounterType != null) {
            // join on the encounter table
            criteria.createAlias("encounter", "encounter");
            criteria.add(Restrictions.eq("encounter.encounterType", encounterType));
        }

        return criteria.list();
    }

}

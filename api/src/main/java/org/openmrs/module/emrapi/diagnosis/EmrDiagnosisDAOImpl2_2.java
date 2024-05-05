package org.openmrs.module.emrapi.diagnosis;

import org.hibernate.Query;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;

import java.util.List;

/**
 * Hibernate implementation of the EmrDiagnosisDAO
 */
public class EmrDiagnosisDAOImpl2_2 implements EmrDiagnosisDAO {

   // TODO: Fetching diagnosis should be delegated to core Diagnosis service.
   // https://issues.openmrs.org/browse/TRUNK-5999

   private static final Integer PRIMARY_RANK = 1;

   private static final String CONFIRMED_CERTAINTY = "CONFIRMED";

   private DbSessionFactory sessionFactory;

   public void setSessionFactory(DbSessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   /**
    * Gets the diagnosis for a given visit
    *
    * @param visit visit to get the diagnoses from
    * @param primaryOnly whether to fetch primary diagnosis only or all diagnosis regardless of rank
    * @param confirmedOnly whether to fetch only confirmed diagnosis or both confirmed and provisional
    * @return list of diagnoses for a visit
    */
   public List<org.openmrs.Diagnosis> getDiagnoses(Visit visit, boolean primaryOnly, boolean confirmedOnly) {
      String queryString = "from Diagnosis d where d.encounter.visit.visitId = :visitId and d.voided = false";
      if (primaryOnly == true) {
         queryString += " and d.rank = :rankId";
      }
      if (confirmedOnly == true) {
         queryString += " and d.certainty = :certainty";
      }
      queryString += " order by d.dateCreated desc";

      Query query = sessionFactory.getCurrentSession().createQuery(queryString);
      query.setInteger("visitId", visit.getId());
      if (primaryOnly == true) {
         query.setInteger("rankId", PRIMARY_RANK);
      }
      if (confirmedOnly == true) {
         query.setString("certainty", CONFIRMED_CERTAINTY);
      }

      return (List<org.openmrs.Diagnosis>) query.list();
   }
}

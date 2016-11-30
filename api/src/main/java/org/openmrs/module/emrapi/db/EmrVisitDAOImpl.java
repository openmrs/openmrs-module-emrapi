package org.openmrs.module.emrapi.db;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;

public class EmrVisitDAOImpl implements EmrVisitDAO {

   protected final Log log = LogFactory.getLog(getClass());

   protected static String RESOURCE_NOT_FOUND = "The HQL query resource file was not found.";

   protected static String DIAGNOSES_HQL                    = "hql/visit_diagnoses.hql";
   protected static String PRIMARY_DIAGNOSES_HQL            = "hql/visit_primary_diagnoses.hql";
   protected static String CONFIRMED_DIAGNOSES_HQL          = "hql/visit_confirmed_diagnoses.hql";
   protected static String CONFIRMED_PRIMARY_DIAGNOSES_HQL  = "hql/visit_confirmed_primary_diagnoses.hql";

   private DbSessionFactory sessionFactory;

   public void setSessionFactory(DbSessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Obs> getDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata) {
      String queryString = "";
      try {
         queryString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(DIAGNOSES_HQL));
      } catch (IOException e) {
         log.error(RESOURCE_NOT_FOUND, e);
         return Collections.emptyList();
      }
      Query query = sessionFactory.getCurrentSession().createQuery(queryString);
      query.setInteger("visitId", visit.getId());
      query.setInteger("diagnosisOrderConceptId", diagnosisMetadata.getDiagnosisOrderConcept().getId());

      return (List<Obs>) query.list();
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Obs> getPrimaryDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata) {
      String queryString = "";
      try {
         queryString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(PRIMARY_DIAGNOSES_HQL));
      } catch (IOException e) {
         log.error(RESOURCE_NOT_FOUND, e);
         return Collections.emptyList();
      }
      Query query = sessionFactory.getCurrentSession().createQuery(queryString);
      query.setInteger("visitId", visit.getId());
      query.setInteger("diagnosisOrderConceptId", diagnosisMetadata.getDiagnosisOrderConcept().getId());
      query.setInteger("primaryOrderConceptId", diagnosisMetadata.getConceptFor(Diagnosis.Order.PRIMARY).getId());
      return (List<Obs>) query.list();
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Obs> getConfirmedDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata) {
      String queryString = "";
      try {
         queryString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(CONFIRMED_DIAGNOSES_HQL));
      } catch (IOException e) {
         log.error(RESOURCE_NOT_FOUND, e);
         return Collections.emptyList();
      }
      Query query = sessionFactory.getCurrentSession().createQuery(queryString);
      query.setInteger("visitId", visit.getId());
      query.setInteger("diagnosisCertaintyConceptId", diagnosisMetadata.getDiagnosisCertaintyConcept().getId());
      query.setInteger("confirmedCertaintyConceptId", diagnosisMetadata.getConceptFor(Diagnosis.Certainty.CONFIRMED).getId());
      return (List<Obs>) query.list();
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Obs> getConfirmedPrimaryDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata) {
      String queryString = "";
      try {
         queryString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(CONFIRMED_PRIMARY_DIAGNOSES_HQL));
      } catch (IOException e) {
         log.error(RESOURCE_NOT_FOUND, e);
         return Collections.emptyList();
      }
      Query query = sessionFactory.getCurrentSession().createQuery(queryString);
      query.setInteger("visitId", visit.getId());
      query.setInteger("diagnosisOrderConceptId", diagnosisMetadata.getDiagnosisOrderConcept().getId());
      query.setInteger("primaryOrderConceptId", diagnosisMetadata.getConceptFor(Diagnosis.Order.PRIMARY).getId());
      query.setInteger("diagnosisCertaintyConceptId", diagnosisMetadata.getDiagnosisCertaintyConcept().getId());
      query.setInteger("confirmedCertaintyConceptId", diagnosisMetadata.getConceptFor(Diagnosis.Certainty.CONFIRMED).getId());
      return (List<Obs>) query.list();
   }
}
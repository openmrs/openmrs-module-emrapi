package org.openmrs.module.emrapi.db;

import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.Diagnosis;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesAndNotes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmrApiDAOImpl implements EmrApiDAO {
   
   protected final Log log = LogFactory.getLog(getClass());
   
   @Setter
   private DbSessionFactory sessionFactory;
   
   @Setter
   private EmrApiProperties emrApiProperties;
   
   @Override
   @SuppressWarnings("unchecked")
   public <T> List<T> executeHql(String queryString, Map<String, Object> parameters, Class<T> clazz) {
      Query query = sessionFactory.getCurrentSession().createQuery(queryString);
      for (String parameter : parameters.keySet()) {
         Object value = parameters.get(parameter);
         if (value instanceof Collection) {
            query.setParameterList(parameter, (Collection) value);
         } else {
            query.setParameter(parameter, value);
         }
      }
      return query.list();
   }
   
   @Override
   public <T> List<T> executeHqlFromResource(String resource, Map<String, Object> parameters, Class<T> clazz) {
      String hql = null;
      try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
         if (is != null) {
            hql = IOUtils.toString(is, StandardCharsets.UTF_8);
         }
      }
      catch (IOException e) {
         throw new RuntimeException("Error loading " + resource, e);
      }
      if (hql == null) {
         throw new RuntimeException("No resource found for " + resource);
      }
      return executeHql(hql, parameters, clazz);
   }
   
   public List<VisitWithDiagnosesAndNotes> getVisitsWithNotesAndDiagnosesByPatient(Patient patient, int startIndex,
           int limit) {
      
      String visitNoteEncounterTypeUuid = emrApiProperties.getVisitNoteEncounterType().getUuid();
      
      // get visits for the patient, paginated
      String hqlVisits = "SELECT v FROM Visit v " +
              "WHERE v.patient.id = :patientId " +
              "AND v.voided = false " +
              "ORDER BY v.startDatetime DESC";
      
      Query visitsQuery = sessionFactory.getCurrentSession().createQuery(hqlVisits);
      visitsQuery.setParameter("patientId", patient.getId());
      visitsQuery.setFirstResult(startIndex);
      visitsQuery.setMaxResults(limit);
      
      List<Visit> visits = visitsQuery.list();
      
      // extract visit IDs, so that we can query notes and diagnoses for these visits
      List<Integer> visitIds = visits.stream()
              .map(Visit::getId)
              .collect(Collectors.toList());
      
      
      String hqlNotesObs = "SELECT obs FROM Obs obs " +
              "JOIN obs.encounter e " +
              "JOIN e.encounterType et " +
              "WHERE et.uuid = :encounterTypeUuid " +
              "AND e.visit.id IN :visitIds " +
              "AND obs.voided = false";
      
      Query notesQuery = sessionFactory.getCurrentSession().createQuery(hqlNotesObs);
      notesQuery.setParameterList("visitIds", visitIds);
      notesQuery.setParameter("encounterTypeUuid", visitNoteEncounterTypeUuid);
      List<Obs> notesObs = notesQuery.list();
      
      // get diagnoses for the visits
      String hqlDiagnosis = "SELECT DISTINCT diag FROM Diagnosis diag " +
              "JOIN diag.encounter e " +
              "WHERE e.visit.id IN :visitIds " +
              "AND diag.voided = false " +
              "ORDER BY diag.rank";
      Query diagnosesQuery = sessionFactory.getCurrentSession().createQuery(hqlDiagnosis);
      diagnosesQuery.setParameterList("visitIds", visitIds);
      List<Diagnosis> diagnoses = diagnosesQuery.list();
      
      // group notes and diagnoses by visit ID
      Map<Integer, ArrayList<Obs>> visitIdToNotesMap = new HashMap<>();
      Map<Integer, ArrayList<Diagnosis>> visitIdToDiagnosesMap = new HashMap<>();
      
      for (Obs obs : notesObs) {
         Integer visitId = obs.getEncounter().getVisit().getId();
         visitIdToNotesMap
                 .computeIfAbsent(visitId, v -> new ArrayList<>())
                 .add(obs);
      }
      for (Diagnosis diagnosis : diagnoses) {
         Integer visitId = diagnosis.getEncounter().getVisit().getId();
         visitIdToDiagnosesMap
                 .computeIfAbsent(visitId, v -> new ArrayList<>())
                 .add(diagnosis);
      }
      
      // create VisitWithDiagnosesAndNotes objects
      List<VisitWithDiagnosesAndNotes> visitWithDiagnosisAndNotes = visits.stream()
              .map(visit -> {
                 ArrayList<Obs> notes = visitIdToNotesMap.computeIfAbsent(visit.getId(), ArrayList::new);
                 ArrayList<Diagnosis> visitDiagnoses = visitIdToDiagnosesMap.computeIfAbsent(visit.getId(), ArrayList::new);
                 return new VisitWithDiagnosesAndNotes(visit, visitDiagnoses, notes);
              })
              .collect(Collectors.toList());
      
      return visitWithDiagnosisAndNotes;
   }
   
}

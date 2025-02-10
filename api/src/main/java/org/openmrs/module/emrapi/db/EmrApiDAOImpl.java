package org.openmrs.module.emrapi.db;

import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesAndNotes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
         }
         else {
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
   
   public List<VisitWithDiagnosesAndNotes> getVisitsWithNotesAndDiagnosesByPatient(Patient patient, int startIndex, int limit) {
      
      String visitNoteEncounterTypeUuid = emrApiProperties.getVisitNoteEncounterType().getUuid();
      
      String hqlVisit = "SELECT DISTINCT v FROM Visit v " +
              "LEFT JOIN FETCH v.encounters enc " +
              "LEFT JOIN enc.encounterType et " +
              "WHERE v.patient.id = :patientId " +
              "AND (et.uuid = :encounterTypeUuid OR enc IS NULL) " +
              "ORDER BY v.startDatetime DESC";
      
      Query visitQuery = sessionFactory.getCurrentSession().createQuery(hqlVisit);
      
      visitQuery.setParameter("patientId", patient.getId());
      visitQuery.setParameter("encounterTypeUuid", visitNoteEncounterTypeUuid);
      visitQuery.setFirstResult(startIndex);
      visitQuery.setMaxResults(limit);
      
      List<Visit> visits = visitQuery.list();
      
      String hqlDiagnosis = "SELECT DISTINCT diag FROM Diagnosis diag " +
              "JOIN diag.encounter e " +
              "WHERE e.visit.id IN :visitIds";
      
      List<Integer> visitIds = visits.stream()
              .map(Visit::getId)
              .collect(Collectors.toList());
      
      List<Diagnosis> diagnoses = sessionFactory.getCurrentSession()
              .createQuery(hqlDiagnosis)
              .setParameterList("visitIds", visitIds)
              .list();
      
      Map<Visit, Set<Diagnosis>> visitToDiagnosesMap = new HashMap<>();
      for (Diagnosis diagnosis : diagnoses) {
         Visit visit = diagnosis.getEncounter().getVisit();
         visitToDiagnosesMap
                 .computeIfAbsent(visit, v -> new HashSet<>())
                 .add(diagnosis);
      }
      
      List<VisitWithDiagnosesAndNotes> visitWithDiagnosisAndNotes = visits.stream()
              .map(visit -> {
                 Set<Encounter> visitNotes = visit.getEncounters();
                 // clear out the encounters so that they don't get serialized
                 visit.setEncounters(new HashSet<>());
                 return new VisitWithDiagnosesAndNotes(visit, visitToDiagnosesMap.getOrDefault(visit, new HashSet<>()), visitNotes);
              })
              .collect(Collectors.toList());
      
      return visitWithDiagnosisAndNotes;
   }
   
}

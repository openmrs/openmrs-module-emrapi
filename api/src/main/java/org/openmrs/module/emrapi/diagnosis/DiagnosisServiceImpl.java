package org.openmrs.module.emrapi.diagnosis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @deprecated as of 1.25.0, replaced by {@link DiagnosisServiceImpl} in the openmrs core platform 2.2.0
 */
@Deprecated
public class DiagnosisServiceImpl extends BaseOpenmrsService implements DiagnosisService {

    private static final Log log = LogFactory.getLog(DiagnosisService.class);

	private EmrApiProperties emrApiProperties;

	private ObsService obsService;

    private EncounterService encounterService;

	public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
		this.emrApiProperties = emrApiProperties;
	}

	public void setObsService(ObsService obsService) {
		this.obsService = obsService;
	}

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @Override
    public List<Obs> codeNonCodedDiagnosis(Obs nonCodedObs, List<Diagnosis> diagnoses) {

        List<Obs> newDiagnoses = null;
        if ( (nonCodedObs != null) && (diagnoses != null && diagnoses.size() > 0) ){
            newDiagnoses = new ArrayList<Obs>();

            Obs obsGroup = nonCodedObs.getObsGroup();
            if ( obsGroup != null ){
                Set<Obs> groupMembers = obsGroup.getGroupMembers();
                if( (groupMembers!=null) && (groupMembers.size() > 0) ){
                    for (Obs groupMember : groupMembers) {
                        obsService.voidObs(groupMember, "code a diagnosis");
                    }
                }
                obsGroup = obsService.voidObs(obsGroup, "code a diagnosis");
            }

            Encounter encounter = nonCodedObs.getEncounter();
            List<Diagnosis> primaryDiagnoses = getPrimaryDiagnoses(encounter);
            boolean havePrimary =false;
            if (primaryDiagnoses !=null && primaryDiagnoses.size() > 0 ){
                // the encounter already has a primary diagnosis
                havePrimary = true;
            }
            DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
            for(Diagnosis diagnosis : diagnoses){
                if (hasDiagnosis(encounter, diagnosis)){
                    // if this encounter already has this diagnosis skip it
                    continue;
                }
                if (havePrimary) {
                    diagnosis.setOrder(Diagnosis.Order.SECONDARY);
                }
                Obs obs = diagnosisMetadata.buildDiagnosisObsGroup(diagnosis);
                if (obs != null) {
                    newDiagnoses.add(obs);
                    encounter.addObs(obs);
                }

            }
            encounterService.saveEncounter(encounter);
        }
        return newDiagnoses;
    }

    @Override
    public List<Diagnosis> getPrimaryDiagnoses(Encounter encounter) {
        List<Diagnosis> diagnoses = null;
        if (encounter != null && !encounter.isVoided()){
            DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
            diagnoses = new ArrayList<Diagnosis>();
            for (Obs obs : encounter.getObsAtTopLevel(false) ){
                if (diagnosisMetadata.isDiagnosis(obs)) {
                    try {
                        Diagnosis diagnosis = diagnosisMetadata.toDiagnosis(obs);
                        if (Diagnosis.Order.PRIMARY == diagnosis.getOrder()) {
                            diagnoses.add(diagnosis);
                        }
                    } catch (Exception ex){
                        log.warn("malformed diagnosis obs group with obsId " + obs.getObsId(), ex);
                    }
                }
            }
        }
        return diagnoses;

    }

    @Override
    public boolean hasDiagnosis(Encounter encounter, Diagnosis diagnosis) {
        boolean hasDiagnosis = false;
        if (encounter != null && !encounter.isVoided()){
            DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
            for (Obs obs : encounter.getObsAtTopLevel(false) ){
                if (diagnosisMetadata.isDiagnosis(obs)) {
                    try {
                        Diagnosis existing = diagnosisMetadata.toDiagnosis(obs);
                        CodedOrFreeTextAnswer answer = existing.getDiagnosis();
                        if (answer != null && answer.equals(diagnosis.getDiagnosis())){
                            hasDiagnosis = true;
                            break;
                        }
                    } catch (Exception ex){
                        log.warn("malformed diagnosis obs group with obsId " + obs.getObsId(), ex);
                    }
                }
            }
        }
        return hasDiagnosis;

    }

    @Override
	public List<Diagnosis> getDiagnoses(Patient patient, Date fromDate) {
		List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();

		DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();

		List<Obs> observations = obsService.getObservations(Arrays.asList((Person) patient), null, Arrays.asList(diagnosisMetadata.getDiagnosisSetConcept()),
				null, null, null, Arrays.asList("obsDatetime"),
				null, null, fromDate, null, false);

		for (Obs obs : observations) {
			Diagnosis diagnosis;
            try {
                diagnosis = diagnosisMetadata.toDiagnosis(obs);
            }
            catch (Exception ex) {
                log.warn("Error trying to interpret " + obs + " as a diagnosis");
                if (log.isDebugEnabled()) {
                    log.debug("Detailed error", ex);
                }
                continue;
            }

			Collection<Concept> nonDiagnosisConcepts = emrApiProperties.getSuppressedDiagnosisConcepts();
			Collection<Concept> nonDiagnosisConceptSets = emrApiProperties.getNonDiagnosisConceptSets();

			Set<Concept> filter = new HashSet<Concept>();
			filter.addAll(nonDiagnosisConcepts);
			for (Concept conceptSet : nonDiagnosisConceptSets) {
				filter.addAll(conceptSet.getSetMembers());
			}

			if (!filter.contains(diagnosis.getDiagnosis().getCodedAnswer())) {
				diagnoses.add(diagnosis);
			}
		}

		return diagnoses;
	}

	@Override
	public List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate) {
		List<Diagnosis> diagnoses = getDiagnoses(patient, fromDate);

		Set<CodedOrFreeTextAnswer> answers = new HashSet<CodedOrFreeTextAnswer>();

		Iterator<Diagnosis> it = diagnoses.iterator();
		while(it.hasNext()) {
			Diagnosis diagnosis = it.next();

			if (!answers.add(diagnosis.getDiagnosis())) {
				 it.remove();
			}
		}

		return diagnoses;
	}
}

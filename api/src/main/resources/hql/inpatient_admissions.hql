select
    visit,
    patient,
    encounter
from
    Encounter as encounter
inner join encounter.visit as visit
inner join encounter.patient as patient
where encounter.voided = false
    and visit.voided = false
    and patient.voided = false
    and (:visitLocation is null or visit.location = :visitLocation)
    and visit.stopDatetime is null
    and encounter.encounterType in (:admissionEncounterType, :transferEncounterType, :dischargeEncounterType)
    and (:limitByPatient is false or patient.patientId in (:patientIds))
    and (:limitByVisit is false or visit.visitId in (:visitIds))
order by visit.visitId, encounter.encounterDatetime, encounter.encounterId

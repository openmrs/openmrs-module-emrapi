select
    distinct visit
from
    Obs as dispo
inner join dispo.encounter as dispoEncounter
inner join dispoEncounter.visit as visit
inner join dispo.person as person
where
    dispo.voided = false
    and dispoEncounter.voided = false
    and visit.voided = false
    and dispo.concept = :dispositionConcept
    and dispo.valueCoded in :admissionDispositions
    and (:visitLocation is null or visit.location = :visitLocation)
    and (:patientIds is null or person.personId in :patientIds)
    and (:visitIds is null or visit.visitId in :visitIds)
    and person.dead = false
    and visit.stopDatetime is null
    and (
        select count(*)
        from Encounter as admission
        where admission.visit = visit
          and admission.voided = false
          and admission.encounterType = :admissionEncounterType
    ) = 0
    and (
        select count(*)
        from Obs as admitDecision
            inner join admitDecision.encounter as encounterInVisit
            where encounterInVisit.visit = visit
            and encounterInVisit.voided = false
            and admitDecision.voided = false
            and admitDecision.concept = :admissionDecisionConcept
            and admitDecision.valueCoded = :denyAdmissionConcept
            and encounterInVisit.encounterDatetime > dispoEncounter.encounterDatetime
    ) = 0

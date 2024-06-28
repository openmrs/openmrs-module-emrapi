select
    dispo
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
    and dispo.valueCoded in :dispositionValues
    and (:visitLocation is null or visit.location = :visitLocation)
    and person.dead = false
    and visit.stopDatetime is null
    and (
        select count(*)
        from Encounter as adtEncounter
        where adtEncounter.visit = visit
          and adtEncounter.voided = false
          and adtEncounter.encounterType in (:adtEncounterTypes)
    ) = 0
    and (
        select count(*)
        from Obs as adtDecision
            inner join adtDecision.encounter as encounterInVisit
            where encounterInVisit.visit = visit
            and encounterInVisit.voided = false
            and adtDecision.voided = false
            and adtDecision.concept = :adtDecisionConcept
            and adtDecision.valueCoded = :denyConcept
            and encounterInVisit.encounterDatetime > dispoEncounter.encounterDatetime
    ) = 0
    and (
        :dispositionLocationIds is null or (
            select count(*)
            from Obs as locationObs
            where locationObs.obsGroup = dispo.obsGroup
            and locationObs.valueText in (:dispositionLocationIds)
        ) > 0
    )

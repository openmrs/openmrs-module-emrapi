select
  o.obsGroup
from
  Obs o
where
  o.voided = 'false'
  and (o.encounter.visit = :visitId)
  and o.concept.conceptId = :diagnosisOrderConceptId
  and o.valueCoded.conceptId = :primaryOrderConceptId
group by o.encounter, o.obsGroup
order by o.encounter.encounterDatetime desc, o.obsGroup.obsDatetime desc
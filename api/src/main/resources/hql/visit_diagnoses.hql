select
  o.obsGroup
from
  Obs o
where
  o.voided = 'false'
  and (o.encounter.visit = :visit)
  and o.concept = :diagnosisOrderConcept
group by o.encounter, o.obsGroup
order by o.encounter.encounterDatetime desc, o.obsGroup.obsDatetime desc
select o1 from Obs o
inner join o.obsGroup o1	
where 
  o.voided = 'false'
  and (o.encounter.visit = :visitId)
  and o.concept.conceptId = :diagnosisOrderConceptId
  and o.valueCoded.conceptId = :primaryOrderConceptId
group by o.encounter, o1.obsId, o.encounter.encounterDatetime
order by o.encounter.encounterDatetime desc, o.obsGroup.obsDatetime desc

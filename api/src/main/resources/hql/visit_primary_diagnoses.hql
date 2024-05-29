select o1 from Obs o
inner join o.obsGroup o1	
where 
  o.voided = 'false'
  and (o.encounter.visit = :visit)
  and o.concept = :diagnosisOrderConcept
  and o.valueCoded = :primaryOrderConcept
group by o.encounter, o1.obsId, o.encounter.encounterDatetime
order by o.encounter.encounterDatetime desc, o.obsGroup.obsDatetime desc

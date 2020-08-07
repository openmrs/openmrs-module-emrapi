select
  distinct patientId
from
  Patient p
where
    p.voided = 'false'
    and p.patientId in(select distinct personId from Obs o
    where
        o.concept.conceptId = :diagnosisSetConceptId
    )

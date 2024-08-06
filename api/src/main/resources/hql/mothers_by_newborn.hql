select
    mother,
    baby,
    motherVisit
from
    Relationship as motherChildRelationship, Visit as motherVisit, Visit as babyVisit
    inner join motherChildRelationship.personA as mother
    inner join motherChildRelationship.personB as baby
where
  baby in (:babies)
  and motherChildRelationship.relationshipType = :motherChildRelationshipType
  and motherVisit.patient = mother and motherVisit.stopDatetime is null
  and babyVisit.patient = baby and babyVisit.stopDatetime is null
  and babyVisit.location = motherVisit.location
  and mother.voided = false and baby.voided = false and motherChildRelationship.voided = false and motherVisit.voided = false and babyVisit.voided = false




select
    baby,
    babyVisit
from
    Person as mother,
    Person as baby,
    Relationship as motherChildRelationship,
    Visit as motherVisit,
    Visit as babyVisit
where
    mother = :mother
    and motherChildRelationship.personA = mother
    and motherChildRelationship.personB = baby
    and motherChildRelationship.relationshipType = :motherChildRelationshipType
    and motherVisit.patient = mother and motherVisit.stopDatetime is null
    and babyVisit.patient = baby and babyVisit.stopDatetime is null
    and year(baby.birthdate) >= year(motherVisit.startDatetime)
    and month(baby.birthdate) >= month(motherVisit.startDatetime)
    and day(baby.birthdate) >= day(motherVisit.startDatetime)
    and (:visitLocation is null or (motherVisit.location = :visitLocation and babyVisit.location = :visitLocation))
    and mother.voided = false and baby.voided = false and motherChildRelationship.voided = false and motherVisit.voided = false and babyVisit.voided = false




select
    child,
    mother
from
    Relationship as motherChildRelationship
    inner join motherChildRelationship.personA as mother
    inner join motherChildRelationship.personB as child
where
    mother.uuid in (:motherUuids)
    and motherChildRelationship.relationshipType = :motherChildRelationshipType
    and (:motherHasActiveVisit = false or (select count(motherVisit) from Visit as motherVisit where motherVisit.patient = mother and motherVisit.stopDatetime is null and motherVisit.voided = false) > 0)
    and (:childHasActiveVisit = false or (select count(childVisit) from Visit as childVisit where childVisit.patient = child and childVisit.stopDatetime is null and childVisit.voided = false) > 0)
    and (:childBornDuringMothersActiveVisit = false or (select count(motherVisit) from Visit as motherVisit where motherVisit.patient = mother and motherVisit.stopDatetime is null and motherVisit.voided = false
           and year(child.birthdate) >= year(motherVisit.startDatetime)
           and month(child.birthdate) >= month(motherVisit.startDatetime)
           and day(child.birthdate) >= day(motherVisit.startDatetime)) > 0)
    and mother.voided = false and child.voided = false and motherChildRelationship.voided = false




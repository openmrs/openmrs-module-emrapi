select
    u
from
    User as u
left join u.person as p
left join p.names as pn
where (p.voided is null or p.voided = false)
  and (pn.voided is null or pn.voided = false)
  and (
      :search is null or
      u.username like :search or
      u.systemId like :search or
      u.email like :search or
      pn.prefix like :search or
      pn.givenName like :search or
      pn.middleName like :search or
      pn.familyNamePrefix like :search or
      pn.familyName like :search or
      pn.familyName2 like :search or
      pn.familyNameSuffix like :search or
      pn.degree like :search
  )

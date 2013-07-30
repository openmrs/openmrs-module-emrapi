/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.emrapi.concept;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.ConceptWord;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 */
public class HibernateEmrConceptDAO implements EmrConceptDAO {

    SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Concept> getConceptsMappedTo(Collection<ConceptMapType> mapTypes, ConceptReferenceTerm term) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(Concept.class);
        crit.createCriteria("conceptMappings")
                .add(Restrictions.in("conceptMapType", mapTypes))
                .add(Restrictions.eq("conceptReferenceTerm", term));
        return crit.list();
    }

    @Override
    @Transactional(readOnly=true)
    public List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes, Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit) {
        List<String> uniqueWords = ConceptWord.getUniqueWords(query, locale);
        if (uniqueWords.size() == 0) {
            return Collections.emptyList();
        }

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();

        // find matches based on name
        {
            Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptName.class, "cn");
            criteria.add(Restrictions.eq("voided", false));
            criteria.add(Restrictions.eq("locale", locale));
            criteria.setMaxResults(limit);

            Criteria conceptCriteria = criteria.createCriteria("concept");
            conceptCriteria.add(Restrictions.eq("retired", false));
            if (classes != null) {
                conceptCriteria.add(Restrictions.in("conceptClass", classes));
            }
            if (inSets != null) {
                DetachedCriteria allowedSetMembers = DetachedCriteria.forClass(ConceptSet.class);
                allowedSetMembers.add(Restrictions.in("conceptSet", inSets));
                allowedSetMembers.setProjection(Projections.property("concept"));
                criteria.add(Subqueries.propertyIn("concept", allowedSetMembers));
            }

            for (String word : uniqueWords) {
                criteria.add(Restrictions.ilike("name", word, MatchMode.ANYWHERE));
            }

            Set<Concept> conceptsMatchedByPreferredName = new HashSet<Concept>();
            for (ConceptName matchedName : (List<ConceptName>) criteria.list()) {
                results.add(new ConceptSearchResult(null, matchedName.getConcept(), matchedName, calculateMatchScore(query, uniqueWords, matchedName)));
                if (matchedName.isLocalePreferred()) {
                    conceptsMatchedByPreferredName.add(matchedName.getConcept());
                }
            }

            // don't display synonym matches if the preferred name matches too
            for (Iterator<ConceptSearchResult> i = results.iterator(); i.hasNext(); ) {
                ConceptSearchResult candidate = i.next();
                if (!candidate.getConceptName().isLocalePreferred() && conceptsMatchedByPreferredName.contains(candidate.getConcept())) {
                    i.remove();
                }
            }
        }

        // find matches based on mapping
        if (sources != null) {
            Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptMap.class);
            criteria.setMaxResults(limit);

            Criteria conceptCriteria = criteria.createCriteria("concept");
            conceptCriteria.add(Restrictions.eq("retired", false));
            if (classes != null) {
                conceptCriteria.add(Restrictions.in("conceptClass", classes));
            }

            Criteria mappedTerm = criteria.createCriteria("conceptReferenceTerm");
            mappedTerm.add(Restrictions.eq("retired", false));
            mappedTerm.add(Restrictions.in("conceptSource", sources));
            mappedTerm.add(Restrictions.ilike("code", query, MatchMode.EXACT));

            for (ConceptMap mapping : (List<ConceptMap>) criteria.list()) {
                results.add(new ConceptSearchResult(null, mapping.getConcept(), null, calculateMatchScore(query, mapping)));
            }
        }

        Collections.sort(results, new Comparator<ConceptSearchResult>() {
            @Override
            public int compare(ConceptSearchResult left, ConceptSearchResult right) {
                return right.getTransientWeight().compareTo(left.getTransientWeight());
            }
        });

        if (results.size() > limit) {
            results = results.subList(0, limit);
        }
        return results;
    }

    private Double calculateMatchScore(String query, ConceptMap matchedMapping) {
        // eventually consider weighting this by map type (e.g. same-as > narrower-than > others)
        return 10000d;
    }

    private Double calculateMatchScore(String query, List<String> uniqueWords, ConceptName matchedName) {
        double score = 0d;
        if (query.equalsIgnoreCase(matchedName.getName())) {
            score += 1000d;
        }
        if (matchedName.isLocalePreferred()) {
            score += 500d;
        }
        score -= matchedName.getName().length();
        return score;
    }
}

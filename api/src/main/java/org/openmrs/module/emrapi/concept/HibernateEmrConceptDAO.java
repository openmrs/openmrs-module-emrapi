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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.openmrs.api.db.hibernate.DbSessionFactory;  
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
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
public class HibernateEmrConceptDAO implements EmrConceptDAO {

	DbSessionFactory sessionFactory;

    public void setSessionFactory(DbSessionFactory sessionFactory) {
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
        List<String> uniqueWords = getUniqueWords(query, locale);
        if (uniqueWords.size() == 0) {
            return Collections.emptyList();
        }

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();

        // find matches based on name
        {
            Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ConceptName.class, "cn");
            criteria.add(Restrictions.eq("voided", false));
            if (StringUtils.isNotBlank(locale.getCountry()) || StringUtils.isNotBlank(locale.getVariant())) {
                Locale[] locales = new Locale[] { locale, new Locale(locale.getLanguage()) };
                criteria.add(Restrictions.in("locale", locales));
            } else {
                criteria.add(Restrictions.eq("locale", locale));
            }
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
    
    /**
     * Copied over from OpenMRS 1.9.8 to provide backwards compatibility.
     * 
     * It's no longer available in 1.11.
     * 
     * @param phrase
     * @param locale
     * @return
     */
    public static List<String> getUniqueWords(String phrase, Locale locale) {
		String[] parts = splitPhrase(phrase);
		List<String> uniqueParts = new Vector<String>();
		
		if (parts != null) {
			List<String> conceptStopWords = Context.getConceptService().getConceptStopWords(locale);
			for (String part : parts) {
				if (!StringUtils.isBlank(part)) {
					String upper = part.trim().toUpperCase();
					if (!conceptStopWords.contains(upper) && !uniqueParts.contains(upper))
						uniqueParts.add(upper);
				}
			}
		}
		
		return uniqueParts;
	}
    
    /**
     * Copied over from OpenMRS 1.9.8 to provide backwards compatibility.
     * 
     * It's no longer available in 1.11.
     * 
     * @param phrase
     * @return
     */
    public static String[] splitPhrase(String phrase) {
		if (StringUtils.isBlank(phrase)) {
			return null;
		}
		if (phrase.length() > 2) {
			phrase = phrase.replaceAll(OpenmrsConstants.REGEX_LARGE, " ");
		} else {
			phrase = phrase.replaceAll(OpenmrsConstants.REGEX_SMALL, " ");
		}
		
		return phrase.trim().replace('\n', ' ').split(" ");
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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
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
		if (mapTypes == null || mapTypes.isEmpty()) {
			return new ArrayList<Concept>();
		}
		Query<Concept> query = sessionFactory.getHibernateSessionFactory().getCurrentSession()
		        .createQuery("select c from Concept c join c.conceptMappings m where m.conceptMapType in (:mapTypes)"
		                + " and m.conceptReferenceTerm = :term");
		query.setParameterList("mapTypes", mapTypes);
		query.setParameter("term", term);
		return query.list();
	}
	
	/**
	 * @see org.openmrs.module.emrapi.concept.EmrConceptDAO#conceptSearch(String,Locale,Collection,Collection,Collection,Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes,
	        Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit) {
		List<String> uniqueWords = getUniqueWords(query, locale);
		if (uniqueWords.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();
		
		// find matches based on name
		{
			StringBuilder hql = new StringBuilder("select cn from ConceptName cn join cn.concept cpt");
			Map<String, Object> params = new HashMap<String, Object>();
			
			boolean joinMappings = !CollectionUtils.isEmpty(sources) && CollectionUtils.isEmpty(inSets);
			if (joinMappings) {
				hql.append(" join cpt.conceptMappings mapping join mapping.conceptReferenceTerm refTerm");
			}
			
			hql.append(" where cn.voided = false");
			if (StringUtils.isNotBlank(locale.getCountry()) || StringUtils.isNotBlank(locale.getVariant())) {
				Locale[] locales = new Locale[] { locale, new Locale(locale.getLanguage()) };
				hql.append(" and cn.locale in (:locales)");
				params.put("locales", Arrays.asList(locales));
			} else {
				hql.append(" and cn.locale = :locale");
				params.put("locale", locale);
			}
			
			hql.append(" and cpt.retired = false");
			
			if (inSets != null) {
				if (inSets.isEmpty()) {
					hql.append(" and 1 = 0");
				} else {
					hql.append(" and cn.concept in (select cs.concept from ConceptSet cs where cs.conceptSet in (:inSets))");
					params.put("inSets", inSets);
				}
			}
			
			if (!CollectionUtils.isEmpty(classes) && CollectionUtils.isEmpty(inSets)) {
				hql.append(" and cpt.conceptClass in (:classes)");
				params.put("classes", classes);
			}
			
			if (joinMappings) {
				hql.append(" and refTerm.conceptSource in (:sources)");
				hql.append(" and mapping.concept = cpt");
				params.put("sources", sources);
			}
			
			int i = 0;
			for (String word : uniqueWords) {
				hql.append(" and lower(cn.name) like :word").append(i);
				params.put("word" + i, "%" + word.toLowerCase() + "%");
				i++;
			}
			
			Query<ConceptName> nameQuery = sessionFactory.getHibernateSessionFactory().getCurrentSession()
			        .createQuery(hql.toString());
			setParameters(nameQuery, params);
			nameQuery.setMaxResults(limit);
			
			Set<Concept> conceptsMatchedByPreferredName = new HashSet<Concept>();
			for (ConceptName matchedName : nameQuery.list()) {
				results.add(new ConceptSearchResult(null, matchedName.getConcept(), matchedName,
				        calculateMatchScore(query, uniqueWords, matchedName)));
				if (matchedName.isLocalePreferred()) {
					conceptsMatchedByPreferredName.add(matchedName.getConcept());
				}
			}
			
			// don't display synonym matches if the preferred name matches too
			for (Iterator<ConceptSearchResult> it = results.iterator(); it.hasNext();) {
				ConceptSearchResult candidate = it.next();
				if (!candidate.getConceptName().isLocalePreferred()
				        && conceptsMatchedByPreferredName.contains(candidate.getConcept())) {
					it.remove();
				}
			}
		}
		
		// find matches based on mapping
		if (!CollectionUtils.isEmpty(sources)) {
			StringBuilder hql = new StringBuilder(
			        "select m from ConceptMap m join m.concept c join m.conceptReferenceTerm term where c.retired = false");
			Map<String, Object> params = new HashMap<String, Object>();
			if (classes != null) {
				if (classes.isEmpty()) {
					hql.append(" and 1 = 0");
				} else {
					hql.append(" and c.conceptClass in (:classes)");
					params.put("classes", classes);
				}
			}
			hql.append(" and term.retired = false and term.conceptSource in (:sources) and lower(term.code) like :code");
			params.put("sources", sources);
			params.put("code", query.toLowerCase());
			
			Query<ConceptMap> mappingQuery = sessionFactory.getHibernateSessionFactory().getCurrentSession()
			        .createQuery(hql.toString());
			setParameters(mappingQuery, params);
			mappingQuery.setMaxResults(limit);
			
			for (ConceptMap mapping : mappingQuery.list()) {
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
	 * Copied over from OpenMRS 1.9.8 to provide backwards compatibility. It's no longer available in
	 * 1.11.
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
	 * Copied over from OpenMRS 1.9.8 to provide backwards compatibility. It's no longer available in
	 * 1.11.
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
	
	private void setParameters(Query<?> query, Map<String, Object> params) {
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (param.getValue() instanceof Collection) {
				query.setParameterList(param.getKey(), (Collection<?>) param.getValue());
			} else {
				query.setParameter(param.getKey(), param.getValue());
			}
		}
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

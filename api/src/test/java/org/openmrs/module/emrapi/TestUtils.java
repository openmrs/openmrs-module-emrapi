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
package org.openmrs.module.emrapi;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.mockito.ArgumentMatcher;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.util.OpenmrsUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Various utils to help with testing
 */
public class TestUtils {
	
	/**
	 * To test things like: assertContainsElementWithProperty(listOfPatients, "patientId", 2)
	 * 
	 * @param collection
	 * @param property
	 * @param value
	 */
	public static void assertContainsElementWithProperty(Collection<?> collection, String property, Object value) {
		for (Object o : collection) {
			try {
				if (OpenmrsUtil.nullSafeEquals(value, PropertyUtils.getProperty(o, property))) {
					return;
				}
			}
			catch (Exception ex) {
				// pass
			}
		}
		Assert.fail("Collection does not contain an element with " + property + " = " + value + ". Collection: "
		        + collection);
	}
	
	public static <T> ArgumentMatcher<T> containsElementsWithProperties(final String property,
	                                                                    final T... expectedPropertyValues) {
		return new ArgumentMatcher<T>() {
			
			@Override
			public boolean matches(Object o) {
				assertTrue(o instanceof Collection);
				Collection actual = (Collection) o;
				for (T expectedPropertyValue : expectedPropertyValues) {
					assertContainsElementWithProperty(actual, property, expectedPropertyValue);
				}
				return true;
			}
		};
	}
	
	public static <T> ArgumentMatcher<T> isCollectionOfExactlyElementsWithProperties(final String property,
	                                                                                 final Object... expectedPropertyValues) {
		return new ArgumentMatcher<T>() {
			
			@Override
			public boolean matches(Object o) {
				assertTrue(o instanceof Collection);
				Collection actual = (Collection) o;
				assertThat(actual.size(), is(expectedPropertyValues.length));
				for (Object expectedPropertyValue : expectedPropertyValues) {
					assertContainsElementWithProperty(actual, property, expectedPropertyValue);
				}
				return true;
			}
		};
	}
	
	/**
	 * Tests whether the substring is contained in the actual string.
	 */
	public static void assertContains(String substring, String actual) {
		if (substring == null) {
			return;
		}
		if (actual == null) {
			Assert.fail(substring + " is not contained in " + actual);
		}
		
		if (!actual.contains(substring)) {
			Assert.fail(substring + " is not contained in " + actual);
		}
	}
	
	/**
	 * Tests whether the two strings are equal, ignoring white space and capitalization.
	 */
	public static void assertFuzzyEquals(String expected, String actual) {
		if (expected == null && actual == null)
			return;
		if (expected == null || actual == null)
			Assert.fail(expected + " does not match " + actual);
		String test1 = stripWhitespaceAndConvertToLowerCase(expected);
		String test2 = stripWhitespaceAndConvertToLowerCase(actual);
		if (!test1.equals(test2)) {
			Assert.fail(expected + " does not match " + actual);
		}
	}
	
	/**
	 * Tests whether the substring is contained in the actual string. Allows for inclusion of
	 * regular expressions in the substring. Ignores white space. Ignores capitalization.
	 */
	public static void assertFuzzyContains(String substring, String actual) {
		if (substring == null) {
			return;
		}
		if (actual == null) {
			Assert.fail(substring + " is not contained in " + actual);
		}
		
		if (!Pattern.compile(stripWhitespaceAndConvertToLowerCase(substring), Pattern.DOTALL)
		        .matcher(stripWhitespaceAndConvertToLowerCase(actual)).find()) {
			Assert.fail(substring + " is not contained in " + actual);
		}
	}
	
	/**
	 * Tests whether the substring is NOT contained in the actual string. Allows for inclusion of
	 * regular expressions in the substring. Ignores white space. Ignores capitalization.
	 */
	public static void assertFuzzyDoesNotContain(String substring, String actual) {
		if (substring == null) {
			return;
		}
		if (actual == null) {
			return;
		}
		
		if (Pattern.compile(stripWhitespaceAndConvertToLowerCase(substring), Pattern.DOTALL)
		        .matcher(stripWhitespaceAndConvertToLowerCase(actual)).find()) {
			Assert.fail(substring + " found in  " + actual);
		}
	}
	
	private static String stripWhitespaceAndConvertToLowerCase(String string) {
		string = string.toLowerCase();
		string = string.replaceAll("\\s", "");
		return string;
	}

    //use DateMatchers.within(2, SECONDS, date)
    @Deprecated
	public static Matcher<Date> isJustNow() {
		return new ArgumentMatcher<Date>() {

			@Override
			public boolean matches(Object o) {
				// within the last two seconds should be safe enough... (needs to be more than a second to account for rounding issues)
				return Math.abs(System.currentTimeMillis() - ((Date) o).getTime()) < 2000;
			}
		};
	}
	
	/**
	 * Creates an argument matcher that tests equality based on the equals method, the developer
	 * doesn't have to type cast the returned argument when pass it to
	 * {@link org.mockito.Mockito#argThat(org.hamcrest.Matcher)} as it would be the case if we used
	 * {@link org.mockito.internal.matchers.Equals} matcher
	 * 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Matcher<T> equalsMatcher(final T object) {
		return new ArgumentMatcher<T>() {
			
			/**
			 * @see org.mockito.ArgumentMatcher#matches(Object)
			 */
			@Override
			public boolean matches(Object arg) {
				return OpenmrsUtil.nullSafeEquals(object, (T) arg);
			}
		};
	}
	
	public static String join(Iterable<?> iter, String separator) {
		StringBuilder ret = new StringBuilder();
		boolean first = true;
		for (Object o : iter) {
			if (!first) {
				ret.append(separator);
			} else {
				first = false;
			}
			ret.append(o);
		}
		return ret.toString();
	}
	
	public static String join(Object[] array, String separator) {
		StringBuilder ret = new StringBuilder();
		boolean first = true;
		for (Object o : array) {
			if (!first) {
				ret.append(separator);
			} else {
				first = false;
			}
			ret.append(o);
		}
		return ret.toString();
	}
	
	public static boolean sameProviders(Map<EncounterRole, Set<Provider>> a, Map<EncounterRole, Set<Provider>> b) {
		Collection<EncounterRole> roles = CollectionUtils.union(a.keySet(), b.keySet());
		for (EncounterRole role : roles) {
			Set<Provider> aSet = a.get(role);
			Set<Provider> bSet = b.get(role);
			if (aSet == null) {
				aSet = Collections.emptySet();
			}
			if (bSet == null) {
				bSet = Collections.emptySet();
			}
			if (!CollectionUtils.isEqualCollection(aSet, bSet)) {
				return false;
			}
		}
		return true;
	}
	
	public static Matcher<Encounter> hasProviders(final Map<EncounterRole, Set<Provider>> providers) {
		return new ArgumentMatcher<Encounter>() {
			
			@Override
			public boolean matches(Object argument) {
				Encounter actual = (Encounter) argument;
				return sameProviders(actual.getProvidersByRoles(), providers);
			}
		};
	}
}

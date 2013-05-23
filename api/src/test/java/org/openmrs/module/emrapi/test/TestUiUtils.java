/**
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

package org.openmrs.module.emrapi.test;

import org.openmrs.Concept;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.ui.framework.BasicUiUtils;
import org.openmrs.ui.framework.FormatterImpl;

/**
 * Implementation of UiUtils suitable for use in non-context-sensitive unit tests. This doesn't have
 * a MessageSource configured, so it won't do localization
 */
public class TestUiUtils extends BasicUiUtils {
	
	private AdministrationService administrationService;
	
	private boolean mockFormattingConcepts = false;
	
	/**
	 * If you use this constructor, the UiUtils will have no AdministrationService so it won't do
	 * date formatting
	 */
	public TestUiUtils() {
		this.formatter = new FormatterImpl(null, null);
	}
	
	/**
	 * Provides an AdministrationService that provides global properties for date formatting
	 * 
	 * @param administrationService
	 */
	public TestUiUtils(AdministrationService administrationService) {
		this.administrationService = administrationService;
		this.formatter = new FormatterImpl(null, this.administrationService);
	}
	
	/**
	 * If you set this to true, then calling the #format(Object) method on a concept will just print
	 * and arbitrary name, instead of going to the context to check locales
	 * 
	 * @param mockFormattingConcepts
	 */
	public void setMockFormattingConcepts(boolean mockFormattingConcepts) {
		this.mockFormattingConcepts = mockFormattingConcepts;
	}
	
	@Override
	public String format(Object o) {
		if (mockFormattingConcepts && o instanceof Concept) {
			Concept concept = (Concept) o;
			return concept.getNames().iterator().next().getName();
		} else {
			return super.format(o);
		}
	}
	
	@Override
	public String message(String code, Object... args) {
		String ret = code;
		if (args.length > 0) {
			ret += ":";
		}
		ret += TestUtils.join(args, ",");
		return ret;
	}
	
}

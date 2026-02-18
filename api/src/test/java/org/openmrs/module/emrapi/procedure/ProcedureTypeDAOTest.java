/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link HibernateProcedureTypeDAO}.
 */
public class ProcedureTypeDAOTest extends BaseModuleContextSensitiveTest {

	private static final String PROCEDURE_DATASET = "ProcedureDataset.xml";

	@Autowired
	private ProcedureTypeDAO procedureTypeDAO;

	@BeforeEach
	public void setUp() throws Exception {
		executeDataSet(PROCEDURE_DATASET);
	}

	@Test
	public void getByUuid_shouldReturnProcedureTypeWhenExists() {
		ProcedureType type = procedureTypeDAO.getByUuid("procedure-type-uuid-001");

		assertNotNull(type);
		assertEquals("Historical", type.getName());
		assertEquals("Historical procedures", type.getDescription());
	}

	@Test
	public void getByUuid_shouldReturnNullWhenNotExists() {
		assertNull(procedureTypeDAO.getByUuid("non-existent-uuid"));
	}

	@Test
	public void getAll_shouldReturnOnlyNonRetiredTypes() {
		List<ProcedureType> types = procedureTypeDAO.getAll(false);

		assertNotNull(types);
		assertEquals(2, types.size());
		for (ProcedureType type : types) {
			assertFalse(type.getRetired());
		}
	}

	@Test
	public void getAll_shouldReturnResultsSortedByName() {
		List<ProcedureType> types = procedureTypeDAO.getAll(false);

		assertEquals("Current", types.get(0).getName());
		assertEquals("Historical", types.get(1).getName());
	}

	@Test
	public void getAll_shouldIncludeRetiredWhenRequested() {
		// Retire one type first
		ProcedureType type = procedureTypeDAO.getByUuid("procedure-type-uuid-001");
		type.setRetired(true);
		type.setRetireReason("testing");
		procedureTypeDAO.saveOrUpdate(type);

		List<ProcedureType> nonRetired = procedureTypeDAO.getAll(false);
		List<ProcedureType> all = procedureTypeDAO.getAll(true);

		assertEquals(1, nonRetired.size());
		assertEquals(2, all.size());
	}

	@Test
	public void saveOrUpdate_shouldSaveNewProcedureType() {
		ProcedureType newType = new ProcedureType("Emergency", "Emergency procedures");

		ProcedureType saved = procedureTypeDAO.saveOrUpdate(newType);

		assertNotNull(saved.getProcedureTypeId());
		assertNotNull(saved.getUuid());

		ProcedureType retrieved = procedureTypeDAO.getByUuid(saved.getUuid());
		assertNotNull(retrieved);
		assertEquals("Emergency", retrieved.getName());
		assertEquals("Emergency procedures", retrieved.getDescription());
	}

	@Test
	public void saveOrUpdate_shouldUpdateExistingProcedureType() {
		ProcedureType type = procedureTypeDAO.getByUuid("procedure-type-uuid-001");
		type.setName("Updated Historical");
		type.setDescription("Updated description");

		procedureTypeDAO.saveOrUpdate(type);

		ProcedureType updated = procedureTypeDAO.getByUuid("procedure-type-uuid-001");
		assertEquals("Updated Historical", updated.getName());
		assertEquals("Updated description", updated.getDescription());
	}

	@Test
	public void delete_shouldRemoveProcedureTypeFromDatabase() {
		// Create a standalone type not referenced by any procedure
		ProcedureType newType = new ProcedureType("Temporary", "To be deleted");
		ProcedureType saved = procedureTypeDAO.saveOrUpdate(newType);
		String uuid = saved.getUuid();
		assertNotNull(procedureTypeDAO.getByUuid(uuid));

		procedureTypeDAO.delete(saved);

		assertNull(procedureTypeDAO.getByUuid(uuid));
	}

	@Test
	public void getByUuid_shouldReturnCurrentProcedureType() {
		ProcedureType type = procedureTypeDAO.getByUuid("cce8ea25-ba2c-4dfe-a386-fba606bc2ef2");

		assertNotNull(type);
		assertEquals("Current", type.getName());
		assertEquals("Current procedures", type.getDescription());
		assertFalse(type.getRetired());
	}
}

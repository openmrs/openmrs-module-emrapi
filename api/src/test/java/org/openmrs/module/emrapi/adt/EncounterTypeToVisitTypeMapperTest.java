package org.openmrs.module.emrapi.adt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class EncounterTypeToVisitTypeMapperTest extends BaseModuleContextSensitiveTest {
	
	private EncounterTypetoVisitTypeMapper encounterTypetoVisitTypeMapper;
	private EncounterService encounterService;
	private VisitService visitService;
	private AdministrationService adminService;
	
	@Before
	public void before(){
		encounterTypetoVisitTypeMapper = new EncounterTypetoVisitTypeMapper();
		encounterService = mock(EncounterService.class);
		visitService = mock(VisitService.class);
		adminService = mock(AdministrationService.class);
		
		EncounterType e1 = new EncounterType();
		e1.setId(1);
		e1.setUuid("61ae96f4-6afe-4351-b6f8-cd4fc383cce1");
		
		EncounterType e2 = new EncounterType();
		e2.setId(2);
		e2.setUuid("07000be2-26b6-4cce-8b40-866d8435b613");
		
		VisitType v1 = new VisitType();
		v1.setId(1);
		v1.setUuid("c0c579b0-8e59-401d-8a4a-976a0b183519");
		
		VisitType v2 = new VisitType();
		v2.setId(2);
		v2.setUuid("759799ab-c9a5-435e-b671-77773ada74e4");
		
		when(encounterService.getEncounterType(1)).thenReturn(e1);
		when(encounterService.getEncounterType("61ae96f4-6afe-4351-b6f8-cd4fc383cce1")).thenReturn(e1);
		
		when(encounterService.getEncounterType(2)).thenReturn(e2);
		when(encounterService.getEncounterType("07000be2-26b6-4cce-8b40-866d8435b613")).thenReturn(e2);
		
		when(visitService.getVisitType(1)).thenReturn(v1);
		when(visitService.getVisitTypeByUuid("c0c579b0-8e59-401d-8a4a-976a0b183519")).thenReturn(v1);
		
		when(visitService.getVisitType(2)).thenReturn(v2);
		when(visitService.getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e4")).thenReturn(v2);
		
		encounterTypetoVisitTypeMapper.setVisitService(visitService);
		encounterTypetoVisitTypeMapper.setAdminService(adminService);
	}
	
	@Test
	public void testMappingWithNoMappingString() {
		encounterTypetoVisitTypeMapper.setMappingString("");
		EncounterType e = encounterService.getEncounterType(1);
		
		when(adminService.getGlobalProperty(EmrApiConstants.GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP)).thenReturn("");
		encounterTypetoVisitTypeMapper.setAdminService(adminService);
		
		VisitType visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNull(visitType);
	}
	
	@Test
	public void testMappingWithEncounterTypeIds(){
		encounterTypetoVisitTypeMapper.setMappingString("1:2");
		EncounterType e = encounterService.getEncounterType(1);
		
		VisitType visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Expected visit_type_id 2", visitType.getId().longValue(), 2);
		
		// test with the Uuid for the visitType
		encounterTypetoVisitTypeMapper.setMappingString("1:759799ab-c9a5-435e-b671-77773ada74e4");
		visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Expected visit_type_uuid 759799ab-c9a5-435e-b671-77773ada74e4", visitType.getUuid(), "759799ab-c9a5-435e-b671-77773ada74e4");
	}
	
	@Test
	public void testMappingWithEncounterTypeUuids(){
		encounterTypetoVisitTypeMapper.setMappingString("07000be2-26b6-4cce-8b40-866d8435b613:1");
		EncounterType e = encounterService.getEncounterType(2);
		
		VisitType visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		Assert.assertNotNull(visitType);
		Assert.assertEquals(1,visitType.getId().longValue());
		
		// test with the Uuid for the visitType
		encounterTypetoVisitTypeMapper.setMappingString("07000be2-26b6-4cce-8b40-866d8435b613:c0c579b0-8e59-401d-8a4a-976a0b183519");
		visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals("c0c579b0-8e59-401d-8a4a-976a0b183519",visitType.getUuid());
	}
	
	@Test
	public void testMappingWithDefaultEncounterTypeMapping(){
		encounterTypetoVisitTypeMapper.setMappingString("default:1");
		EncounterType e = encounterService.getEncounterType(2);
		
		VisitType visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals(visitType.getId().longValue(), 1);
		
		// test with the Uuid for the visitType
		encounterTypetoVisitTypeMapper.setMappingString("default:c0c579b0-8e59-401d-8a4a-976a0b183519");
		visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals(visitType.getUuid(), "c0c579b0-8e59-401d-8a4a-976a0b183519");
	}
	@Test
	public void testMappingWithDefaultEncounterTypeMappingOverride(){
		//default visit_type is 1 but override is 2
		encounterTypetoVisitTypeMapper.setMappingString("default:1,2:2");
		EncounterType e = encounterService.getEncounterType(2);
		
		VisitType visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals(visitType.getId().longValue(), 2);
		
		// test with the Uuid for the visitType
		encounterTypetoVisitTypeMapper.setMappingString("default:c0c579b0-8e59-401d-8a4a-976a0b183519,07000be2-26b6-4cce-8b40-866d8435b613:759799ab-c9a5-435e-b671-77773ada74e4");
		visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals(visitType.getUuid(), "759799ab-c9a5-435e-b671-77773ada74e4");
	}
	
	@Test
	public void testMappingWithDefaultEncounterTypeMappingOverrideWithDefaultLast(){
		//default visit_type is 1 but override is 2
		encounterTypetoVisitTypeMapper.setMappingString("2:2,default:1");
		EncounterType e = encounterService.getEncounterType(2);
		
		VisitType visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals(2, visitType.getId().longValue());
		
		// test with the Uuid for the visitType
		encounterTypetoVisitTypeMapper.setMappingString("07000be2-26b6-4cce-8b40-866d8435b613:759799ab-c9a5-435e-b671-77773ada74e4,default:c0c579b0-8e59-401d-8a4a-976a0b183519");
		visitType = encounterTypetoVisitTypeMapper.getVisitTypeForEncounterType(e);
		
		Assert.assertNotNull(visitType);
		Assert.assertEquals("759799ab-c9a5-435e-b671-77773ada74e4",visitType.getUuid());
	}
	
}

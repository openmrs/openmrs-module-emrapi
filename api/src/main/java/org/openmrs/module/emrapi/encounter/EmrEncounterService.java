package org.openmrs.module.emrapi.encounter;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransactionResponse;

/**
 * <pre>
 * Handy service to create/update an {@link org.openmrs.Encounter}. Use this to add {@link org.openmrs.Obs}, {@link org.openmrs.TestOrder} to an Encounter.
 * The encounter is saved against the latest active visit of the {@link org.openmrs.Patient} if one exists, else a new visit is created.
 *
 * A strategy can be specified to choose which Encounter to update
 * @see org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher
 * </pre>
 */
public interface EmrEncounterService extends OpenmrsService {

    EncounterTransactionResponse save(EncounterTransaction encounterTransaction);
}

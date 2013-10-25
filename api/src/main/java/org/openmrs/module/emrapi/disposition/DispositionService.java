package org.openmrs.module.emrapi.disposition;

import org.openmrs.Obs;

import java.io.IOException;
import java.util.List;

public interface DispositionService {


    /**
     * Allows the default name of the disposition config string to be overwritten
     *
     * @param dispositionConfig
     */
    void setDispositionConfig(String dispositionConfig);

    /**
     * Fetch a copy of the Disposition Descriptor, which describes the concepts necessary (and optional,
     * like admission location, transfer location, and date of death) for recording a Disposition concept set
     *
     * @return dispositionDescriptor
     */
    DispositionDescriptor getDispositionDescriptor();

    /**
     * Gets all the currently configured dispositions
     *
     * @return
     * @throws IOException
     */
    List<Disposition> getDispositions() throws IOException;

    /**
     * Fetches a disposition referenced by it's unique id
     *
     * @param uniqueId
     * @return
     * @throws IOException
     */
    Disposition getDispositionByUniqueId(String uniqueId) throws IOException;


    /**
     * Given an disposition observation, returns the disposition recorded in that obs
     *
     * @param obs
     * @return
     * @throws IOException
     */
    Disposition getDispositionFromObs(Obs obs) throws IOException;


    /**
     * Given a disposition obs group, returns the disposition recorded in that obs group
     *
     * @param obsGroup
     * @return
     * @throws IOException
     */
    Disposition getDispositionFromObsGroup(Obs obsGroup) throws IOException;

}

package org.openmrs.module.emrapi.disposition;

import org.openmrs.Obs;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

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
     * True/false whether dispositions are currently configured and supported
     *
     * @return
     */
    boolean dispositionsSupported();

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
    List<Disposition> getDispositions();

    /**
     * Gets only dispositions that are valid in the specified visit context
     *
     * Currently, logic works as follows:
     * 1) if visit.isActive() = false, then return all dispositions, else
     * 2) if visit.isAdmitted() = true, then return only those whore careSettingTypes contains INPATIENT (or any where careSettingType = null)
     * 3) if visit.isAdmitted() = false, then return only those whore careSettingTypes contains OUTPATIENT (or any where careSettingType = null)
     */
    List<Disposition> getValidDispositions(VisitDomainWrapper visitDomainWrapper);

    /**
     * Fetches a disposition referenced by it's unique id
     *
     * @param uniqueId
     * @return
     * @throws IOException
     */
    Disposition getDispositionByUniqueId(String uniqueId);

    /**
     * Gets all dispositions of the specified type
     *
     * @param dispositionType
     * @return
     */
    List<Disposition> getDispositionsByType(DispositionType dispositionType);

    /**
     * Given an disposition observation, returns the disposition recorded in that obs
     *
     * @param obs
     * @return
     * @throws IOException
     */
    Disposition getDispositionFromObs(Obs obs);


    /**
     * Given a disposition obs group, returns the disposition recorded in that obs group
     *
     * @param obsGroup
     * @return
     * @throws IOException
     */
    Disposition getDispositionFromObsGroup(Obs obsGroup);


}

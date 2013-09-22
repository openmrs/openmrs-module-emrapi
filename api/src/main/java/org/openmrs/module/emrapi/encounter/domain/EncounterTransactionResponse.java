package org.openmrs.module.emrapi.encounter.domain;

public class EncounterTransactionResponse {

    private String visitUuid;
    private String encounterUuid;
    private String status;
    private String errorMessage;

    public EncounterTransactionResponse() {
    }

    public EncounterTransactionResponse(String visitUuid, String encounterUuid) {
        this.visitUuid = visitUuid;
        this.encounterUuid = encounterUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
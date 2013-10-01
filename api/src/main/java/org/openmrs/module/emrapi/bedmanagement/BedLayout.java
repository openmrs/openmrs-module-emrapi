package org.openmrs.module.emrapi.bedmanagement;

public class BedLayout {
    private Integer rowNumber;
    private Integer columnNumber;
    private Integer bedId;
    private Integer bedPatientAssignmentId;

    public Integer getBedPatientAssignmentId() {
        return bedPatientAssignmentId;
    }

    public void setBedPatientAssignmentId(Integer bedPatientAssignmentId) {
        this.bedPatientAssignmentId = bedPatientAssignmentId;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public Integer getBedId() {
        return bedId;
    }

    public void setBedId(Integer bedId) {
        this.bedId = bedId;
    }

    public Boolean isAvailable() {
        return this.bedPatientAssignmentId != null;
    }

    @Override
    public String toString() {
        return "BedLayout{" +
                "rowNumber=" + rowNumber +
                ", columnNumber=" + columnNumber +
                ", bedId=" + bedId +
                ", bedPatientAssignmentId=" + bedPatientAssignmentId +
                '}';
    }
}

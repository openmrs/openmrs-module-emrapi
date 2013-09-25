package org.openmrs.module.emrapi.bedmanagement.domain;

public class AdmissionLocation {
    private String name;
    private String description;
    private int totalBeds;
    private int occupiedBeds;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTotalBeds() {
        return totalBeds;
    }

    public int getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTotalBeds(int totalBeds) {
        this.totalBeds = totalBeds;
    }

    public void setOccupiedBeds(int occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    @Override
    public String toString() {
        return "AdmissionLocation{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", totalBeds=" + totalBeds +
                ", occupiedBeds=" + occupiedBeds +
                '}';
    }
}

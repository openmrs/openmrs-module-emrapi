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

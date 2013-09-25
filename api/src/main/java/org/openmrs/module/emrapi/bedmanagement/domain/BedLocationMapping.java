package org.openmrs.module.emrapi.bedmanagement.domain;

import org.openmrs.Location;

public class BedLocationMapping {
    private int id;
    private Location location;
    private Bed bed;
    private int row;
    private int column;
}

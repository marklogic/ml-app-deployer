package com.marklogic.mgmt.api.database;

public class FieldPath {

    private String path;
    private Double weight;

    public FieldPath() {
    }

    public FieldPath(String path, Double weight) {
        this.path = path;
        this.weight = weight;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

}

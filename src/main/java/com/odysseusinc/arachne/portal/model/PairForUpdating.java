package com.odysseusinc.arachne.portal.model;

public class PairForUpdating<DS> {

    private DS existed;
    private DS updated;

    public PairForUpdating(DS existed, DS updated) {

        this.existed = existed;
        this.updated = updated;
    }

    public DS getExisted() {
        return existed;
    }

    public void setExisted(DS existed) {
        this.existed = existed;
    }

    public DS getUpdated() {
        return updated;
    }

    public void setUpdated(DS updated) {
        this.updated = updated;
    }
}
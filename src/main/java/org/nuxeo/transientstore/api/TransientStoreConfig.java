package org.nuxeo.transientstore.api;

import org.nuxeo.common.xmap.annotation.XNode;

public class TransientStoreConfig {

    @XNode("@name")
    protected String name;

    @XNode("@cluster")
    protected boolean cluster=false;

    // target size that ideally should never be exceeded
    @XNode("targetMaxSizeMB")
    protected int targetMaxSizeMB;

    // size that must never be exceeded
    @XNode("absoluteMaxSizeMB")
    protected int absoluteMaxSizeMB=-1;

    @XNode("fistLevelTTL")
    protected int fistLevelTTL = 60*2;

    @XNode("secondLevelTTL")
    protected int secondLevelTTL = 10;

    public String getName() {
        return name;
    }

    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    public int getTargetMaxSizeMB() {
        return targetMaxSizeMB;
    }

    public void setTargetMaxSizeMB(int targetMaxSizeMB) {
        this.targetMaxSizeMB = targetMaxSizeMB;
    }

    public int getAbsoluteMaxSizeMB() {
        return absoluteMaxSizeMB;
    }

    public void setAbsoluteMaxSizeMB(int absoluteMaxSizeMB) {
        this.absoluteMaxSizeMB = absoluteMaxSizeMB;
    }

    public int getFistLevelTTL() {
        return fistLevelTTL;
    }

    public void setFistLevelTTL(int fistLevelTTL) {
        this.fistLevelTTL = fistLevelTTL;
    }

    public int getSecondLevelTTL() {
        return secondLevelTTL;
    }

    public void setSecondLevelTTL(int secondLevelTTL) {
        this.secondLevelTTL = secondLevelTTL;
    }


}

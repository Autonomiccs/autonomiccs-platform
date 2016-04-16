package br.com.autonomiccs.autonomic.plugin.common.enums;

public enum SystemVmType {

    ClusterManagerAgent("CM-A"),
    ClusterManagerStartHostService("CM-SHS");


    private String namePrefix;

    private SystemVmType(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}

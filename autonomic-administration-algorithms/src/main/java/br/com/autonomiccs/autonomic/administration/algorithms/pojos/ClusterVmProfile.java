package br.com.autonomiccs.autonomic.administration.algorithms.pojos;

/**
 * Contains the cluster VMs profile attributes (number of instances, total cpus,
 * total cpu speed, memory, cpus profile, cpus speed profile, memory profile).
 */
public class ClusterVmProfile {

    private int numberOfInstances;
    private int totalCpus;
    private int totalCpuSpeed;
    private int totalMemory;
    private double cpusProfile;
    private double cpuSpeedProfile;
    private double memoryProfile;

    public int getNumberOfInstances() {
        return numberOfInstances;
    }
    public void setNumberOfInstances(int totalInstances) {
        this.numberOfInstances = totalInstances;
    }
    public int getTotalCpus() {
        return totalCpus;
    }
    public void setTotalCpus(int totalCpus) {
        this.totalCpus = totalCpus;
    }
    public int getTotalCpuSpeed() {
        return totalCpuSpeed;
    }
    public void setTotalCpuSpeed(int totalCpuSpeed) {
        this.totalCpuSpeed = totalCpuSpeed;
    }
    public int getTotalMemory() {
        return totalMemory;
    }
    public void setTotalMemory(int totalMemory) {
        this.totalMemory = totalMemory;
    }
    public double getCpusProfile() {
        return cpusProfile;
    }
    public void setCpusProfile(double cpusProfile) {
        this.cpusProfile = cpusProfile;
    }
    public double getCpuSpeedProfile() {
        return cpuSpeedProfile;
    }
    public void setCpuSpeedProfile(double speedProfile) {
        this.cpuSpeedProfile = speedProfile;
    }
    public double getMemoryProfile() {
        return memoryProfile;
    }
    public void setMemoryProfile(double memoryProfile) {
        this.memoryProfile = memoryProfile;
    }

}

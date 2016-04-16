package br.com.autonomiccs.autonomic.administration.algorithms.pojos;

/**
 * Contains the host profile attributes (number of cpus, cpu speed and memory).
 */
public class HostProfile {

    private double cpusProfile;
    private double cpuSpeedProfile;
    private double memoryProfile;

    public double getCpusProfile() {
        return cpusProfile;
    }
    public void setCpusProfile(double cpusProfile) {
        this.cpusProfile = cpusProfile;
    }
    public double getCpuSpeedProfile() {
        return cpuSpeedProfile;
    }
    public void setCpuSpeedProfile(double cpuSpeedProfile) {
        this.cpuSpeedProfile = cpuSpeedProfile;
    }
    public double getMemoryProfile() {
        return memoryProfile;
    }
    public void setMemoryProfile(double memoryProfile) {
        this.memoryProfile = memoryProfile;
    }

}

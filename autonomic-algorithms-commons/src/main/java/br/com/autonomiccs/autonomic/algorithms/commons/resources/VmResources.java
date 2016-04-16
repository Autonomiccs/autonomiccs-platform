package br.com.autonomiccs.autonomic.algorithms.commons.resources;

/**
 * Contains the VM resources (vm id, cpus, cpu speed and memory).
 */
public class VmResources implements Cloneable {
    private long vmId;
    private int cpus;
    private long cpuSpeed;
    private long memory;

    public VmResources(long id, int cpus, long cpuSpeed, long memory) {
        this.vmId = id;
        this.cpus = cpus;
        this.cpuSpeed = cpuSpeed;
        this.memory = memory;
    }

    public int getNumberOfCpus() {
        return cpus;
    }

    public long getVmId() {
        return vmId;
    }

    public long getCpuSpeed() {
        return cpuSpeed;
    }

    public long getMemoryInMegaBytes() {
        return memory;
    }

    public void setVmId(long vmId) {
        this.vmId = vmId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

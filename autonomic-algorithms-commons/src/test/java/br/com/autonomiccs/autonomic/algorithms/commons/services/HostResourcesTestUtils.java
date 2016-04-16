package br.com.autonomiccs.autonomic.algorithms.commons.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ObjectUtils;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

public class HostResourcesTestUtils {

    private Random random = new Random();

    protected void createHostWithSmallVms(HostResources host, int amount) {
        List<VmResources> vms = createSmallVms(amount);
        configuringHostVms(host, vms);
    }

    private void configuringHostVms(HostResources host, List<VmResources> vms) {
        long usedMemory = 0;
        long usedCpuSpeed = 0;
        for (VmResources vm : vms) {
            usedMemory += vm.getMemoryInMegaBytes();
            usedCpuSpeed += vm.getCpuSpeed() * vm.getNumberOfCpus();
        }
        host.setUsedMemoryInMegaBytes(usedMemory);
        host.setVmsResources(vms);
        host.setUsedCpu(usedCpuSpeed);
    }

    protected List<VmResources> createSmallVms(int amount) {
        return replicateVm(amount, createVmResourcesSmall());
    }

    private VmResources createVmResourcesSmall() {
        return new VmResources(random.nextLong(), 1, 1000l, 512l);
    }

    private List<VmResources> replicateVm(int amount, VmResources vm) {
        List<VmResources> vms = new ArrayList<VmResources>();
        for (int i = 0; i < amount; i++) {
            VmResources clonedVm = (VmResources) ObjectUtils.clone(vm);
            clonedVm.setVmId(random.nextLong());
            vms.add(clonedVm);
        }
        return vms;
    }

}

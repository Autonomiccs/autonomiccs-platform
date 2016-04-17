package br.com.autonomiccs.autonomic.administration.plugin.hypervisors;

import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;

/**
 * All Host Hypervisors basic operations developed in the consolidation plugin
 * are in this class.
 */
public interface HypervisorHost {

    /**
     * Shutdown a given {@link HostVO}.
     *
     * @param hostVo
     */
    public void shutdownHost(HostVO hostVo);

    /**
     * Returns true if the given {@link HypervisorType} all methods from
     * {@link HypervisorHost}.
     *
     * @param hypervisorType
     * @return
     */
    public boolean supportsHypervisor(HypervisorType hypervisorType);

}

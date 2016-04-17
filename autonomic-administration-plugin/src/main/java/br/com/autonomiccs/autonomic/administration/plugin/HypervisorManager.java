package br.com.autonomiccs.autonomic.administration.plugin;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.cloud.host.Host;
import com.cloud.host.HostVO;

import br.com.autonomiccs.autonomic.administration.plugin.hypervisors.HypervisorHost;
import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;

/**
 * Manages hypervisor's operations, passing the execution flow to the correct hypervisor facade.
 */
@Component(value = "hypervisorManager")
public class HypervisorManager {

    @Inject
    private HostService hostService;

    @Inject
    private List<HypervisorHost> hypervisorHosts;

    /**
     * Executes the shutdownHost method of the {@link HostVO} hypervisor. If the
     * hypervisor from {@link HostVO} does not supports shutdown, then it throws
     * a {@link RuntimeException}.
     *
     * @throws RuntimeException
     * @param hostVo
     */
    public void shutdownHost(HostVO hostVo) {
        if (!Host.Type.Routing.equals(hostVo.getType())) {
            return;
        }
        for (HypervisorHost currentHypervisorHost : hypervisorHosts) {
            if (currentHypervisorHost.supportsHypervisor(hostVo.getHypervisorType())) {
                shutdown(hostVo, currentHypervisorHost);
                return;
            }
        }
        throw new RuntimeException(String.format("The the hypervisor[%d] from host[%d] does not support shutdown to consolidate", hostVo.getHypervisorType(), hostVo.getId()));
    }

    /**
     * Calls the {@link HypervisorHost#shutdownHost(HostVO)} method and mark the
     * host administration process as {@link HostAdministrationStatus#ShutDownToConsolidate} using
     * {@link HostService#markHostAsShutdownByAdministrationAgent(long)}.
     *
     * @param hostVo
     * @param currentHypervisorHost
     */
    private void shutdown(HostVO hostVo, HypervisorHost currentHypervisorHost) {
        hostService.loadHostDetails(hostVo);
        currentHypervisorHost.shutdownHost(hostVo);
        hostService.markHostAsShutdownByAdministrationAgent(hostVo.getId());
    }

}

package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster;
import com.cloud.resource.ResourceState;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.HostJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;

/**
 * Provides operations over the host table.
 */
@Service
public class HostService {

    @Inject
    private HostJdbcDao hostDaoJdbc;
    @Inject
    private HostDao hostDao;
    @Inject
    private VMInstanceDao vmInstanceDao;

    /**
     * Returns true if the host (with the given id) is 'Up' and 'Enabled'
     *
     * @param hostId
     * @return
     */
    public boolean isHostUpAndEnabled(long hostId) {
        return Status.Up.equals(hostDaoJdbc.getStatus(hostId)) && ResourceState.Enabled.equals(hostDaoJdbc.getResourceState(hostId));
    }

    /**
     * Returns true if the host (with the given id) is in the Maintenance Error
     * state ({@link ResourceState#ErrorInMaintenance}).
     *
     * @param hostId
     * @return
     */
    public boolean isHostInMaintenanceError(long hostId) {
        return hostDaoJdbc.getResourceState(hostId) == ResourceState.ErrorInMaintenance;
    }

    /**
     * Returns true if the host (with the given id) is preparing for maintenance
     * state (({@link ResourceState#PrepareForMaintenance}).
     *
     * @param hostId
     * @return
     */
    public boolean isHostInPreparedForMaintenance(long hostId) {
        return hostDaoJdbc.getResourceState(hostId) == ResourceState.PrepareForMaintenance;
    }

    /**
     * Marks the host as shutdown to consolidated (
     * {@link HostAdministrationStatus#ShutDownToConsolidate}).
     *
     * @param id
     */
    @Transactional(readOnly = false)
    public void markHostAsShutdownByAdministrationAgent(long id) {
        hostDaoJdbc.setAdministrationStatus(HostAdministrationStatus.ShutDownToConsolidate, id);
    }

    /**
     * Loads the host details. This method must be executed first than any other
     * that might need the host informations, such as host ip, username,
     * password or guest uuid; thus this method is the first method to be
     * executed in {@link #getConnection(HostVO)}, getConnection is the first
     * method executed by {@link #shutdownHost(HostVO)}. TODO
     * @param host
     */
    public void loadHostDetails(HostVO host) {
        hostDao.loadDetails(host);
    }

    /**
     * It returns all of the hypervisors types that are in use by the whole environment.
     * @return List<HypervisorType>
     */
    public List<HypervisorType> getAllHypervisorsTypeInCloud() {
        return this.hostDaoJdbc.getAllHypervisorsTypeInCloud();
    }

    /**
     * Searches a host with the given id
     *
     * @param hostId
     * @return {@link HostVO} that represents a host with the given id.
     */
    public HostVO findHostById(Long hostId) {
        return hostDao.findById(hostId);
    }

    public List<HostVO> listAllHostsInCluster(Cluster cluster) {
        return hostDao.listAllUpAndEnabledNonHAHosts(Type.Routing, cluster.getId(), cluster.getPodId(), cluster.getDataCenterId(), null);
    }

    public List<VMInstanceVO> listAllVmsFromHost(long hostId) {
        return vmInstanceDao.listByHostId(hostId);
    }

    @Transactional(readOnly = false)
    public void updateHostPrivaceMacAddress(HostVO hostVo, String privateMacAddress) {
        hostVo.setPrivateMacAddress(privateMacAddress);
        hostDao.update(hostVo.getId(), hostVo);
    }

    public boolean isHostDown(long id) {
        HostAdministrationStatus hostConsolidationStatus = hostDaoJdbc.getAdministrationStatus(id);
        return hostConsolidationStatus != null && !HostAdministrationStatus.Up.equals(hostConsolidationStatus);
    }

    public boolean isThereAnyHostOnCloudDeactivatedByOurManager() {
        return hostDaoJdbc.isThereAnyHostOnCloudDeactivatedByOurManager();
    }

}

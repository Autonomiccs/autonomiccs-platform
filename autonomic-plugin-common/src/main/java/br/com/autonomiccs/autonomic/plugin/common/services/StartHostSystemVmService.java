package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.vm.NicVO;
import com.cloud.vm.dao.NicDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;

/**
 * This class deals with interactions with the Start host service system VMs
 */
@Service
public class StartHostSystemVmService {

    private Logger logger = Logger.getLogger(getClass());

    private static final String RESERVER_NAME = "PodBasedNetworkGuru";

    @Autowired
    private AutonomiccsSystemVmJdbcDao autonomiccsSystemVmJdbcDao;

    @Autowired
    private NicDao nicDao;

    @Autowired
    private HostUtils hostUtils;

    /**
     * Checks if there is a start host system Vm on the given pod.
     * It uses the {@link #getStartHostServiceVmIdFromPod(Long)} method.
     *
     * @param podId
     * @return true if there a startHostSystemVm on the given Pod
     */
    public boolean isStartHostSystemVmDeployedOnPod(Long podId) {
        return getStartHostServiceVmIdFromPod(podId) != null;
    }

    /**
     * Checks if there is a start host system Vm on the given pod.
     * It uses the {@link AutonomiccsSystemVmJdbcDao#getStartHostServiceVmIdFromPod(Long, SystemVmType)} method.
     *
     * @param podId
     * @return the id of the start host service system vm.
     */
    public Long getStartHostServiceVmIdFromPod(Long podId) {
        return autonomiccsSystemVmJdbcDao.getStartHostServiceVmIdFromPod(podId, SystemVmType.ClusterManagerStartHostService);
    }

    public boolean isStartHostServiceVmRunningOnPod(Long podId) {
        Long startHostServiceVmId = getStartHostServiceVmIdFromPod(podId);
        if(startHostServiceVmId == null){
            return false;
        }
        NicVO nic = getInternalCommunicationNicFromVm(startHostServiceVmId);
        if (nic == null) {
            return false;
        }
        return hostUtils.isHostReachable(nic.getIPv4Address());
    }

    private NicVO getInternalCommunicationNicFromVm(Long startHostServiceVmId) {
        List<NicVO> nicsOfVm = nicDao.listByVmId(startHostServiceVmId);
        for (NicVO nic : nicsOfVm) {
            if (RESERVER_NAME.equals(nic.getReserver())) {
                return nic;
            }
        }
        logger.info(String.format("Could not find a NIC created by PodBasedNetworkGuru, vmId[%d]", startHostServiceVmId));
        return null;
    }

    public boolean isStartHostServiceVmReadyToStartHostOnPod(Long podId) {
        Long startHostServiceVmId = getStartHostServiceVmIdFromPod(podId);
        if (startHostServiceVmId == null) {
            return false;
        }
        NicVO nic = getInternalCommunicationNicFromVm(startHostServiceVmId);
        if (nic == null) {
            return false;
        }
        return hostUtils.isHostReachableOnPort8080(nic.getIPv4Address());
    }

}

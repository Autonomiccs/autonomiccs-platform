package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.HostPodDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.HostJdbcDao;

/**
 * This class is used to manage Pod object into the database.
 */
@Service
public class PodService {

    @Autowired
    private HostPodDao hostPodDao;

    @Autowired
    private HostJdbcDao hostJdbcDao;

    /**
     * List all Pods from a given zone.
     *
     * @param zoneId
     * @return {@link List<HostPodVo>} all pods of the given zone.
     */
    public List<HostPodVO> getAllPodsEnabledFromZone(long zoneId) {
        return hostPodDao.listByDataCenterId(zoneId);
    }

    public HostPodVO findPodById(Long podId) {
        return hostPodDao.findById(podId);
    }

    public boolean isThereAnyHostOnPodDeactivatedByOurManager(long id) {
        return hostJdbcDao.isThereAnyHostOnPodDeactivatedByOurManager(id);
    }

}

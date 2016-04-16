package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;

@Service
public class ClusterService {

    @Autowired
    private ClusterDao clusterDao;

    public List<ClusterVO> listAllClustersFromPod(long podId) {
        return clusterDao.listByPodId(podId);
    }

    public ClusterVO findById(long clusterId) {
        return clusterDao.findById(clusterId);
    }

    public List<ClusterVO> listAllClustersOnZone(Long zoneId) {
        return clusterDao.listClustersByDcId(zoneId);
    }

    public List<ClusterVO> listAllClusters() {
        return clusterDao.listAll();
    }

}

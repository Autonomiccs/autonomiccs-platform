package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterManagerHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm;

@Service("autonomicClusterManagementHeuristicService")
public class AutonomicClusterManagementHeuristicService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Class<? extends ClusterManagerHeuristicAlgorithm>> algorithmsMap = new HashMap<String, Class<? extends ClusterManagerHeuristicAlgorithm>>();

    @Autowired
    private ConfigurationDao configurationDao;

    /**
     * TODO
     */
    public ClusterManagerHeuristicAlgorithm getConsolidationAlgorithm() {
        String algorithmName = configurationDao.getValue("smart.cloudstack.clustermanager.algorithm");
        if (StringUtils.isBlank(algorithmName)) {
            return getDummyClusterManagementHeuristc();
        }
        return getInstanceOfClass(algorithmName);
    }

    private ClusterManagerHeuristicAlgorithm getInstanceOfClass(String algorithmName) {
        Class<? extends ClusterManagerHeuristicAlgorithm> clusterManagerHeuristicAlgorithmClass = algorithmsMap.get(algorithmName);
        if (clusterManagerHeuristicAlgorithmClass != null) {
            return getInstanceOfClass(clusterManagerHeuristicAlgorithmClass);
        }
        try {
            Class<? extends ClusterManagerHeuristicAlgorithm> algorithmClass = loadAlgorithmClass(algorithmName);
            return getInstanceOfClass(algorithmClass);
        } catch (Exception e) {
            logger.warn(String.format("Could not load heuristics from algorithm [algorithm class name=%s], using the Dummy management algorithm", algorithmName), e);
            return getDummyClusterManagementHeuristc();
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends ClusterManagerHeuristicAlgorithm> loadAlgorithmClass(String algorithmName) throws ClassNotFoundException {
        logger.info(String.format("Loading heuristics from algorithm [algorithm class name=%s]", algorithmName));
        Class<? extends ClusterManagerHeuristicAlgorithm> algorithmClass = (Class<? extends ClusterManagerHeuristicAlgorithm>)Class.forName(algorithmName.trim());
        algorithmsMap.put(algorithmName, algorithmClass);
        return algorithmClass;
    }

    private ClusterManagerHeuristicAlgorithm getInstanceOfClass(Class<? extends ClusterManagerHeuristicAlgorithm> clusterManagerHeuristicAlgorithmClass) {
        try {
            return clusterManagerHeuristicAlgorithmClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Error while instantiating object, using the dummy algorithm.", e);
            return getDummyClusterManagementHeuristc();
        }
    }

    private ClusterManagerHeuristicAlgorithm getDummyClusterManagementHeuristc() {
        return getInstanceOfClass(ClusterManagementDummyAlgorithm.class.toString());
    }
}

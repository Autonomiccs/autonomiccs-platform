package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm;

@Service("autonomicClusterManagementHeuristicService")
public class AutonomicClusterManagementHeuristicService {

    public final static String CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY = "autonomiccs.clustermanager.algorithm";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Class<? extends ClusterAdministrationHeuristicAlgorithm>> algorithmsMap = new HashMap<String, Class<? extends ClusterAdministrationHeuristicAlgorithm>>();

    @Autowired
    private ConfigurationDao configurationDao;

    /**
     * TODO
     */
    public ClusterAdministrationHeuristicAlgorithm getAdministrationAlgorithm() {
        String algorithmName = configurationDao.getValue(CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY);
        if (StringUtils.isBlank(algorithmName)) {
            return getDummyClusterManagementHeuristc();
        }
        return getInstanceOfClass(algorithmName);
    }

    private ClusterAdministrationHeuristicAlgorithm getInstanceOfClass(String algorithmName) {
        Class<? extends ClusterAdministrationHeuristicAlgorithm> clusterManagerHeuristicAlgorithmClass = algorithmsMap.get(algorithmName);
        if (clusterManagerHeuristicAlgorithmClass != null) {
            return getInstanceOfClass(clusterManagerHeuristicAlgorithmClass);
        }
        try {
            Class<? extends ClusterAdministrationHeuristicAlgorithm> algorithmClass = loadAlgorithmClass(algorithmName);
            return getInstanceOfClass(algorithmClass);
        } catch (Exception e) {
            logger.warn(String.format("Could not load heuristics from algorithm [algorithm class name=%s], using the Dummy management algorithm", algorithmName), e);
            return getDummyClusterManagementHeuristc();
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends ClusterAdministrationHeuristicAlgorithm> loadAlgorithmClass(String algorithmName) throws ClassNotFoundException {
        logger.info(String.format("Loading heuristics from algorithm [algorithm class name=%s]", algorithmName));
        Class<? extends ClusterAdministrationHeuristicAlgorithm> algorithmClass = (Class<? extends ClusterAdministrationHeuristicAlgorithm>)Class.forName(algorithmName.trim());
        algorithmsMap.put(algorithmName, algorithmClass);
        return algorithmClass;
    }

    private ClusterAdministrationHeuristicAlgorithm getInstanceOfClass(Class<? extends ClusterAdministrationHeuristicAlgorithm> clusterManagerHeuristicAlgorithmClass) {
        try {
            return clusterManagerHeuristicAlgorithmClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Error while instantiating object, using the dummy algorithm.", e);
            return getDummyClusterManagementHeuristc();
        }
    }

    private ClusterAdministrationHeuristicAlgorithm getDummyClusterManagementHeuristc() {
        return getInstanceOfClass(ClusterManagementDummyAlgorithm.class);
    }
}

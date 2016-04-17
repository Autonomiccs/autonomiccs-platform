package br.com.autonomiccs.autonomic.administration.plugin.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.plugin.common.daos.ClusterJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.ClusterAdministrationStatus;

/**
 * Provides methods to use the {@link ClusterJdbcDao} operations,
 * including any logic necessary into operations that use informations from or
 * modify the 'cluster' table.
 */
@Service
public class AutonomicClusterManagementService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long ONE_MINUTE_IN_MILLISECONDS = 60000;
    private static final int NUMBER_OF_MINUTES_BETWEEN_CHECKS = 180;

    @Autowired
    private ClusterJdbcDao clusterDaoJdbc;
    @Autowired
    private ClusterDao clusterDao;

    /**
     * Returns true if the cluster is in {@link ClusterAdministrationStatus#InProgress} state.
     *
     * @param id
     * @return
     */
    public boolean isClusterBeingAdministrated(long id) {
        return ClusterAdministrationStatus.isClusterBeingManaged(clusterDaoJdbc.getClusterAdministrationStatus(id));
    }

    /**
     * Checks if the cluster has been processed recently (according to the
     * administration algorithm management interval). If the host has no last administration value
     * ('last_administration' column is null), then we consider that the cluster
     * has never been processed; therefore, we will put it into the management queue.
     *
     * @param cluster
     * @param algorithm
     * @return true if the cluster can be processed
     */
    public boolean canProcessCluster(long clusterId, ClusterAdministrationHeuristicAlgorithm algorithm) {
        Date lastAdministration = clusterDaoJdbc.getClusterLastAdminstration(clusterId);
        if (lastAdministration == null) {
            return true;
        }
        Calendar lastAdministrationCalendar = Calendar.getInstance();
        lastAdministrationCalendar.setTime(lastAdministration);
        lastAdministrationCalendar.add(Calendar.SECOND, algorithm.getClusterIntervalBetweenConsolidation());
        return lastAdministrationCalendar.before(Calendar.getInstance());
    }

    /**
     * Sets the cluster administration status to {@link ClusterAdministrationStatus#InProgress} using  {@link ClusterJdbcDao#setClusterAdministrationStatus(ClusterConsolidationStatus, long)} method.
     *
     * @param cluster
     */
    @Transactional(readOnly = false)
    public void setClusterWorkInProgress(long clusterId) {
        clusterDaoJdbc.setClusterAdministrationStatus(ClusterAdministrationStatus.InProgress, clusterId);
        logger.debug("Starting the process on cluster = " + clusterId);
    }

    /**
     * Mark the cluster (with the given id) as in
     * {@link ClusterAdministrationStatus#Done} state and sets the last
     * administration to the current Date.
     *
     * @param cluster
     */
    @Transactional(readOnly = false)
    public void markAdministrationStatusInClusterAsDone(long clusterId) {
        clusterDaoJdbc.setClusterLastAdministration(new Date(), clusterId);
        clusterDaoJdbc.setClusterAdministrationStatus(ClusterAdministrationStatus.Done, clusterId);
    }

    @Transactional(readOnly = false)
    @Scheduled(initialDelay = ONE_MINUTE_IN_MILLISECONDS, fixedDelay = ONE_MINUTE_IN_MILLISECONDS * NUMBER_OF_MINUTES_BETWEEN_CHECKS)
    public void removeClusterStuckProcessing() {
        List<ClusterVO> allClusters = clusterDao.listAll();
        if (CollectionUtils.isEmpty(allClusters)) {
            return;
        }
        for (ClusterVO cluster : allClusters) {
            final long clusterId = cluster.getId();
            if (!ClusterAdministrationStatus.isClusterBeingManaged(clusterDaoJdbc.getClusterAdministrationStatus(clusterId))) {
                continue;
            }
            Date lastAdministration = clusterDaoJdbc.getClusterLastAdminstration(clusterId);
            if (lastAdministration == null) {
                markAdministrationStatusInClusterAsDone(clusterId);
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastAdministration);
            cal.add(Calendar.HOUR_OF_DAY, 6);

            Date now = new Date();
            if (cal.getTime().before(now)) {
                markAdministrationStatusInClusterAsDone(clusterId);
            }
        }
    }

}

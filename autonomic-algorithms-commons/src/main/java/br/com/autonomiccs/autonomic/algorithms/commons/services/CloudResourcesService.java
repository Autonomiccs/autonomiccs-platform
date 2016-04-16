package br.com.autonomiccs.autonomic.algorithms.commons.services;


import java.util.List;

import org.springframework.stereotype.Component;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesUp;

/**
 * Provides operations over {@link CloudResources} objects.
 */
@Component
public class CloudResourcesService {

    /**
     * Aggregates the amount of resources from all clusters of objects (
     * {@link ClusterResourcesUp}), also it creates and returns a
     * {@link CloudResources} object.
     *
     * @param clustersResourcesUp
     * @return {@link CloudResources}
     */
    public CloudResources createCloudResources(List<ClusterResourcesUp> clustersResourcesUp) {
        long uMemory = 0, mem = 0, uCpu = 0, cpuFreq = 0;
        int cpuN = 0;
        for (ClusterResourcesUp currentCluster : clustersResourcesUp) {
            uMemory += currentCluster.getUsedMemory();
            mem += currentCluster.getMemoryInBytes();
            uCpu += currentCluster.getUsedCpu();
            cpuFreq += currentCluster.getCpuSpeed();
            cpuN += currentCluster.getCpus();
        }
        return new CloudResources(clustersResourcesUp, uMemory, mem, uCpu, cpuFreq, cpuN);
    }

}

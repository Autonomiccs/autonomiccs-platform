
/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The Autonomiccs, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.autonomic.algorithms.commons.beans;

/**
 * Representation of cluster resources. This class does not considers the hosts in the cluster
 * neither resources usage. For this, {@link ClusterResourcesAvailableToStart} and
 * {@link ClusterResourcesUp} extend it and specialize according their need.
 *
 * @param ClusterVO
 * @param List
 *            of HostResources, every HostResource in this list must be in the cluster.
 */
public abstract class ClusterResources {

    private int cpus;
    private long cpuSpeed, memory, clusterId;
    private String clusterName;
    private double score;

    public long getMemoryInBytes() {
        return memory;
    }
    public void setMemoryInBytes(long memory) {
        this.memory = memory;
    }

    public ClusterResources(long clusterId, String clusterName, long cpuSpeed, int cpus, long memory) {
        this.clusterId = clusterId;
        this.clusterName = clusterName;
        this.cpuSpeed = cpuSpeed;
        this.cpus = cpus;
        this.memory = memory;
    }

    /**
     * @return sum of cpu speed (frequency of each cpu core) of each host in this cluster
     * */
    public long getCpuSpeed() {
        return cpuSpeed;
    }
    public void setCpuSpeed(long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    /**
     * @return sum number of cores in each host in this cluster.
     * */
    public int getCpus() {
        return cpus;
    }
    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "clusterId= " + Long.toString(this.clusterId) + ", usedMemory= " + ", memory= " + Long.toString(this.memory) + ", usedCpu= " + ", cpuSpeed= "
                + Long.toString(this.cpuSpeed) + ", cpus= " + Integer.toString(this.cpus);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

}
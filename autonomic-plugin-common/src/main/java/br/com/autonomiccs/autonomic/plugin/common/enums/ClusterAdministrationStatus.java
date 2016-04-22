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
package br.com.autonomiccs.autonomic.plugin.common.enums;

/**
 * A cluster can be in one of two administration status: {@link #InProgress} or
 * {@link #Done}. That means, the agents has already worked on it, or the agent has already finished its work there.
 */
public enum ClusterAdministrationStatus {

    Done, InProgress;

    /**
     * Returns true if the cluster consolidation status is equals to
     * {@link #InProgress}.
     *
     * @param administrationStatus
     * @return true if the administrationStatus is {@link #InProgress}
     */
    public static boolean isClusterBeingManaged(ClusterAdministrationStatus administrationStatus) {
        return administrationStatus == InProgress;
    }

}

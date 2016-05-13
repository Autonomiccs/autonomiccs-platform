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
package br.com.autonomiccs.autonomic.administration.plugin.hypervisors;

import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;

/**
 * This interface provides all Host Hypervisors basic operations developed by the Autonomiccs
 * platform.
 */
public interface HypervisorHost {

    /**
     * Shutdown a given {@link HostVO}.
     *
     * @param hostVo
     */
    public void shutdownHost(HostVO hostVo);

    /**
     * Returns true if the given {@link HypervisorType} all methods from
     * {@link HypervisorHost}.
     *
     * @param hypervisorType
     * @return
     */
    public boolean supportsHypervisor(HypervisorType hypervisorType);

}

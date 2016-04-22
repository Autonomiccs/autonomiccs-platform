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
package br.com.autonomiccs.autonomic.plugin.common.pojos;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.vm.VMInstanceVO;

@Entity
@SuppressWarnings("serial")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue(value = "AutonomiccsSystemVm")
public class AutonomiccsSystemVm extends VMInstanceVO {

    public AutonomiccsSystemVm(long id, long serviceOfferingId, String name, long templateId, HypervisorType hypervisorType, long guestOSId, long dataCenterId, long domainId,
            long accountId, long userId, boolean haEnabled) {
        super(id, serviceOfferingId, name, name, Type.Instance, templateId, hypervisorType, guestOSId, domainId, accountId, userId, haEnabled);
    }

    protected AutonomiccsSystemVm() {
        super();
    }

    @Column(name = "public_ip_address", nullable = false)
    private String publicIpAddress;

    @Column(name = "management_ip_address", nullable = false)
    private String managementIpAddress;

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getManagementIpAddress() {
        return managementIpAddress;
    }

    public void setManagementIpAddress(String managementIpAddress) {
        this.managementIpAddress = managementIpAddress;
    }

}

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
package br.com.autonomiccs.starthost.plugin.proxies;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloud.host.HostVO;

import br.com.autonomiccs.autonomic.plugin.common.services.HostService;

@Component
public class ScanDirectAgentToLoadMethodInterceptor implements MethodInterceptor, InitializingBean {

    private static ScanDirectAgentToLoadMethodInterceptor scanDirectAgentToLoadMethodInterceptor;

    @Autowired
    private HostService hostService;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object result = methodInvocation.proceed();
        if(result == null){
            return result;
        }
        @SuppressWarnings("unchecked")
        List<HostVO> hosts = (List<HostVO>)result;
        if (CollectionUtils.isEmpty(hosts)) {
            return hosts;
        }
        List<HostVO> onlyActiveHosts = new ArrayList<>();
        for (HostVO host : hosts) {
            if(hostService.isHostDown(host.getId())){
                continue;
            }
            onlyActiveHosts.add(host);
        }
        return onlyActiveHosts;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scanDirectAgentToLoadMethodInterceptor = this;
    }

    public static ScanDirectAgentToLoadMethodInterceptor getScanDirectAgentToLoadMethodInterceptor() {
        return scanDirectAgentToLoadMethodInterceptor;
    }

}

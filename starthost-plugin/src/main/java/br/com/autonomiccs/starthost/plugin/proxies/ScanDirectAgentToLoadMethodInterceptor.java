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
        List<HostVO> onlyActiveHosts = new ArrayList<HostVO>();
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

package br.com.autonomiccs.starthost.plugin.proxies;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class ScanDirectAgentToLoadMethodInterceptorWorkAroundForProxy implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return ScanDirectAgentToLoadMethodInterceptor.getScanDirectAgentToLoadMethodInterceptor().invoke(invocation);
    }

}

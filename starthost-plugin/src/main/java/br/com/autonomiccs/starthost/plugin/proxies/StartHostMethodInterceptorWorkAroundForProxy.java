package br.com.autonomiccs.starthost.plugin.proxies;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class StartHostMethodInterceptorWorkAroundForProxy implements MethodInterceptor, InitializingBean {

    private final Logger logger = Logger.getLogger(getClass());

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Start host proxy workaround initialized.");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return StartHostMethodInterceptor.getStartHostMethodInterceptor().invoke(invocation);
    }

}

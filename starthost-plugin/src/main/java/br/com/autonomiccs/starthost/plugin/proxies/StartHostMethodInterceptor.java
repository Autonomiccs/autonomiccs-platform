package br.com.autonomiccs.starthost.plugin.proxies;

import java.util.UUID;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMEntityVO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.host.HostVO;

import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;
import br.com.autonomiccs.starthost.plugin.services.StartHostService;

@Component
public class StartHostMethodInterceptor implements MethodInterceptor, InitializingBean {

    private final Logger logger = Logger.getLogger(getClass());

    private static StartHostMethodInterceptor startHostMethodInterceptor;

    @Autowired
    private StartHostService startHostService;

    @Autowired
    private AutonomicClusterManagementHeuristicService autonomicClusterManagementHeuristicService;

    @Autowired
    private ThreadUtils threadUtils;

    @Autowired
    private HostService hostService;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        UUID uuid = UUID.randomUUID();
        logger.debug(String.format("In proxy method before the method [%s] execution, we will use the idenfier [%s] for debug purpose.", methodInvocation.getMethod().getName(), uuid));
        logger.debug(String.format("Parameters before request [%s] execution, parameters [%s]", uuid, ArrayUtils.toString(methodInvocation.getArguments())));
        try {
            return methodInvocation.proceed();
        } catch (Exception e) {
            logger.info(String.format("Dealing with exception [%s] for method [%s], UUID of the call [%s].", e.getClass(), methodInvocation.getMethod().getName(), uuid));
            if (!hostService.isThereAnyHostOnCloudDeactivatedByOurManager()
                    && !autonomicClusterManagementHeuristicService.getAdministrationAlgorithm().canHeuristicShutdownHosts()) {
                throw e;
            }
            return synchronizedExecuteDeployVMStartingHostIfNeeded(methodInvocation);
        }
    }

    private synchronized Object synchronizedExecuteDeployVMStartingHostIfNeeded(MethodInvocation methodInvocation) throws Throwable {
        return internalExecuteDeployVMStartingHostIfNeeded(methodInvocation);
    }

    private Object internalExecuteDeployVMStartingHostIfNeeded(MethodInvocation methodInvocation) throws Throwable {
        try {
            whipeExcludeList(methodInvocation);
            return methodInvocation.proceed();
        } catch (Exception e) {
            if (!shouldTryToStartHost(e)) {
                throw e;
            }
            VMEntityVO vmEntityVO = getVmEntityVoFromMethodArguments(methodInvocation);
            HostVO startedHost = startHostService.startHost(vmEntityVO, e);
            if (startedHost == null) {
                throw e;
            }
            prepareHostToReceiveVms(startedHost);
            return internalExecuteDeployVMStartingHostIfNeeded(methodInvocation);
        }
    }

    private void whipeExcludeList(MethodInvocation methodInvocation) {
        Object[] arguments = methodInvocation.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof ExcludeList) {
                arguments[i] = new ExcludeList();
            }
        }
    }

    private void prepareHostToReceiveVms(HostVO startedHost) {
        if (waitUntilTheHostStatusIsUp(startedHost)) {
            startHostService.prepareHostToReceiveVms(startedHost);
            return;
        }
        startHostService.markHostAsFailedToStart(startedHost);
    }

    /**
     * Waits until the host status is 'Up' in database. It tries 50 times, each
     * retry waits 5 seconds (maximum of 250 seconds of waiting). If the status
     * is updated to 'Up' returns true.
     *
     * @param hostVO
     * @return
     */
    private boolean waitUntilTheHostStatusIsUp(HostVO hostVO) {
        for (int tries = 0; tries < 200; tries++) {
            if (startHostService.isHostStatusUpInDataBase(hostVO)) {
                logger.debug(String.format("Host[%d] status is up in database.", hostVO.getId()));
                return true;
            }
            threadUtils.sleepThread(3);
        }
        return false;
    }

    private VMEntityVO getVmEntityVoFromMethodArguments(MethodInvocation methodInvocation) {
        Object[] arguments = methodInvocation.getArguments();
        if (!ArrayUtils.isEmpty(arguments)) {
            for (Object o : arguments) {
                if (o instanceof VMEntityVO) {
                    return (VMEntityVO)o;
                }
            }
        }
        throw new RuntimeException("Could not find VMEntityVo from method arguments");
    }

    /**
     * Verifies the exception, if it is an
     * {@link InsufficientServerCapacityException} returns true. If it is not an
     * {@link InsufficientServerCapacityException}, it will check if the
     * exception message is equal to
     * "Unable to start a VM due to insufficient capacity"
     *
     * @param e
     * @return
     */
    private boolean shouldTryToStartHost(Exception e) {
        if (e instanceof InsufficientServerCapacityException) {
            return true;
        }
        return "Unable to start a VM due to insufficient capacity".equals(e.getMessage());
    }

    public static StartHostMethodInterceptor getStartHostMethodInterceptor() {
        return startHostMethodInterceptor;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Start host proxy initialized.");
        startHostMethodInterceptor = this;
    }

}

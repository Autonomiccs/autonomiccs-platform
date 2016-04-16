package br.com.autonomiccs.autonomic.plugin.common.daos;

import org.springframework.stereotype.Component;

import com.cloud.utils.db.GenericDaoBase;

import br.com.autonomiccs.autonomic.plugin.common.pojos.CleverCloudsSystemVm;

/**
 * This DAO is meant to be used to execute a similar flow as the deployment of system VMs in CloudStack, to deploy clever clouds system VMs
 */
@Component
public class CleverCloudSystemVmDao extends GenericDaoBase<CleverCloudsSystemVm, Long> {

}

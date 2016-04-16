package br.com.autonomiccs.autonomic.plugin.common.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsVmTemplateJdbcDao;

/**
 * This class is responsible to manage the Autonomiccs System VMs templates
 */
@Service
public class AutonomiccsVmTemplateService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AutonomiccsVmTemplateJdbcDao autonomiccsSystemVmTemplateJdbcDao;

    @Autowired
    private VMTemplateDao templateDao;

    /**
     * the base URL used to construct the link from which we download the Autonomiccs system VM template
     */
    @Autowired
    @Qualifier("autonomiccsSystemVmsTemplateUrlBase")
    private String autonomiccsSystemVmsTemplateUrlBase;

    /**
     * Version of the system VM that is being installed.
     */
    @Autowired
    @Qualifier("systemVmTemplateVersion")
    private String systemVmTemplateVersion;

    /**
     * Display text used in the System VM.
     */
    private String autonomiccsSystemVmTemplateDisplayText;

    /**
     * The Autonomiccs system VM template unique name identifier.
     */
    private String autonomiccsSystemVmTemplateName;

    /**
     * Checks if the template is registered and active in the database.
     *
     * @param templateName
     * @return true if the template is already registered
     */
    private boolean isTemplateRegistered(String templateName) {
        return autonomiccsSystemVmTemplateJdbcDao.isTemplateRegistered(templateName);
    }

    /**
     * Checks is there is a template registered for a given hypervisor
     *
     * @param hypervisorType
     * @return boolean indicating if there is a template registered for a given hypervisor
     */
    public boolean isTemplateRegisteredForHypervisor(HypervisorType hypervisorType) {
        return isTemplateRegistered(getAutonomiccsSystemVmTemplateName(hypervisorType));
    }

    public String getAutonomiccsSystemVmTemplateDisplayText(HypervisorType hypervisor) {
        return String.format("%s - %s", autonomiccsSystemVmTemplateDisplayText, hypervisor.name().toLowerCase());
    }

    public String getAutonomiccsSystemVmTemplateName(HypervisorType hypervisor) {
        return String.format("%s-%s", autonomiccsSystemVmTemplateName, hypervisor.name().toLowerCase());
    }

    /**
     * It constructs the System VMs template URL for download; to do that, it uses the {@link #autonomiccsSystemVmsTemplateUrlBase},
     * {@link #getAutonomiccsSystemVmTemplateName(hypervisorType)}
     * and the given hypervisor name
     *
     * @param hypervisorType
     * @return SystemVms template URL
     */
    private String constructSystemVmTemplateUrl(HypervisorType hypervisorType) {
        return String.format("%s/%s.%s", autonomiccsSystemVmsTemplateUrlBase, getAutonomiccsSystemVmTemplateName(hypervisorType),
                HypervisorType.getSupportedImageFormat(hypervisorType).getFileExtension());
    }

    /**
     * Retrieves the URL to download the System VM template for the given hypervisor type.
     * If the hypervisor type is not supported a {@link RuntimeException} will be thrown.
     *
     * @param hypervisor
     * @return URL to download the system VM
     */
    public String getSystemVmTemplateUrl(HypervisorType hypervisor) {
        switch (hypervisor) {
        case XenServer:
        case KVM:
            return constructSystemVmTemplateUrl(hypervisor);
        default:
            throw new RuntimeException(String.format("We do not have a Autonomiccs System VM template for the given Hypervisor [%s] .", hypervisor));
        }
    }

    /**
     * It looks for the {@link VMTemplateVO} that represents the template of the system VM for the given hypervisor
     *
     * @param hypervisor
     * @return {@link VMTemplateVO} that represents the Autonomiccs system VM for the given hypervisor
     */
    public VMTemplateVO findAutonomiccsSystemVmTemplate(HypervisorType hypervisor) {
        String autonomiccsSystemVmTemplateName = getAutonomiccsSystemVmTemplateName(hypervisor);
        long templateId = autonomiccsSystemVmTemplateJdbcDao.searchAutonomiccsSystemVmTemplateIdForHypervisor(autonomiccsSystemVmTemplateName);
        return templateDao.findById(templateId);
    }

    @Override
    /**
     * This method is executed by Spring framework.
     * It configures the template's display text and name
     */
    public void afterPropertiesSet() throws Exception {
        logger.info("Autonomiccs system VMs template service initialized.");

        autonomiccsSystemVmTemplateDisplayText = String.format("Autonomiccs System VM template (%s)", systemVmTemplateVersion);
        logger.debug(String.format("Autonomiccs system VMs template display text configured [%s]", autonomiccsSystemVmTemplateDisplayText));

        autonomiccsSystemVmTemplateName = String.format("autonomiccs-systemVm-%s", systemVmTemplateVersion);
        logger.debug(String.format("Autonomiccs system VMs template name configured [%s]", systemVmTemplateVersion));
    }

    public boolean isTemplateRegisteredAndReadyForHypervisor(HypervisorType hypervisorType) {
        return autonomiccsSystemVmTemplateJdbcDao.isTemplateRegisteredAndReady(getAutonomiccsSystemVmTemplateName(hypervisorType));
    }

}

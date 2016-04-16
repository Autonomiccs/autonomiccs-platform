package br.com.autonomiccs.autonomic.plugin.common.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.CleverCloudSystemVmTemplateJdbcDao;

/**
 * This class is responsible to manage the Clever Clouds System VMs templates
 */
@Service
public class CleverCloudSystemVmTemplateService implements InitializingBean {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private CleverCloudSystemVmTemplateJdbcDao cleverCloudSystemVmTemplateJdbcDao;

    @Autowired
    private VMTemplateDao templateDao;

    /**
     * the base URL used to construct the link from which we download the Clever Clouds system VM template
     */
    @Autowired
    @Qualifier("cleverCloudSystemVmsTemplateUrlBase")
    private String cleverCloudSystemVmsTemplateUrlBase;

    /**
     * Version of the system VM that is being installed.
     */
    @Autowired
    @Qualifier("systemVmTemplateVersion")
    private String systemVmTemplateVersion;

    /**
     * Display text used in the System VM.
     */
    private String cleverCloudsSystemVmTemplateDisplayText;

    /**
     * The Clever Clouds system VM template unique name identifier.
     */
    private String cleverCloudsSystemVmTemplateName;

    /**
     * Checks if the template is registered and active in the database.
     *
     * @param templateName
     * @return true if the template is already registered
     */
    private boolean isTemplateRegistered(String templateName) {
        return cleverCloudSystemVmTemplateJdbcDao.isTemplateRegistered(templateName);
    }

    /**
     * Checks is there is a template registered for a given hypervisor
     *
     * @param hypervisorType
     * @return boolean indicating if there is a template registered for a given hypervisor
     */
    public boolean isTemplateRegisteredForHypervisor(HypervisorType hypervisorType) {
        return isTemplateRegistered(getCleverCloudSystemVmTemplateName(hypervisorType));
    }

    public String getCleverCloudsSystemVmTemplateDisplayText(HypervisorType hypervisor) {
        return String.format("%s - %s", cleverCloudsSystemVmTemplateDisplayText, hypervisor.name().toLowerCase());
    }

    public String getCleverCloudSystemVmTemplateName(HypervisorType hypervisor) {
        return String.format("%s-%s", cleverCloudsSystemVmTemplateName, hypervisor.name().toLowerCase());
    }

    /**
     * It constructs the System VMs template URL for download; to do that, it uses the {@link #cleverCloudSystemVmsTemplateUrlBase},
     * {@link #getCleverCloudSystemVmTemplateName(hypervisorType)}
     * and the given hypervisor name
     *
     * @param hypervisorType
     * @return SystemVms template URL
     */
    private String constructSystemVmTemplateUrl(HypervisorType hypervisorType) {
        return String.format("%s/%s.%s", cleverCloudSystemVmsTemplateUrlBase, getCleverCloudSystemVmTemplateName(hypervisorType),
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
            throw new RuntimeException(String.format("We do not have a Clever Cloud System VM template for the given Hypervisor [%s] .", hypervisor));
        }
    }

    /**
     * It looks for the {@link VMTemplateVO} that represents the template of the system VM for the given hypervisor
     *
     * @param hypervisor
     * @return {@link VMTemplateVO} that represents the Clever clouds system VM for the given hypervisor
     */
    public VMTemplateVO findCleverCloudSystemVmTemplate(HypervisorType hypervisor) {
        String cleverCloudSystemVmTemplateName = getCleverCloudSystemVmTemplateName(hypervisor);
        long templateId = cleverCloudSystemVmTemplateJdbcDao.searchCleverCloudsSystemVmTemplateIdForHypervisor(cleverCloudSystemVmTemplateName);
        return templateDao.findById(templateId);
    }

    @Override
    /**
     * This method is executed by Spring framework.
     * It configures the template's display text and name
     */
    public void afterPropertiesSet() throws Exception {
        logger.info("Clever cloud system VMs template service initialized.");

        cleverCloudsSystemVmTemplateDisplayText = String.format("Clever Clouds System VM template (%s)", systemVmTemplateVersion);
        logger.debug(String.format("Clever Clouds system VMs template display text configured [%s]", cleverCloudsSystemVmTemplateDisplayText));

        cleverCloudsSystemVmTemplateName = String.format("cleverClouds-systemVm-%s", systemVmTemplateVersion);
        logger.debug(String.format("Clever Clouds system VMs template name configured [%s]", systemVmTemplateVersion));
    }

    public boolean isTemplateRegisteredAndReadyForHypervisor(HypervisorType hypervisorType) {
        return cleverCloudSystemVmTemplateJdbcDao.isTemplateRegisteredAndReady(getCleverCloudSystemVmTemplateName(hypervisorType));
    }

}

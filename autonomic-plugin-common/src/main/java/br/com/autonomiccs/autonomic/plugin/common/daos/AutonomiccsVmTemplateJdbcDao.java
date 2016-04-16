package br.com.autonomiccs.autonomic.plugin.common.daos;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class AutonomiccsVmTemplateJdbcDao extends JdbcDaoSupport {

    private String sqlIsTemplateRegistered = "select count(id) from vm_template where removed is null and state <> 'Inactive' and name = ?";

    private String sqlIsTemplateRegisteredAndReady = "select count(id) from template_view where state = 'READY' and removed is null and name = ?";

    private String sqlSearchAutonomiccsSystemVmTemplateIdForHypervisor = "select id from template_view where state = 'READY' and removed is null and name = ?";

    /**
     * Checks if the template name is registered into the database.
     *
     * @param templateName
     * @return true if the template name is found in our database.
     */
    public boolean isTemplateRegistered(String templateName) {
        return executeTemplateQueryAndRetrieveBoolean(templateName, sqlIsTemplateRegistered);
    }

    private boolean executeTemplateQueryAndRetrieveBoolean(String templateName, String sql) {
        Integer numberOfRegister = getJdbcTemplate().queryForObject(sql, new Object[] {templateName}, Integer.class);
        if (numberOfRegister > 1) {
            throw new RuntimeException(String.format("More than one template with name [%s]", templateName));
        }
        return BooleanUtils.toBoolean(numberOfRegister);
    }

    public boolean isTemplateRegisteredAndReady(String autonomiccsSystemVmTemplateName) {
        return executeTemplateQueryAndRetrieveBoolean(autonomiccsSystemVmTemplateName, sqlIsTemplateRegisteredAndReady);
    }

    public long searchAutonomiccsSystemVmTemplateIdForHypervisor(String autonomiccsSystemVmTemplateName) {
        return getJdbcTemplate().queryForObject(sqlSearchAutonomiccsSystemVmTemplateIdForHypervisor, Long.class, autonomiccsSystemVmTemplateName);
    }
}

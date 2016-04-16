package br.com.autonomiccs.autonomic.plugin.common.daos;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Executed the access to the "gues_os" data table
 */
public class GuestOsJdbcDao extends JdbcDaoSupport {

    /**
     * SQL to retrieve the Guest OS ID.
     */
    private String sqlGetGuestOsId = "select id from guest_os where display_name = ?";

    /**
     * Retrieves the ID of a given Guest OS name
     *
     * @param guesOsName
     * @return guest OS ID
     */
    public Long getGuestOsUuid(String guesOsName) {
        return getJdbcTemplate().queryForObject(sqlGetGuestOsId, Long.class, guesOsName);
    }
}

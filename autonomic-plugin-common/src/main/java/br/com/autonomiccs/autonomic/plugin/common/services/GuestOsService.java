package br.com.autonomiccs.autonomic.plugin.common.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.autonomiccs.autonomic.plugin.common.daos.GuestOsJdbcDao;

/**
 * This class is meant to deal with Guest Operating systems configurations.
 */
@Service
public class GuestOsService {

    @Autowired
    private GuestOsJdbcDao guestOsJdbcDao;

    /**
     * Retrieves from the database the Guest OS ID.
     *
     * @param guestOsName
     * @return guestOs ID
     */
    public Long getGuestOsUuid(String guestOsName) {
        return this.guestOsJdbcDao.getGuestOsUuid(guestOsName);
    }

}

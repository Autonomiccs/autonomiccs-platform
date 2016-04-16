package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;

/**
 * This class is intended to manage Zones configurations.
 */
@Service
public class ZoneService {

    @Autowired
    private DataCenterDao dataCenterDao;

    /**
     * It will list all of the enabled zones of the cloud environment.
     *
     * @return List<DataCenterVO> that represents of of the enabled zones in the cloud
     */
    public List<DataCenterVO> listAllZonesEnabled() {
        return dataCenterDao.listEnabledZones();
    }
}

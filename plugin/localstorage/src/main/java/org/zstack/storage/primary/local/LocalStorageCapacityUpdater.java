package org.zstack.storage.primary.local;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.header.storage.primary.PrimaryStorageCapacityUpdaterRunnable;
import org.zstack.header.storage.primary.PrimaryStorageCapacityVO;
import org.zstack.storage.primary.PrimaryStorageCapacityUpdater;
import org.zstack.storage.primary.local.LocalStorageKvmBackend.AgentResponse;
import org.zstack.utils.Utils;
import org.zstack.utils.logging.CLogger;

import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by frank on 11/10/2015.
 */
@Configurable(preConstruction = true, autowire = Autowire.BY_TYPE)
public class LocalStorageCapacityUpdater {
    private static CLogger logger = Utils.getLogger(LocalStorageCapacityUpdater.class);

    @Autowired
    private DatabaseFacade dbf;

    @Transactional
    private LocalStorageHostRefVO updateLocalStorageRef(String psUuid, String hostUuid, AgentResponse rsp) {
        String sqlLocalStorageHostRefVO = "select ref" +
                " from LocalStorageHostRefVO ref" +
                " where ref.hostUuid = :hostUuid" +
                " and ref.primaryStorageUuid = :primaryStorageUuid";
        TypedQuery<LocalStorageHostRefVO> query = dbf.getEntityManager().
                createQuery(sqlLocalStorageHostRefVO, LocalStorageHostRefVO.class);
        query.setParameter("hostUuid", hostUuid);
        query.setParameter("primaryStorageUuid", psUuid);
        List<LocalStorageHostRefVO> refs = query.getResultList();
        if (refs.isEmpty()) {
            return null;
        }

        LocalStorageHostRefVO ref = refs.get(0);
        CompositePrimaryKeyForLocalStorageHostRefVO id = new CompositePrimaryKeyForLocalStorageHostRefVO();
        id.setHostUuid(ref.getHostUuid());
        id.setPrimaryStorageUuid(ref.getPrimaryStorageUuid());

        ref = dbf.getEntityManager().find(LocalStorageHostRefVO.class, id, LockModeType.PESSIMISTIC_WRITE);

        if (ref.getAvailablePhysicalCapacity() == rsp.getAvailableCapacity()
                && ref.getTotalPhysicalCapacity() == rsp.getTotalCapacity()) {
            return null;
        }

        long originalPhysicalTotal = ref.getTotalPhysicalCapacity();
        long originalPhysicalAvailable = ref.getAvailablePhysicalCapacity();

        ref.setTotalPhysicalCapacity(rsp.getTotalCapacity());
        ref.setAvailablePhysicalCapacity(rsp.getAvailableCapacity());
        dbf.getEntityManager().merge(ref);

        if (logger.isTraceEnabled()) {
            logger.trace(String.format("[Local Storage Capacity] changed the physical capacity of the host[uuid:%s] of " +
                            "the local primary storage[uuid:%s] as:\n" +
                            "physical total: %s --> %s\n" +
                            "physical available: %s --> %s\n",
                    hostUuid, psUuid, originalPhysicalTotal, ref.getTotalPhysicalCapacity(),
                    originalPhysicalAvailable, ref.getAvailablePhysicalCapacity()));
        }

        return ref;
    }

    public void updatePhysicalCapacityByKvmAgentResponse(String psUuid, String hostUuid, AgentResponse rsp) {
        LocalStorageHostRefVO ref = updateLocalStorageRef(psUuid, hostUuid, rsp);
        if (ref == null) {
            return;
        }

        final long totalChange = rsp.getTotalCapacity() - ref.getTotalPhysicalCapacity();
        final long availChange = rsp.getAvailableCapacity() - ref.getAvailablePhysicalCapacity();

        new PrimaryStorageCapacityUpdater(psUuid).run(new PrimaryStorageCapacityUpdaterRunnable() {
            @Override
            public PrimaryStorageCapacityVO call(PrimaryStorageCapacityVO cap) {
                cap.setTotalPhysicalCapacity(cap.getTotalPhysicalCapacity() + totalChange);
                cap.setAvailablePhysicalCapacity(cap.getAvailablePhysicalCapacity() + availChange);
                return cap;
            }
        });
    }
}

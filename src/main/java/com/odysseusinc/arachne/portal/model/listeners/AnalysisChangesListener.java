package com.odysseusinc.arachne.portal.model.listeners;

import com.odysseusinc.arachne.portal.config.tenancy.TenantContext;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import java.util.Date;

public class AnalysisChangesListener {

    @PreUpdate
    @PrePersist
    @PreRemove
    public void update(Object entity) {

        if (TenantContext.getCurrentTenant() != null) {
            if (entity instanceof Analysis) {
                ((Analysis) entity).getStudy().setUpdated(new Date());
            } else if (entity instanceof AnalysisFile) {
                ((AnalysisFile) entity).getAnalysis().getStudy().setUpdated(new Date());
            }
        }
    }
}
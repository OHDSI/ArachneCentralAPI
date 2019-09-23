package com.odysseusinc.arachne.portal.db.migration;

import com.google.gson.JsonElement;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringMigration;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.util.SubmissionHelper;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class V20190923174625__IncidenceRateResultInfo implements ApplicationContextAwareSpringMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger(V20190923174625__IncidenceRateResultInfo.class);

    private final BaseAnalysisService<Analysis> analysisService;
    private final SubmissionRepository submissionRepository;
    private final SubmissionHelper submissionHelper;
    private final TransactionTemplate transactionTemplate;

    public V20190923174625__IncidenceRateResultInfo(BaseAnalysisService<Analysis> analysisService,
                                                    SubmissionRepository submissionRepository,
                                                    SubmissionHelper submissionHelper,
                                                    TransactionTemplate transactionTemplate) {

        this.analysisService = analysisService;
        this.submissionRepository = submissionRepository;
        this.submissionHelper = submissionHelper;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void migrate() throws Exception {

        List<Analysis> analyses = analysisService.findByType(CommonAnalysisType.INCIDENCE);
        LOGGER.info("Migrate Incidence Rate analyses ResultInfo, found: {} analyses", analyses.size());
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                analyses.forEach(analysis -> {
                    List<Submission> submissions = analysis.getSubmissions();
                    submissions.forEach(s -> {
                        JsonElement resultInfo = s.getResultInfo();
                        if (Objects.nonNull(resultInfo) && resultInfo.isJsonObject()) {
                            submissionHelper.updateSubmissionExtendedInfo(s);
                            submissionRepository.save(s);
                        }
                    });
                });
            }
        });
        LOGGER.info("Incidence Rate ResultInfo migrated");
    }
}
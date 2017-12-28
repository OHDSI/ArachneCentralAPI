package com.odysseusinc.arachne.portal.db.migration;

import com.odysseusinc.arachne.portal.config.flyway.ApplicationContextAwareSpringMigration;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class V20171219232521__ResultFilesToJcr implements ApplicationContextAwareSpringMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger(V20171219232521__ResultFilesToJcr.class);
    private static final String SUBMISSIONS_TABLE = "submissions";
    private static final String RESULT_FILES_DIR = "results";

    @Value("${files.store.path}")
    private String legacyFilesDir;

    private JdbcTemplate jdbcTemplate;
    private ContentStorageService contentStorageService;

    @Autowired
    public V20171219232521__ResultFilesToJcr(JdbcTemplate jdbcTemplate, ContentStorageService contentStorageService) {

        this.jdbcTemplate = jdbcTemplate;
        this.contentStorageService = contentStorageService;
    }

    @Override
    public void migrate() throws Exception {

        String queryNotMigratedFilesSql = "SELECT rf.id, \n" +
                "rf.legacy_uuid, \n" +
                "rf.submission_id, \n" +
                "rf.legacy_real_name, \n" +
                "rf.legacy_manual_upload, \n" +
                "'" + legacyFilesDir + "' || '/content/' || s.id || '/' || a.id || '/sg_' || sg.id || '/' || sm.id || '/result/' || rf.legacy_uuid AS legacy_path \n" +
                "FROM result_files rf \n" +
                "   JOIN submissions sm ON sm.id = rf.submission_id \n" +
                "   JOIN submission_groups sg ON sg.id = sm.submission_group_id \n" +
                "   JOIN analyses a ON a.id = sg.analysis_id \n" +
                "   JOIN studies s ON s.id = a.study_id \n" +
                "WHERE rf.legacy_real_name IS NOT NULL \n" +
                "AND rf.path IS NULL";

        List<LegacyResultFile> resultList = new ArrayList<>();

        jdbcTemplate.query(queryNotMigratedFilesSql, rs -> {

            LegacyResultFile lrf = new LegacyResultFile();

            lrf.setId(rs.getLong("id"));
            lrf.setUuid(rs.getString("legacy_uuid"));
            lrf.setSubmissionId(rs.getLong("submission_id"));
            lrf.setRelativeFilename(rs.getString("legacy_real_name"));
            lrf.setManualUpload(rs.getBoolean("legacy_manual_upload"));
            lrf.setLegacyPath(rs.getString("legacy_path"));

            resultList.add(lrf);
        });

        for (LegacyResultFile lrf : resultList) {

            File file = new File(lrf.getLegacyPath());

            if (file.exists()) {
                String targetPath = getResultFilesDir(lrf.getSubmissionId(), lrf.getRelativeFilename().replace('\\', '/'));
                ArachneFileMeta meta = migrateFile(targetPath, file, lrf.getManualUpload() ? 1L : null);
                updateResultFile(lrf, meta);
            } else {
                LOGGER.error("File not found: " + file.getAbsolutePath());
            }
        }
    }

    private String getResultFilesDir(Long submissionId, String relativePath) {

        return contentStorageService.getLocationForEntity(SUBMISSIONS_TABLE, submissionId, Arrays.asList(new String[]{RESULT_FILES_DIR, relativePath}));
    }

    private ArachneFileMeta migrateFile(String path, File file, Long createdBy) {

        return contentStorageService.saveFile(file, path, createdBy);
    }

    private void updateResultFile(LegacyResultFile lrf, ArachneFileMeta migratedFileMeta) {

        jdbcTemplate.execute(
                "UPDATE result_files " +
                        "SET uuid = '" + migratedFileMeta.getUuid() + "', path = '" + migratedFileMeta.getPath() + "' " +
                        "WHERE id = " + lrf.getId().toString());
    }

    class LegacyResultFile {

        private Long id;
        private String uuid;
        private Long submissionId;
        private String relativeFilename;
        private Boolean manualUpload;
        private String legacyPath;

        public Long getId() {

            return id;
        }

        public void setId(Long id) {

            this.id = id;
        }

        public String getUuid() {

            return uuid;
        }

        public void setUuid(String uuid) {

            this.uuid = uuid;
        }

        public Long getSubmissionId() {

            return submissionId;
        }

        public void setSubmissionId(Long submissionId) {

            this.submissionId = submissionId;
        }

        public String getRelativeFilename() {

            return relativeFilename;
        }

        public void setRelativeFilename(String relativeFilename) {

            this.relativeFilename = relativeFilename;
        }

        public Boolean getManualUpload() {

            return manualUpload;
        }

        public void setManualUpload(Boolean manualUpload) {

            this.manualUpload = manualUpload;
        }

        public String getLegacyPath() {

            return legacyPath;
        }

        public void setLegacyPath(String legacyPath) {

            this.legacyPath = legacyPath;
        }
    }
}
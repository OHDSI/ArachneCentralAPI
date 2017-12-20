package com.odysseusinc.arachne.portal.config.flyway;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.MigrationExecutor;

import java.sql.Connection;

/**
 * Adapter for executing migrations implementing ApplicationContextAwareSpringMigration.
 */
public class ApplicationContextAwareSpringJdbcMigrationExecutor implements MigrationExecutor {
    /**
     * The ApplicationContextAwareSpringMigration to execute.
     */
    private final ApplicationContextAwareSpringMigration springJdbcMigration;

    /**
     * Creates a new ApplicationContextAwareSpringMigration.
     *
     * @param springJdbcMigration The Application Context Aware Spring Jdbc Migration to execute.
     */
    public ApplicationContextAwareSpringJdbcMigrationExecutor(ApplicationContextAwareSpringMigration springJdbcMigration) {
        this.springJdbcMigration = springJdbcMigration;
    }

    @Override
    public void execute(Connection connection) {
        try {
            springJdbcMigration.migrate();
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean executeInTransaction() {
        return true;
    }
}

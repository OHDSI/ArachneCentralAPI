package com.odysseusinc.arachne.portal.config.flyway;

/**
 * Interface to be implemented by Spring Autowired Java Migrations. By default the migration version and description will be extracted
 * from the class name. This can be overridden by also implementing the {@link org.flywaydb.core.api.migration.MigrationInfoProvider}
 * interface, in which case it can be specified programmatically. The checksum of this migration (for validation) will also
 * be null, unless the migration also implements the {@link org.flywaydb.core.api.migration.MigrationChecksumProvider},
 * in which case it can be returned programmatically.
 *
 * <p>When the JdbcMigration implements {@link org.flywaydb.core.api.configuration.ConfigurationAware},
 * the master {@link org.flywaydb.core.api.configuration.FlywayConfiguration} is automatically injected upon creation,
 * which is especially useful for getting placeholder and schema information.</p>
 *
 * It is encouraged not to implement this interface directly and subclass {@link BaseApplicationContextAwareSpringMigration} instead.
 */
public interface ApplicationContextAwareSpringMigration {
    /**
     * Executes this migration. The execution will automatically take place within a transaction, when the underlying
     * database supports it.
     *
     * @throws Exception when the migration failed.
     */
    void migrate() throws Exception;
}
package com.odysseusinc.arachne.portal;

import static org.testcontainers.containers.PostgreSQLContainer.IMAGE;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.io.IOException;
import java.util.stream.Stream;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SolrContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private final static String CONTAINER_CONFIG_PATH = "/opt/solr/portal-configs";

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse(IMAGE).withTag("9.6.12");
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE);
    static SolrContainer solr;

    static {
        solr = new SolrContainer(DockerImageName.parse("solr").withTag("7.2.1")) {
            @Override
            protected void configure() {
                addExposedPort(SOLR_PORT);
                setCommand("solr start -c -f");
            }

            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                super.containerIsStarted(containerInfo);
                Stream.of(
                        "users", "data-sources", "studies", "analyses", "analysis-files", "paper-protocols", "paper-files", "submissions", "insights", "result-files", "study-files", "papers"
                ).forEach(name ->
                        exec("solr", "create_collection", "-c", name, "-n", "arachne-config")
                );
                exec("solr", "zk", "upconfig", "-n", "arachne-config", "-d", "portal-configs", "-z", "localhost:9983");
            }

            private void exec(String... args) {
                try {
                    ExecResult result = execInContainer(args);
                    if (result.getExitCode() != 0) {
                        throw new IllegalStateException("Unable to create solr core:\nStdout: " + result.getStdout() + "\nStderr:" + result.getStderr());
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }


        }
                .withCopyFileToContainer(MountableFile.forHostPath("solr_config", 775), "/opt/solr/server/solr/configsets/portal-configs")
        ;
    }

    private static void startContainers() {
        Startables.deepStart(Stream.of(
                postgres,
                solr
        )).join();
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        startContainers();
        MapPropertySource testcontainers = new MapPropertySource("testcontainers", ImmutableMap.of(
                "spring.datasource.url", postgres.getJdbcUrl(),
                "spring.datasource.username", postgres.getUsername(),
                "spring.datasource.password", postgres.getPassword(),
                "arachne.solrServerUrl", "http://localhost:" + String.valueOf(solr.getSolrPort()).trim() + "/solr"
        ));
        context.getEnvironment().getPropertySources().addFirst(testcontainers);
    }
}

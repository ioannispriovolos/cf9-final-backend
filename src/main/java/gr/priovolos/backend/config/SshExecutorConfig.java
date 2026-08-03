package gr.priovolos.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spring configuration class responsible for creating the thread pool used
 * for concurrent SSH command execution.
 *
 * <p>The configured {@link ExecutorService} is shared by the SSH execution
 * service to execute commands on multiple network devices in parallel.</p>
 *
 * <p>The maximum number of concurrent SSH connections is configurable
 * through {@link SshProperties}, allowing the application to control
 * resource consumption while improving execution performance.</p>
 *
 * <p>The thread pool is automatically shut down when the Spring
 * application context is closed.</p>
 *
 * @author Ioannis Priovolos
 */
@Configuration
public class SshExecutorConfig {

    /**
     * Creates the application's shared SSH executor service.
     *
     * <p>A fixed-size thread pool is used to limit the number of concurrent
     * SSH connections while allowing commands to be executed in parallel
     * across multiple network devices.</p>
     *
     * <p>The pool size is obtained from the application's SSH configuration
     * properties.</p>
     *
     * <p>Worker threads are assigned descriptive names to simplify debugging,
     * logging, and thread dump analysis.</p>
     *
     * @param properties the SSH configuration properties
     * @return the shared executor service used for SSH command execution
     */
    @Bean(name = "sshExecutorService", destroyMethod = "shutdown")
    public ExecutorService sshExecutorService(
            SshProperties properties
    ) {
        return Executors.newFixedThreadPool(
                properties.maximumConcurrentConnections(),
                Thread.ofPlatform()
                        .name("ssh-worker-", 0)
                        .factory()
        );
    }
}
package gr.priovolos.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class SshExecutorConfig {

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
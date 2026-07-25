package gr.priovolos.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ssh")
public record SshProperties(

        Duration connectionTimeout,
        Duration authenticationTimeout,
        Duration commandTimeout,
        int maximumOutputBytes,
        int maximumConcurrentConnections,
        int maximumDevicesPerRequest,
        int maximumCommandLength
) {
}
package gr.priovolos.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the application's SSH subsystem.
 *
 * <p>The values are loaded from the application's configuration using the
 * {@code app.ssh} prefix and provide a centralized way of configuring
 * SSH connection behavior, execution limits, and security constraints.</p>
 *
 * <p>Externalizing these settings allows the application to be tuned
 * without modifying or recompiling the source code.</p>
 *
 * @author Ioannis Priovolos
 */
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
package gr.priovolos.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates the application's SSH configuration properties during startup.
 *
 * <p>This component performs additional runtime validation of the values
 * loaded into {@link SshProperties}. If an invalid configuration is
 * detected, the application fails to start with a descriptive error
 * message, preventing undefined behaviour during SSH operations.</p>
 *
 * <p>The validation ensures that configured timeouts and operational
 * limits remain within acceptable ranges for the application's
 * security and performance requirements.</p>
 *
 * @author Ioannis Priovolos
 */
@Component
@RequiredArgsConstructor
public class SshPropertiesValidator {

    /**
     * SSH configuration properties loaded from the application
     * configuration.
     */
    private final SshProperties properties;

    /**
     * Validates all SSH configuration properties after the Spring
     * Bean has been initialized.
     *
     * <p>The validation verifies:
     * <ul>
     *     <li>Connection timeout.</li>
     *     <li>Authentication timeout.</li>
     *     <li>Command timeout.</li>
     *     <li>Maximum SSH command output size.</li>
     *     <li>Maximum concurrent SSH connections.</li>
     *     <li>Maximum devices per execution request.</li>
     *     <li>Maximum command length.</li>
     * </ul>
     *
     * <p>If any value is invalid, an {@link IllegalStateException}
     * is thrown and the application startup is aborted.</p>
     */
    @PostConstruct
    public void validate() {

        requirePositiveDuration(
                properties.connectionTimeout(),
                "SSH connection timeout"
        );

        requirePositiveDuration(
                properties.authenticationTimeout(),
                "SSH authentication timeout"
        );

        requirePositiveDuration(
                properties.commandTimeout(),
                "SSH command timeout"
        );

        if (properties.maximumOutputBytes() < 1
                || properties.maximumOutputBytes() > 10_485_760) {
            throw new IllegalStateException(
                    "SSH maximum output must be between 1 byte and 10 MiB."
            );
        }

        if (properties.maximumConcurrentConnections() < 1
                || properties.maximumConcurrentConnections() > 50) {
            throw new IllegalStateException(
                    "SSH concurrency must be between 1 and 50."
            );
        }

        if (properties.maximumDevicesPerRequest() < 1
                || properties.maximumDevicesPerRequest() > 500) {
            throw new IllegalStateException(
                    "SSH maximum devices per request is invalid."
            );
        }

        if (properties.maximumCommandLength() < 1
                || properties.maximumCommandLength() > 2_000) {
            throw new IllegalStateException(
                    "SSH maximum command length is invalid."
            );
        }
    }

    /**
     * Verifies that a configured timeout is present and represents
     * a positive duration.
     *
     * @param duration the configured duration to validate
     * @param propertyName the name of the configuration property
     *                     used in validation error messages
     * @throws IllegalStateException if the duration is {@code null},
     *                               zero or negative
     */
    private void requirePositiveDuration(
            java.time.Duration duration,
            String propertyName
    ) {
        if (duration == null
                || duration.isZero()
                || duration.isNegative()) {
            throw new IllegalStateException(
                    propertyName + " must be positive."
            );
        }
    }
}

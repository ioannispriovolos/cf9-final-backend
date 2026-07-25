package gr.priovolos.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SshPropertiesValidator {

    private final SshProperties properties;

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
                || properties.maximumCommandLength() > 10_000) {
            throw new IllegalStateException(
                    "SSH maximum command length is invalid."
            );
        }
    }

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

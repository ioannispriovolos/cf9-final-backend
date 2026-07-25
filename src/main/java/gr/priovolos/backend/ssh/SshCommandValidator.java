package gr.priovolos.backend.ssh;

import gr.priovolos.backend.config.SshProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SshCommandValidator {

    private final SshProperties properties;

    public String validateAndNormalize(String command) {

        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException(
                    "SSH command must not be empty."
            );
        }

        String normalized = command.strip();

        if (normalized.length()
                > properties.maximumCommandLength()) {
            throw new IllegalArgumentException(
                    "SSH command exceeds the maximum allowed length."
            );
        }

        if (normalized.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(
                    "SSH command contains a null character."
            );
        }

        boolean invalidControlCharacter =
                normalized.chars().anyMatch(character ->
                        Character.isISOControl(character)
                                && character != '\n'
                                && character != '\r'
                                && character != '\t'
                );

        if (invalidControlCharacter) {
            throw new IllegalArgumentException(
                    "SSH command contains invalid control characters."
            );
        }

        return normalized;
    }
}
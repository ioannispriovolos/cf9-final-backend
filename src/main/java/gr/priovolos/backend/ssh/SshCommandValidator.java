package gr.priovolos.backend.ssh;

import gr.priovolos.backend.config.SshProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for validating and normalizing SSH commands
 * supplied by users.
 *
 * <p>This validator performs basic syntactic validation before a
 * command is executed on one or more network devices. The validation
 * ensures that commands are well-formed and comply with the
 * application's configurable security and size constraints.</p>
 *
 * <p>The validator does <strong>not</strong> restrict the specific
 * commands that users may execute. Authorization to execute SSH
 * commands is enforced elsewhere by Spring Security, while this
 * component focuses solely on validating the structure of the
 * supplied command.</p>
 *
 * @author Ioannis Priovolos
 */
@Component
@RequiredArgsConstructor
public class SshCommandValidator {

    private final SshProperties properties;

    /**
     * Validates and normalizes a user-supplied SSH command.
     *
     * <p>The validation performs the following checks:</p>
     * <ul>
     *     <li>The command must not be {@code null} or blank.</li>
     *     <li>The command must not exceed the configured maximum
     *     length.</li>
     *     <li>The command must not contain null characters.</li>
     *     <li>The command must not contain unsupported ISO control
     *     characters.</li>
     * </ul>
     *
     * <p>Leading and trailing whitespace is removed before the
     * validated command is returned.</p>
     *
     * @param command the SSH command supplied by the user
     * @return the normalized command ready for execution
     * @throws IllegalArgumentException if the command violates any
     *                                  validation rule
     */
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
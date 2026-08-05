package gr.priovolos.backend.ssh;

import gr.priovolos.backend.model.Device;

/**
 * Immutable Data Transfer Object representing a network device that is
 * ready for SSH command execution.
 *
 * <p>This record contains only the information required by the SSH
 * execution layer to establish a connection and execute a command on
 * a managed network device.</p>
 *
 * <p>Instances of this record are created from {@link Device} entities
 * before concurrent execution begins. This ensures that worker threads
 * operate on immutable data instead of managed JPA entities, avoiding
 * thread-safety issues and eliminating any dependency on an active
 * persistence context.</p>
 *
 * <p>The password contained in this record remains encrypted until
 * immediately before authentication. Decryption is performed by the
 * SSH command executor using the application's encryption service.</p>
 *
 * @param deviceId the unique identifier of the managed device
 * @param title the display name of the device
 * @param ipAddress the IPv4 address of the device
 * @param sshPort the SSH port used for the connection
 * @param username the SSH username
 * @param encryptedPassword the encrypted SSH password
 *
 * @author Ioannis Priovolos
 */
public record SshTarget(

        Long deviceId,
        String title,
        String ipAddress,
        int sshPort,
        String username,
        String encryptedPassword
) {

    /**
     * Creates an immutable SSH target from a managed device entity.
     *
     * <p>If the device does not specify an SSH port, the standard
     * SSH port ({@code 22}) is used.</p>
     *
     * <p>The device password remains encrypted and is transferred
     * unchanged into the resulting record. Decryption is intentionally
     * deferred until immediately before the SSH authentication phase.</p>
     *
     * @param device the managed network device
     * @return an immutable SSH target ready for command execution
     */
    public static SshTarget from(Device device) {

        int port = device.getSshPort() == null
                ? 22
                : device.getSshPort();

        return new SshTarget(
                device.getId(),
                device.getTitle(),
                device.getIpAddress(),
                port,
                device.getUsername(),
                device.getPassword()
        );
    }
}

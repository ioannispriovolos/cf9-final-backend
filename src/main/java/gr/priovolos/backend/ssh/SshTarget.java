package gr.priovolos.backend.ssh;

import gr.priovolos.backend.model.Device;

public record SshTarget(

        Long deviceId,
        String title,
        String ipAddress,
        int sshPort,
        String username,
        String encryptedPassword
) {

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

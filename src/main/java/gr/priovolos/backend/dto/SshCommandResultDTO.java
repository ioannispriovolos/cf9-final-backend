package gr.priovolos.backend.dto;

public record SshCommandResultDTO(

        Long deviceId,
        String deviceTitle,
        String ipAddress,
        boolean successful,
        Integer exitStatus,
        String output,
        String errorOutput,
        String errorMessage,
        long durationMs
) {

    public static SshCommandResultDTO success(
            Long deviceId,
            String deviceTitle,
            String ipAddress,
            Integer exitStatus,
            String output,
            String errorOutput,
            long durationMs
    ) {
        return new SshCommandResultDTO(
                deviceId,
                deviceTitle,
                ipAddress,
                true,
                exitStatus,
                output,
                errorOutput,
                null,
                durationMs
        );
    }

    public static SshCommandResultDTO remoteCommandFailure(
            Long deviceId,
            String deviceTitle,
            String ipAddress,
            Integer exitStatus,
            String output,
            String errorOutput,
            long durationMs
    ) {
        return new SshCommandResultDTO(
                deviceId,
                deviceTitle,
                ipAddress,
                false,
                exitStatus,
                output,
                errorOutput,
                "The remote command reported a failure.",
                durationMs
        );
    }

    public static SshCommandResultDTO executionFailure(
            Long deviceId,
            String deviceTitle,
            String ipAddress,
            String errorMessage,
            long durationMs
    ) {
        return new SshCommandResultDTO(
                deviceId,
                deviceTitle,
                ipAddress,
                false,
                null,
                "",
                "",
                errorMessage,
                durationMs
        );
    }
}

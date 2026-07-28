package gr.priovolos.backend.dto.dashboard;

public record MonthlyDeviceCountDTO(
        String month,
        long count
) {
}
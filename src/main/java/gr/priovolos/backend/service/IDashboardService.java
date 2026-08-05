package gr.priovolos.backend.service;

import gr.priovolos.backend.dto.dashboard.ViewerDashboardResponseDTO;

/**
 * Service interface defining operations for the application dashboard.
 *
 * <p>This service provides statistical information and aggregated
 * metrics used by the viewer dashboard. The returned data is intended
 * to present an overview of the managed network infrastructure without
 * exposing sensitive device credentials or user information.</p>
 *
 * <p>The dashboard includes summary statistics, device distribution
 * charts, monthly device creation metrics, and recently created
 * network devices.</p>
 *
 * @author Ioannis Priovolos
 */
public interface IDashboardService {

    /**
     * Retrieves the complete viewer dashboard.
     *
     * <p>The returned dashboard contains statistical information
     * calculated from all active (non-deleted) network devices,
     * including:</p>
     * <ul>
     *     <li>Total active devices.</li>
     *     <li>Total distinct manufacturers.</li>
     *     <li>Total distinct device models.</li>
     *     <li>Devices added during the current month.</li>
     *     <li>Device distribution by manufacturer.</li>
     *     <li>Device distribution by model.</li>
     *     <li>Monthly device creation statistics.</li>
     *     <li>Recently created network devices.</li>
     * </ul>
     *
     * @return the aggregated viewer dashboard information
     */
    ViewerDashboardResponseDTO getViewerDashboard();
}

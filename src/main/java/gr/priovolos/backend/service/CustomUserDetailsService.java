package gr.priovolos.backend.service;

import gr.priovolos.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security implementation of the {@link UserDetailsService}
 * interface.
 *
 * <p>This service is responsible for loading application users
 * during the authentication process. It retrieves users from the
 * persistence layer using their username and returns them as
 * {@link UserDetails} objects that can be consumed by Spring
 * Security.</p>
 *
 * <p>If no user with the specified username exists, a
 * {@link UsernameNotFoundException} is thrown, causing the
 * authentication process to fail.</p>
 *
 * <p>This service is used by the authentication manager during
 * username/password authentication and by the JWT authentication
 * filter when validating authenticated requests.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads an application user by username.
     *
     * <p>The returned {@link User} entity implements
     * {@link UserDetails}, allowing it to be used directly by
     * Spring Security during authentication and authorization.</p>
     *
     * <p>The associated role and capabilities are eagerly fetched
     * by the repository using an {@code EntityGraph}, ensuring
     * that the user's granted authorities are immediately available
     * without additional database queries.</p>
     *
     * @param username the username identifying the user
     * @return the authenticated user as a {@link UserDetails} object
     * @throws UsernameNotFoundException if no user with the supplied
     *                                   username exists
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username));
    }
}
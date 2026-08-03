package gr.priovolos.backend.config;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class responsible for creating and configuring the
 * Apache MINA SSH client used by the application.
 *
 * <p>The configured {@link SshClient} is exposed as a singleton Spring Bean
 * and is shared by the SSH command execution service for establishing
 * secure SSH connections to managed network devices.</p>
 *
 * <p>For the purposes of this project, the SSH client operates within a
 * trusted network infrastructure and therefore accepts all server host keys
 * without performing cryptographic verification.</p>
 *
 * <p><strong>Important:</strong> Disabling host key verification is acceptable
 * only in controlled environments where all managed devices are trusted.
 * Production systems exposed to untrusted networks should verify host keys
 * to prevent man-in-the-middle (MITM) attacks.</p>
 *
 * @author Ioannis Priovolos
 */
@Configuration
@EnableConfigurationProperties(SshProperties.class)
public class SshClientConfig {

    /**
     * Creates and configures the application's SSH client.
     *
     * <p>The client is configured with Apache MINA SSHD's default settings,
     * accepts all SSH server host keys, and is automatically started before
     * being returned to the Spring container.</p>
     *
     * <p>The {@code destroyMethod = "stop"} attribute ensures that the SSH
     * client is shut down gracefully when the Spring application context
     * is closed.</p>
     *
     * @return the configured and started {@link SshClient}
     */
    @Bean(destroyMethod = "stop")
    public SshClient sshClient() {

        SshClient client = SshClient.setUpDefaultClient();

        /*
         * Intentional project decision:
         *
         * The backend operates inside a trusted management network and
         * does not verify router host keys.
         *
         * This means a host at the requested IP address is accepted
         * without cryptographic identity verification.
         */
        client.setServerKeyVerifier(
                AcceptAllServerKeyVerifier.INSTANCE
        );

        client.start();

        return client;
    }
}
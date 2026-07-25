package gr.priovolos.backend.config;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SshProperties.class)
public class SshClientConfig {

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
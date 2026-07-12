package gr.priovolos.backend.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SshAutomationService {

    private SshClient client;

    @PostConstruct
    public void init() {
        client = SshClient.setUpDefaultClient();

        /*
         * EVALUATION MODE CONFIGURATION:
         * Automatically accepts any device's public key fingerprint. This removes the
         * need for a local known_hosts file, ensuring full compatibility across diverse
         * examiner testing networks and isolated environments.
         */
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);

        client.start();
        log.warn("SSH Client initialized in EVALUATION mode: Host verification skipped for deployment simplicity.");
    }

    @PreDestroy
    public void teardown() {
        if (client != null) {
            client.stop();
            log.info("SSH Client shut down gracefully.");
        }
    }

    /**
     * Connects to a physical network device using an interactive PTY shell,
     * executing commands natively across Cisco, Palo Alto, Aruba, and MikroTik.
     */
    public String executeUniversalCommand(String host, int port, String username, String password, String command) throws IOException {
        try (ClientSession session = client.connect(username, host, port).verify(10, TimeUnit.SECONDS).getSession()) {

            session.addPasswordIdentity(password);
            session.auth().verify(10, TimeUnit.SECONDS);

            try (ChannelShell channel = session.createShellChannel();
                 ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {

                channel.setOut(outStream);
                channel.setErr(outStream); // Merges error streams to prevent pipeline thread locks
                channel.open().verify(5, TimeUnit.SECONDS);

                // Open inverted stream to pump output directly into the remote terminal CLI
                try (OutputStream commandPipe = channel.getInvertedIn()) {

                    // 1. Send Paging Bypass commands (Prevents Cisco/Aruba from freezing at a "--More--" prompt)
                    String pagingBypass = "terminal length 0\nset cli pager off\n";
                    commandPipe.write(pagingBypass.getBytes());
                    commandPipe.flush();

                    // 2. Deliver your targeted execution instruction
                    String targetCommand = command.endsWith("\n") ? command : command + "\n";
                    commandPipe.write(targetCommand.getBytes());
                    commandPipe.flush();

                    // 3. Gracefully sign off the terminal shell session
                    commandPipe.write("exit\n".getBytes());
                    commandPipe.flush();

                    // Give physical hardware memory buffers time to process instructions and respond
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(5));

                return sanitizeTerminalOutput(outStream.toString());
            }
        }
    }

    /**
     * Strips low-level ANSI console text highlights and standard line feeds cleanly
     */
    private String sanitizeTerminalOutput(String output) {
        if (output == null) return "";

        return output
                // Erases VT100/ANSI character formatting escape brackets
                .replaceAll("\\x1B\\[[0-9;]*[a-zA-Z]", "")
                // Clears hidden carriage return formats
                .replaceAll("\\r", "")
                .trim();
    }
}
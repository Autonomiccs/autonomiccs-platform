package br.com.autonomiccs.autonomic.plugin.common.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Execute shell commands
 */
@Component
public class ShellCommandUtils {

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * Executes the specified shell command and wait for the
     * end of command execution to continue with the application
     * flow.
     *
     * @param command
     *            The command that will be executed.
     * @return
     *         A <code>String</code> that is the result from
     *         command executed.
     */
    public String executeCommand(String command) {
        Writer output = new StringWriter();
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            IOUtils.copy(p.getInputStream(), output);
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }
        return output.toString();
    }

}
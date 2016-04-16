package br.com.autonomiccs.autonomic.plugin.common.utils;

import org.springframework.stereotype.Component;

import com.cloud.utils.exception.CloudRuntimeException;

/**
 * Util operations over threads (for example, sleep)
 */
@Component
public class ThreadUtils {

    /**
     * The thread executing this method sleeps a given amount of seconds.
     *
     * @param secondsToSleep
     */
    public void sleepThread(int secondsToSleep) {
        try {
            Thread.sleep(secondsToSleep * 1000);
        } catch (InterruptedException e) {
            throw new CloudRuntimeException(e);
        }
    }

}

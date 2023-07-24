package utils;

import network.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodicTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PeriodicTask.class.getName());
    @Override
    public void run() {
        try {
            System.out.println("Hello World!" + System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("Error in task execution: " + e.getMessage());
        }
    }
}
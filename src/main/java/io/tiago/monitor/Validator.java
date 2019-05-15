package io.tiago.monitor;

import java.util.HashMap;
import java.util.Map;

public class Validator {

    public Map<String, String> validate(Node node) {

        Map<String, String> validations = new HashMap<>();

        if (node.getPollFrequency() < 1) {
            validations.put("poll_frequency", "Poll frequency must be not empty or equal to zero");
        }

        if (node.getStart() == null || node.getEnd() == null) {
            validations.put("start", "Start time must be not empty");
        }

        if (node.getEnd() == null) {
            validations.put("end", "End time must be not empty");
        }

        if (node.getHost() == null || "".equals(node.getHost())) {
            validations.put("host", "Host must be not empty");
        }

        if (node.getPort() < 1) {
            validations.put("port", "Port must be not empty or equal to 0");
        }

        return validations;
    }
}

package common;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonCreator {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    public JsonNode createCheckinResponse(boolean[] relayStatus) {
        JsonNodeFactory nodeFactory = new JsonNodeFactory(false);
        ObjectNode result = nodeFactory.objectNode();

        ArrayList<ObjectNode> actionList = new ArrayList<>();
        ArrayNode actions = nodeFactory.arrayNode();
        int i = 1;
        for (boolean status : relayStatus) {
            ObjectNode action = nodeFactory.objectNode();
            action.put("target", String.format("%d", i));
            action.put("action", relayStatus[i - 1] ? "on" : "off");

            // ridiculous solution due to the arduino json lib apparently
            // not handling complex nodes within an array very well
            ObjectNode command = nodeFactory.objectNode();
            command.put("command", action.toString());

            actions.add(command);
            i++;
        }
        result.put("actions", actions);

        return (JsonNode)result;
    }
}

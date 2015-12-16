package common;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonCreator {
    public JsonNode createCheckinResponse(boolean[] relayStatus) {
        JsonNodeFactory nodeFactory = new JsonNodeFactory(false);
        ObjectNode result = nodeFactory.objectNode();

        ArrayList<ObjectNode> actionList = new ArrayList<>();
        ArrayNode actions = nodeFactory.arrayNode();
        int i = 0;
        for (boolean status : relayStatus) {
            ObjectNode action = nodeFactory.objectNode();
            action.put("action", relayStatus[i++] ? "on" : "off");
            action.put("target", i);
            actions.add(action);
        }
        result.put("actions", actions);

        return (JsonNode)result;
    }
}

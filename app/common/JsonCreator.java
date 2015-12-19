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
        int i = 1;
        for (boolean status : relayStatus) {
            ObjectNode action = nodeFactory.objectNode();
            action.put("action", String.format("%d, %s", i, relayStatus[i - 1] ? "on" : "off"));
            actions.add(action);
            i++;
        }
        result.put("actions", actions);

        return (JsonNode)result;
    }
}

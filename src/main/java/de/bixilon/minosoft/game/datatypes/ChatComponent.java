package de.bixilon.minosoft.game.datatypes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatComponent {
    JSONObject json;

    public ChatComponent(String raw) {
        try {
            this.json = new JSONObject(raw);
        } catch (JSONException e) {
            // not a text component, is a legacy string
            this.json = new JSONObject();
            this.json.put("text", raw);
        }
    }

    public ChatComponent(JSONObject json) {
        this.json = json;
    }

    //ToDo
    public String getRawMessage() {
        if (json.has("text") && json.getString("text").length() != 0) {
            return json.getString("text");
        }
        StringBuilder buffer = new StringBuilder();
        JSONArray arr = json.getJSONArray("extra");
        for (int i = 0; i < arr.length(); i++) {
            buffer.append(arr.getJSONObject(i).getString("text"));
        }
        return buffer.toString();
    }

    public JSONObject getRaw() {
        return this.json;
    }
}

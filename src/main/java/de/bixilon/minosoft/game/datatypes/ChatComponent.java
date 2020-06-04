package de.bixilon.minosoft.game.datatypes;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatComponent {
    final JSONObject json;

    public ChatComponent(String raw) {
        this.json = new JSONObject(raw);
    }

    public ChatComponent(JSONObject json) {
        this.json = json;
    }

    //ToDo
    public String getRawMessage() {
        if (json.getString("text").length() != 0) {
            return json.getString("text");
        }
        StringBuffer buffer = new StringBuffer();
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

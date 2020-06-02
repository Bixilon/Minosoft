package de.bixilon.minosoft.objects;

import org.json.JSONObject;

public class ServerListPing {
    JSONObject raw;

    public ServerListPing(JSONObject json) {
        this.raw = json;
    }

    public int getProtocolNumber() {
        return raw.getJSONObject("version").getInt("protocol");
    }

    public String getServerBrand() {
        return raw.getJSONObject("version").getString("name");
    }

    public int getPlayerOnline() {
        return raw.getJSONObject("players").getInt("online");
    }

    public int getMaxPlayers() {
        return raw.getJSONObject("players").getInt("max");
    }

    public String getBase64EncodedFavicon() {
        return raw.getString("favicon");
    }

    public String getMotd() {
        //ToDo TextComponent handling
        return raw.getString("description");
    }
}

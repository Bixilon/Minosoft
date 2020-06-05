package de.bixilon.minosoft.config;

public enum GameConfiguration implements ConfigEnum {
    CONFIG_VERSION("version"),
    GAME_RENDER_DISTANCE("game.render-distance"),
    NETWORK_FAKE_CLIENT_BRAND("network.fake-client-brand"),
    GENERAL_LOG_LEVEL("general.log-level");


    final String path;

    GameConfiguration(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }
}

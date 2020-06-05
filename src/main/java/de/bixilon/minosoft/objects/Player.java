package de.bixilon.minosoft.objects;

import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.game.datatypes.World;
import de.bixilon.minosoft.game.datatypes.player.Location;

import java.util.UUID;

public class Player {
    Account acc;
    float health;
    short food;
    float saturation;
    Location spawnLocation;
    int entityId;
    GameMode gameMode;
    World world = new World("world");
    byte selectedSlot;
    short level;
    short totalExperience;

    public Player(Account acc) {
        this.acc = acc;
        acc.login();
    }

    public String getPlayerName() {
        return acc.getPlayerName();
    }

    public UUID getPlayerUUID() {
        return acc.getUUID();
    }

    public Account getAccount() {
        return this.acc;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public short getFood() {
        return food;
    }

    public void setFood(short food) {
        this.food = food;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public float getSaturation() {
        return saturation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public World getWorld() {
        return world;
    }

    public byte getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(byte selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public short getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(short totalExperience) {
        this.totalExperience = totalExperience;
    }
}

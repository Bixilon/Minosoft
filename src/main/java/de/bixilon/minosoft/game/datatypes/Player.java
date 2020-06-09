/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.meta.HumanMetaData;
import de.bixilon.minosoft.game.datatypes.world.World;
import de.bixilon.minosoft.mojang.api.MojangAccount;

import java.util.HashMap;
import java.util.UUID;

public class Player {
    final MojangAccount acc;
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
    HumanMetaData metaData;
    HashMap<Slots.Inventory, Slot> inventory = new HashMap<>();
    boolean spawnConfirmed = false;

    public Player(MojangAccount acc) {
        this.acc = acc;
    }

    public String getPlayerName() {
        return acc.getPlayerName();
    }

    public UUID getPlayerUUID() {
        return acc.getUUID();
    }

    public MojangAccount getAccount() {
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

    public HumanMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(HumanMetaData metaData) {
        this.metaData = metaData;
    }

    public HashMap<Slots.Inventory, Slot> getInventory() {
        return inventory;
    }

    public void setInventory(HashMap<Slots.Inventory, Slot> inventory) {
        this.inventory = inventory;
    }

    public void setInventory(Slot[] data) {
        for (int i = 0; i < data.length; i++) {
            setSlot(Slots.Inventory.byId(i), data[i]);
        }
    }

    public Slot getSlot(Slots.Inventory slot) {
        return inventory.get(slot);
    }

    public void setSlot(Slots.Inventory slot, Slot data) {
        inventory.put(slot, data);
    }

    public boolean isSpawnConfirmed() {
        return spawnConfirmed;
    }

    public void setSpawnConfirmed(boolean spawnConfirmed) {
        this.spawnConfirmed = spawnConfirmed;
    }
}

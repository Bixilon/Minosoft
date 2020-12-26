/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data;

import de.bixilon.minosoft.data.accounts.Account;
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity;
import de.bixilon.minosoft.data.inventory.Inventory;
import de.bixilon.minosoft.data.inventory.InventoryProperties;
import de.bixilon.minosoft.data.inventory.InventorySlots;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.player.PlayerListItem;
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.data.world.World;

import java.util.HashMap;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.PLAYER_INVENTORY_ID;

public class Player {
    public final HashMap<UUID, PlayerListItem> playerList = new HashMap<>();
    final Account account;
    final ScoreboardManager scoreboardManager = new ScoreboardManager();
    final World world = new World();
    final HashMap<Integer, Inventory> inventories = new HashMap<>();
    float health;
    int food;
    float saturation;
    BlockPosition spawnLocation;
    GameModes gameMode;
    byte selectedSlot;
    int level;
    int totalExperience;
    PlayerEntity entity;
    boolean spawnConfirmed;

    ChatComponent tabHeader;
    ChatComponent tabFooter;

    public Player(Account account) {
        this.account = account;
        // create our own inventory without any properties
        this.inventories.put(PLAYER_INVENTORY_ID, new Inventory(null));
    }

    public String getPlayerName() {
        return this.account.getUsername();
    }

    public UUID getPlayerUUID() {
        return this.account.getUUID();
    }

    public Account getAccount() {
        return this.account;
    }

    public float getHealth() {
        return this.health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public int getFood() {
        return this.food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public float getSaturation() {
        return this.saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public BlockPosition getSpawnLocation() {
        return this.spawnLocation;
    }

    public void setSpawnLocation(BlockPosition spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public GameModes getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameModes gameMode) {
        this.gameMode = gameMode;
    }

    public World getWorld() {
        return this.world;
    }

    public byte getSelectedSlot() {
        return this.selectedSlot;
    }

    public void setSelectedSlot(byte selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    public Inventory getPlayerInventory() {
        return getInventory(PLAYER_INVENTORY_ID);
    }

    public void setPlayerInventory(Slot[] data) {
        setInventory(PLAYER_INVENTORY_ID, data);
    }

    public void setInventory(int windowId, Slot[] data) {
        for (int i = 0; i < data.length; i++) {
            setSlot(windowId, i, data[i]);
        }
    }

    public void setSlot(int windowId, int slot, Slot data) {
        this.inventories.get(windowId).setSlot(slot, data);
    }

    public Inventory getInventory(int id) {
        return this.inventories.get(id);
    }

    public Slot getSlot(int windowId, InventorySlots.InventoryInterface slot, int versionId) {
        return getSlot(windowId, slot.getId(versionId));
    }

    public Slot getSlot(int windowId, int slot) {
        return this.inventories.get(windowId).getSlot(slot);
    }

    public void setSlot(int windowId, InventorySlots.InventoryInterface slot, int versionId, Slot data) {
        setSlot(windowId, slot.getId(versionId), data);
    }

    public void createInventory(InventoryProperties properties) {
        this.inventories.put(properties.getWindowId(), new Inventory(properties));
    }

    public void deleteInventory(int windowId) {
        this.inventories.remove(windowId);
    }

    public boolean isSpawnConfirmed() {
        return this.spawnConfirmed;
    }

    public void setSpawnConfirmed(boolean spawnConfirmed) {
        this.spawnConfirmed = spawnConfirmed;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public HashMap<UUID, PlayerListItem> getPlayerList() {
        return this.playerList;
    }

    public PlayerListItem getPlayerListItem(String name) {
        // only legacy
        for (PlayerListItem listItem : this.playerList.values()) {
            if (listItem.getName().equals(name)) {
                return listItem;
            }
        }
        return null;
    }

    public ChatComponent getTabHeader() {
        return this.tabHeader;
    }

    public void setTabHeader(ChatComponent tabHeader) {
        this.tabHeader = tabHeader;
    }

    public ChatComponent getTabFooter() {
        return this.tabFooter;
    }

    public void setTabFooter(ChatComponent tabFooter) {
        this.tabFooter = tabFooter;
    }

    public PlayerEntity getEntity() {
        return this.entity;
    }

    public void setEntity(PlayerEntity entity) {
        this.entity = entity;
    }
}

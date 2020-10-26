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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketBossBar;

import java.util.UUID;

/**
 * Fired when the/one boss bar changes
 */
public class BossBarChangeEvent extends CancelableEvent {
    private UUID uuid;
    private PacketBossBar.BossBarActions action;
    private ChatComponent title;
    private float health;
    private PacketBossBar.BossBarColors color;
    private PacketBossBar.BossBarDivisions divisions;
    private boolean isDragonBar;
    private boolean shouldDarkenSky;
    private boolean createFog;

    public BossBarChangeEvent(Connection connection, UUID uuid, PacketBossBar.BossBarActions action, ChatComponent title, float health, PacketBossBar.BossBarColors color, PacketBossBar.BossBarDivisions divisions, boolean isDragonBar, boolean shouldDarkenSky, boolean createFog) {
        super(connection);
        this.uuid = uuid;
        this.action = action;
        this.title = title;
        this.health = health;
        this.color = color;
        this.divisions = divisions;
        this.isDragonBar = isDragonBar;
        this.shouldDarkenSky = shouldDarkenSky;
        this.createFog = createFog;
    }

    public BossBarChangeEvent(Connection connection, PacketBossBar pkg) {
        super(connection);
        this.uuid = pkg.getUUID();
        this.action = pkg.getAction();
        this.title = pkg.getTitle();
        this.health = pkg.getHealth();
        this.color = pkg.getColor();
        this.divisions = pkg.getDivisions();
        this.isDragonBar = pkg.isDragonBar();
        this.shouldDarkenSky = pkg.shouldDarkenSky();
        this.createFog = pkg.createFog();
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public PacketBossBar.BossBarActions getAction() {
        return action;
    }

    public void setAction(PacketBossBar.BossBarActions action) {
        this.action = action;
    }

    public ChatComponent getTitle() {
        return title;
    }

    public void setTitle(ChatComponent title) {
        this.title = title;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public PacketBossBar.BossBarColors getColor() {
        return color;
    }

    public void setColor(PacketBossBar.BossBarColors color) {
        this.color = color;
    }

    public PacketBossBar.BossBarDivisions getDivisions() {
        return divisions;
    }

    public void setDivisions(PacketBossBar.BossBarDivisions divisions) {
        this.divisions = divisions;
    }

    public boolean isDragonBar() {
        return isDragonBar;
    }

    public void setDragonBar(boolean dragonBar) {
        isDragonBar = dragonBar;
    }

    public boolean isShouldDarkenSky() {
        return shouldDarkenSky;
    }

    public void setShouldDarkenSky(boolean shouldDarkenSky) {
        this.shouldDarkenSky = shouldDarkenSky;
    }

    public boolean isCreateFog() {
        return createFog;
    }

    public void setCreateFog(boolean createFog) {
        this.createFog = createFog;
    }
}

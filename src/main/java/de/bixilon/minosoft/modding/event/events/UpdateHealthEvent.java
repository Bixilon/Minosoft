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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketUpdateHealth;

public class UpdateHealthEvent extends ConnectionEvent {
    private final float health;
    private final int food;
    private final float saturation;

    public UpdateHealthEvent(Connection connection, float health, int food, float saturation) {
        super(connection);
        this.health = health;
        this.food = food;
        this.saturation = saturation;
    }

    public UpdateHealthEvent(Connection connection, PacketUpdateHealth pkg) {
        super(connection);
        this.health = pkg.getHealth();
        this.food = pkg.getFood();
        this.saturation = pkg.getSaturation();
    }

    public float getHealth() {
        return this.health;
    }

    public int getFood() {
        return this.food;
    }

    public float getSaturation() {
        return this.saturation;
    }
}

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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketSpawnLocation;

public class SpawnLocationChangeEvent extends Event {
    private final BlockPosition location;

    public SpawnLocationChangeEvent(Connection connection, BlockPosition location) {
        super(connection);
        this.location = location;
    }

    public SpawnLocationChangeEvent(Connection connection, PacketSpawnLocation pkg) {
        super(connection);
        this.location = pkg.getSpawnLocation();
    }

    public BlockPosition getSpawnLocation() {
        return location;
    }
}

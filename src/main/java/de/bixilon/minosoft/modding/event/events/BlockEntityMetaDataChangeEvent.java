/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.registries.ResourceLocation;
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.block.BlockDataS2CP;
import glm_.vec3.Vec3i;

import java.util.Map;

public class BlockEntityMetaDataChangeEvent extends PlayConnectionEvent {
    private final Vec3i position;
    private final ResourceLocation type;
    private final Map<String, Object> nbt;

    public BlockEntityMetaDataChangeEvent(PlayConnection connection, Vec3i position, ResourceLocation type, Map<String, Object> nbt) {
        super(connection);
        this.position = position;
        this.type = type;
        this.nbt = nbt;
    }

    public BlockEntityMetaDataChangeEvent(PlayConnection connection, BlockDataS2CP pkg) {
        super(connection);
        this.position = pkg.getPosition();
        this.type = pkg.getType();
        this.nbt = pkg.getNbt();
    }

    public Vec3i getPosition() {
        return this.position;
    }
}

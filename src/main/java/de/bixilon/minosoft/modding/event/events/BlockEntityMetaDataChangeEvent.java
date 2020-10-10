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
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketBlockEntityMetadata;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import javax.annotation.Nullable;

public class BlockEntityMetaDataChangeEvent extends Event {
    private final BlockPosition position;
    private final PacketBlockEntityMetadata.BlockEntityActions action;
    private final CompoundTag nbt;

    public BlockEntityMetaDataChangeEvent(Connection connection, BlockPosition position, PacketBlockEntityMetadata.BlockEntityActions action, CompoundTag nbt) {
        super(connection);
        this.position = position;
        this.action = action;
        this.nbt = nbt;
        this.nbt.setFinal();
    }

    public BlockEntityMetaDataChangeEvent(Connection connection, PacketBlockEntityMetadata pkg) {
        super(connection);
        this.position = pkg.getPosition();
        this.action = pkg.getAction();
        this.nbt = pkg.getNbt();
        this.nbt.setFinal();
    }

    public BlockPosition getPosition() {
        return position;
    }

    @Nullable
    public PacketBlockEntityMetadata.BlockEntityActions getAction() {
        return action;
    }

    public CompoundTag getNbt() {
        return nbt;
    }
}

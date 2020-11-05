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

import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketOpenSignEditor;

public class OpenSignEditorEvent extends CancelableEvent {
    private final BlockPosition position;

    public OpenSignEditorEvent(Connection connection, BlockPosition position) {
        super(connection);
        this.position = position;
    }

    public OpenSignEditorEvent(Connection connection, PacketOpenSignEditor pkg) {
        super(connection);
        this.position = pkg.getPosition();
    }

    public BlockPosition getPosition() {
        return position;
    }
}

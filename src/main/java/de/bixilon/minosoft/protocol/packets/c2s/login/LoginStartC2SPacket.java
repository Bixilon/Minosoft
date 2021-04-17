/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.c2s.login;

import de.bixilon.minosoft.data.player.Player;
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class LoginStartC2SPacket implements PlayC2SPacket {
    private final String username;

    public LoginStartC2SPacket(Player player) {
        this.username = player.getEntity().getName();
    }

    public LoginStartC2SPacket(String username) {
        this.username = username;
    }

    @Override
    public void write(PlayOutByteBuffer buffer) {
        buffer.writeString(this.username);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending login start (username=%s)", this.username));
    }
}

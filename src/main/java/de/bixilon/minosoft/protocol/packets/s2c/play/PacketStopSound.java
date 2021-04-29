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

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.data.SoundCategories;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W45A;

public class PacketStopSound extends PlayS2CPacket {
    private SoundCategories category;
    private ResourceLocation soundResourceLocation;

    public PacketStopSound(PlayInByteBuffer buffer) {
        if (buffer.getVersionId() < V_17W45A) { // ToDo: these 2 values need to be switched in before 1.12.2
            this.category = SoundCategories.valueOf(buffer.readString().toUpperCase());
            this.soundResourceLocation = buffer.readResourceLocation();
            return;
        }
        byte flags = buffer.readByte();
        if (BitByte.isBitMask(flags, 0x01)) {
            this.category = SoundCategories.Companion.get(buffer.readVarInt());
        }
        if (BitByte.isBitMask(flags, 0x02)) {
            this.soundResourceLocation = buffer.readResourceLocation();
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received stop sound (category=%s, soundResourceLocation=%s)", this.category, this.soundResourceLocation));
    }

    public SoundCategories getSoundId() {
        return this.category;
    }

    public ResourceLocation getSoundResourceLocation() {
        return this.soundResourceLocation;
    }
}

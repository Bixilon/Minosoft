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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.Identifier;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.sounds.Sounds;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;


public class PacketSoundEffect implements ClientboundPacket {
    static final float pitchCalc = 100.0F / 63.0F;
    Location location;
    Sounds sound;
    String soundName;
    float volume;
    int pitch;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                soundName = buffer.readString();
                sound = Sounds.byName(new Identifier(soundName));
                location = new Location(buffer.readInteger() * 8, buffer.readInteger() * 8, buffer.readInteger() * 8);
                volume = buffer.readFloat();
                pitch = (int) (buffer.readByte() * pitchCalc);
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Play sound effect %s with volume=%s and pitch=%s at %s", ((sound == null) ? TextComponent.ChatAttributes.RED + soundName + TextComponent.ChatAttributes.RESET : sound.name()), volume, pitch, location.toString()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public Location getLocation() {
        return location;
    }

    /**
     * @return Pitch in Percent * 100
     */
    public int getPitch() {
        return pitch;
    }

    public Sounds getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }
}

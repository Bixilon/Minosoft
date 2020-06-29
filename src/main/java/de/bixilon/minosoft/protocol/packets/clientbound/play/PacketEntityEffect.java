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

import de.bixilon.minosoft.game.datatypes.entities.StatusEffect;
import de.bixilon.minosoft.game.datatypes.entities.StatusEffects;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketEntityEffect implements ClientboundPacket {
    int entityId;
    StatusEffect effect;
    boolean hideParticle;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                entityId = buffer.readInteger();
                effect = new StatusEffect(StatusEffects.byId(buffer.readByte()), buffer.readByte(), buffer.readShort());
                hideParticle = false;
                return true;
            case VERSION_1_8:
            case VERSION_1_9_4:
                entityId = buffer.readVarInt();
                effect = new StatusEffect(StatusEffects.byId(buffer.readByte()), buffer.readByte(), buffer.readVarInt());
                hideParticle = buffer.readBoolean();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.game(String.format("Entity effect added: %d %s", entityId, effect.toString()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public StatusEffect getEffect() {
        return effect;
    }
}

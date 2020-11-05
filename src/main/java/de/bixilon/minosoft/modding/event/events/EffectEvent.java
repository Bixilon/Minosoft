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
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketEffect;

public class EffectEvent extends CancelableEvent {
    private final PacketEffect.EffectEffects effect;
    private final BlockPosition position;
    private final int data;
    private final boolean disableRelativeVolume;

    public EffectEvent(Connection connection, PacketEffect.EffectEffects effect, BlockPosition position, int data, boolean disableRelativeVolume) {
        super(connection);
        this.effect = effect;
        this.position = position;
        this.data = data;
        this.disableRelativeVolume = disableRelativeVolume;
    }

    public EffectEvent(Connection connection, PacketEffect pkg) {
        super(connection);
        this.effect = pkg.getEffect();
        this.position = pkg.getPosition();
        this.data = pkg.getData();
        this.disableRelativeVolume = pkg.isDisableRelativeVolume();
    }

    public PacketEffect.EffectEffects getEffect() {
        return effect;
    }

    public BlockPosition getPosition() {
        return position;
    }

    public int getData() {
        return data;
    }

    public boolean isDisableRelativeVolume() {
        return disableRelativeVolume;
    }
}

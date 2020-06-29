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

package de.bixilon.minosoft.game.datatypes.entities.objects;

import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.entities.*;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.entities.meta.FallingBlockMetaData;

public class FallingBlock extends EntityObject implements ObjectInterface {
    FallingBlockMetaData metaData;
    final Blocks block;

    public FallingBlock(int id, Location location, short yaw, short pitch, int additionalInt) {
        super(id, location, yaw, pitch, null);
        // objects do not spawn with metadata... reading additional info from the following int
        block = Blocks.byId(additionalInt & 0xFFF, additionalInt >>> 12);
    }

    public FallingBlock(int id, Location location, short yaw, short pitch, int additionalInt, Velocity velocity) {
        super(id, location, yaw, pitch, velocity);
        block = Blocks.byId(additionalInt & 0xFFF, additionalInt >>> 12);
    }

    @Override
    public Objects getEntityType() {
        return Objects.FALLING_BLOCK;
    }

    @Override
    public FallingBlockMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = (FallingBlockMetaData) metaData;
    }

    @Override
    public float getWidth() {
        return 0.98F;
    }

    @Override
    public float getHeight() {
        return 0.98F;
    }

    public Blocks getBlock() {
        //ToDo depends on protocol version
        return block;
    }

    @Override
    public Class<? extends EntityMetaData> getMetaDataClass() {
        return FallingBlockMetaData.class;
    }
}

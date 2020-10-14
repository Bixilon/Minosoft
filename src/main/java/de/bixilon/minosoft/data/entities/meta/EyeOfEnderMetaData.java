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
package de.bixilon.minosoft.data.entities.meta;

import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.Item;

public class EyeOfEnderMetaData extends EntityMetaData {

    public EyeOfEnderMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public Slot getItem() {
        Slot defaultValue = new Slot(new Item("minecraft", "ender_eye"));
        if (protocolId < 477) { // ToDo
            return defaultValue;
        }
        return sets.getSlot(super.getLastDataIndex(), defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        if (protocolId < 57) {
            return super.getLastDataIndex();
        }
        return super.getLastDataIndex() + 1;
    }
}

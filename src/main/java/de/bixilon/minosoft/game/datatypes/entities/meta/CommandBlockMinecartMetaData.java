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
package de.bixilon.minosoft.game.datatypes.entities.meta;

import de.bixilon.minosoft.game.datatypes.TextComponent;

import javax.annotation.Nullable;

public class CommandBlockMinecartMetaData extends EntityMetaData {

    public CommandBlockMinecartMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    @Nullable
    public String getCommand() {
        final String defaultValue = null;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getString(super.getLastDataIndex() + 1, defaultValue);
    }

    @Nullable
    public TextComponent getLastOutput() {
        final TextComponent defaultValue = null;
        if (protocolId < 110) { //ToDo
            return defaultValue;
        }
        return sets.getTextComponent(super.getLastDataIndex() + 2, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 2;
    }
}

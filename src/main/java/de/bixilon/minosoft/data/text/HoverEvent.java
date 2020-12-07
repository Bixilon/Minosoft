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

package de.bixilon.minosoft.data.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.util.Util;

import java.util.UUID;

public class HoverEvent {
    private final HoverEventActions action;
    private final Object value; // TextComponent, NBT, Entity, Achievement Id

    public HoverEvent(JsonObject json) {
        this.action = HoverEventActions.valueOf(json.get("action").getAsString().toUpperCase());
        JsonElement data = json.get("value");
        value = switch (action) { // ToDo
            case SHOW_TEXT -> ChatComponent.valueOf(data);
            case SHOW_ENTITY -> EntityHoverData.deserialize(JsonParser.parseString(json.get("value").getAsString()).getAsJsonObject());
            default -> null;
        };
    }

    public HoverEvent(HoverEventActions action, Object value) {
        this.action = action;
        if (!(value instanceof ChatComponent) && !(value instanceof EntityHoverData)) {
            throw new IllegalArgumentException(String.format("%s is not a valid value hier", value.getClass().getSimpleName()));
        }
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public enum HoverEventActions {
        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY,
        SHOW_ACHIEVEMENT
    }

    public static record EntityHoverData(UUID uuid, ModIdentifier identifier, ChatComponent name) {

        public static EntityHoverData deserialize(JsonObject json) {
            return new EntityHoverData(Util.getUUIDFromString(json.get("id").getAsString()), new ModIdentifier(json.get("type").getAsString()), ChatComponent.valueOf(json.get("name").getAsString()));
        }
    }
}

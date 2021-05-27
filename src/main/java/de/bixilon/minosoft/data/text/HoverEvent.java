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

package de.bixilon.minosoft.data.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.util.Util;

import java.util.UUID;

public class HoverEvent {
    private final HoverEventActions action;
    private final Object value; // TextComponent, NBT, Entity, Achievement Id

    public HoverEvent(JsonObject json) {
        this.action = HoverEventActions.valueOf(json.get("action").getAsString().toUpperCase());
        JsonElement data = null;
        if (json.has("value")) {
            data = json.get("value");
        }
        if (json.has("contents")) {
            data = json.get("contents");
        }
        json.get("value");
        this.value = switch (this.action) { // ToDo
            case SHOW_TEXT -> ChatComponent.Companion.of(data);
            case SHOW_ENTITY -> EntityHoverData.deserialize(data);
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
        return this.value;
    }

    public enum HoverEventActions {
        SHOW_TEXT,
        SHOW_ITEM,
        SHOW_ENTITY,
        SHOW_ACHIEVEMENT
    }

    public static final class EntityHoverData {
        private final UUID uuid;
        private final ResourceLocation resourceLocation;
        private final ChatComponent name;

        public EntityHoverData(UUID uuid, ResourceLocation resourceLocation, ChatComponent name) {
            this.uuid = uuid;
            this.resourceLocation = resourceLocation;
            this.name = name;
        }

        public static EntityHoverData deserialize(JsonElement data) {
            JsonObject json;
            if (data instanceof JsonPrimitive) {
                json = JsonParser.parseString(data.getAsString()).getAsJsonObject();
            } else {
                json = (JsonObject) data;
            }
            if (json.has("text")) {
                // 1.14.3.... lol
                json = JsonParser.parseString(json.get("text").getAsString()).getAsJsonObject();
            }
            ResourceLocation type = null;
            if (json.has("type")) {
                type = new ResourceLocation(json.get("type").getAsString());
            }
            return new EntityHoverData(Util.getUUIDFromString(json.get("id").getAsString()), type, ChatComponent.Companion.of(json.get("name")));
        }

        public UUID uuid() {
            return this.uuid;
        }

        public ResourceLocation resourceLocation() {
            return this.resourceLocation;
        }

        public ChatComponent name() {
            return this.name;
        }
    }
}

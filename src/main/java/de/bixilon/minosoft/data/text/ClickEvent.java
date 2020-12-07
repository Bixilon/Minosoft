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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ClickEvent {
    private final ClickEventActions action;
    private final Object value;

    public ClickEvent(JsonObject json) {
        this.action = ClickEventActions.valueOf(json.get("action").getAsString().toUpperCase());
        JsonPrimitive primitive = json.get("value").getAsJsonPrimitive();
        if (primitive.isNumber()) {
            this.value = primitive.getAsNumber();
        } else {
            this.value = primitive.getAsString();
        }
    }

    public ClickEvent(ClickEventActions action, Object value) {
        this.action = action;
        this.value = value;
    }

    public ClickEventActions getAction() {
        return action;
    }

    public Object getValue() {
        return value;
    }

    public enum ClickEventActions {
        OPEN_URL,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE
    }
}

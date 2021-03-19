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
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.bixilon.minosoft.data.locale.minecraft.MinecraftLocaleManager;
import de.bixilon.minosoft.gui.rendering.font.Font;
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.TextElement;
import glm_.vec2.Vec2i;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import javax.annotation.Nullable;

public abstract class ChatComponent {
    public static ChatComponent valueOf(Object raw) {
        return valueOf(null, null, raw);
    }

    public static ChatComponent valueOf(MinecraftLocaleManager localeManager, Object raw) {
        return valueOf(localeManager, null, raw);
    }

    public static ChatComponent valueOf(MinecraftLocaleManager localeManager, @Nullable TextComponent parent, Object raw) {
        if (raw == null) {
            return new BaseComponent();
        }
        if (raw instanceof ChatComponent component) {
            return component;
        }
        if (raw instanceof JsonPrimitive primitive) {
            raw = primitive.getAsString();
        }

        JsonObject json;
        if (raw instanceof JsonObject) {
            json = (JsonObject) raw;
        } else if (raw instanceof String) {
            try {
                json = JsonParser.parseString((String) raw).getAsJsonObject();
            } catch (JsonParseException | IllegalStateException ignored) {
                return new BaseComponent(localeManager, (String) raw);
            }
        } else {
            return new BaseComponent(localeManager, parent, raw.toString());
            // throw new IllegalArgumentException(String.format("%s is not a valid type here!", raw.getClass().getSimpleName()));
        }
        return new BaseComponent(localeManager, parent, json);
    }

    /**
     * @return Returns the message formatted with ANSI Formatting codes
     */
    public abstract String getANSIColoredMessage();

    /**
     * @return Returns the message formatted with minecraft formatting codes (§)
     */
    public abstract String getLegacyText();

    /**
     * @return Returns the unformatted message
     */
    public abstract String getMessage();

    /**
     * @return Returns the a list of Nodes, drawable in JavaFX (TextFlow)
     */
    public abstract ObservableList<Node> getJavaFXText(ObservableList<Node> nodes);

    /**
     * @return Returns the a list of Nodes, drawable in JavaFX (TextFlow)
     */
    public ObservableList<Node> getJavaFXText() {
        return getJavaFXText(FXCollections.observableArrayList());
    }

    /**
     * Prepares the chat component for rendering (used in opengl)
     */
    public abstract void prepareRender(Vec2i startPosition, Vec2i offset, Font font, TextElement textElement, int z, Vec2i retMaxSize);
}

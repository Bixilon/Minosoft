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

package de.bixilon.minosoft.data.text;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public interface ChatComponent {
    static ChatComponent fromString(String raw) {
        if (raw == null) {
            return new BaseComponent();
        }
        try {
            return new BaseComponent(JsonParser.parseString(raw).getAsJsonObject());
        } catch (JsonParseException | IllegalStateException ignored) {
        }
        return new BaseComponent(raw);
    }

    /**
     * @return Returns the message formatted with ANSI Formatting codes
     */
    String getANSIColoredMessage();

    /**
     * @return Returns the message formatted with minecraft formatting codes (§)
     */
    String getLegacyText();

    /**
     * @return Returns the unformatted message
     */
    String getMessage();

    /**
     * @return Returns the a list of Nodes, drawable in JavaFX (TextFlow)
     */
    ObservableList<Node> getJavaFXText();
}
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

import com.google.gson.JsonArray;
import de.bixilon.minosoft.data.locale.minecraft.MinecraftLocaleManager;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.ArrayList;

public class TranslatableComponent implements ChatComponent {
    private final ArrayList<ChatComponent> data = new ArrayList<>();
    private final String key;

    public TranslatableComponent(String key, JsonArray data) {
        this.key = key;
        if (data == null) {
            return;
        }
        data.forEach((jsonElement -> {
            if (jsonElement.isJsonPrimitive()) {
                this.data.add(ChatComponent.fromString(jsonElement.getAsString()));
            } else {
                this.data.add(new BaseComponent(jsonElement.getAsJsonObject()));
            }
        }));
    }

    @Override
    public String getANSIColoredMessage() {
        Object[] data = new String[this.data.size()];
        for (int i = 0; i < this.data.size(); i++) {
            data[i] = this.data.get(i).getANSIColoredMessage();
        }
        return MinecraftLocaleManager.translate(key, data);
    }

    @Override
    public String getLegacyText() {
        Object[] data = new String[this.data.size()];
        for (int i = 0; i < this.data.size(); i++) {
            data[i] = this.data.get(i).getLegacyText();
        }
        return MinecraftLocaleManager.translate(key, data);
    }

    @Override
    public String getMessage() {
        Object[] data = new String[this.data.size()];
        for (int i = 0; i < this.data.size(); i++) {
            data[i] = this.data.get(i).getMessage();
        }
        return MinecraftLocaleManager.translate(key, data);
    }

    @Override
    public ObservableList<Node> getJavaFXText() {
        // ToDo fix nested base component (formatting), not just a string

        // This is just a dirty workaround to enable formatting and coloring. Still need to do hover, click, ... stuff
        return new BaseComponent(getLegacyText()).getJavaFXText();
    }
}

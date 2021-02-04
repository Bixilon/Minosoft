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

import com.google.gson.JsonArray;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

public class TranslatableComponent extends ChatComponent {
    private final ArrayList<ChatComponent> data = new ArrayList<>();
    private final String key;
    private final TextComponent parent;
    private final Version version;

    public TranslatableComponent(Version version, String key, JsonArray data) {
        this(version, null, key, data);
    }

    public TranslatableComponent(Version version, @Nullable TextComponent parent, String key, JsonArray data) {
        this.version = version;
        this.parent = parent;
        this.key = key;
        if (data == null) {
            return;
        }
        data.forEach((jsonElement -> {
            if (jsonElement.isJsonPrimitive()) {
                this.data.add(ChatComponent.valueOf(version, parent, jsonElement.getAsString()));
            } else {
                this.data.add(new BaseComponent(version, parent, jsonElement.getAsJsonObject()));
            }
        }));
    }

    @Override
    public String getANSIColoredMessage() {
        return getList("getANSIColoredMessage");
    }

    @Override
    public String getLegacyText() {
        return getList("getLegacyText");
    }

    @Override
    public String getMessage() {
        return getList("getMessage");
    }

    @Override
    public ObservableList<Node> getJavaFXText(ObservableList<Node> nodes) {
        // ToDo fix nested base component (formatting), not just a string

        // This is just a dirty workaround to enable formatting and coloring. Still need to do hover, click, ... stuff
        return new BaseComponent(this.version, getLegacyText()).getJavaFXText(nodes);
    }

    // just used reflections to not write this twice anc only change the method name
    private String getList(String methodName) {
        try {
            Object[] data = new String[this.data.size()];
            for (int i = 0; i < this.data.size(); i++) {
                data[i] = this.data.get(i).getClass().getMethod(methodName).invoke(this.data.get(i));
            }
            if (this.parent != null) {
                StringBuilder builder = new StringBuilder();
                if (methodName.equals("getANSIColoredMessage")) {
                    builder.append(ChatColors.getANSIColorByRGBColor(this.parent.getColor()));
                } else if (methodName.equals("getLegacyText")) {
                    builder.append(ChatColors.getColorChar(this.parent.getColor()));

                }
                for (ChatFormattingCode code : this.parent.getFormatting()) {
                    if (code instanceof PreChatFormattingCodes preCode) {
                        builder.append(switch (methodName) {
                            case "getANSIColoredMessage" -> preCode.getANSI();
                            case "getLegacyText" -> ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR + preCode.getChar();
                            default -> "";
                        });
                    }
                }
                builder.append(this.version.getLocaleManager().translate(this.key, data));
                for (ChatFormattingCode code : this.parent.getFormatting()) {
                    if (code instanceof PostChatFormattingCodes postCode) {
                        builder.append(switch (methodName) {
                            case "getANSIColoredMessage" -> postCode.getANSI();
                            case "getLegacyText" -> ProtocolDefinition.TEXT_COMPONENT_SPECIAL_PREFIX_CHAR + postCode.getChar();
                            default -> "";
                        });
                    }
                }
                return builder.toString();
            }
            String text = this.version.getLocaleManager().translate(this.key, data);
            if (text == null) {
                // Error, can not translate
                text = "{invalid=true, key=" + this.key + ", data=" + Arrays.toString(data);
            }
            return text;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.text

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.data.locale.minecraft.Translator
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.text.TextGetProperties
import de.bixilon.minosoft.gui.rendering.font.text.TextSetProperties
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.LabelNode
import glm_.vec2.Vec2i
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node

interface ChatComponent {
    /**
     * @return Returns the message formatted with ANSI Formatting codes
     */
    val ansiColoredMessage: String

    /**
     * @return Returns the message formatted with minecraft formatting codes (§)
     */
    val legacyText: String

    /**
     * @return Returns the unformatted message
     */
    val message: String

    /**
     * @return Returns the a list of Nodes, drawable in JavaFX (TextFlow)
     */
    fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node>

    /**
     * @return Returns the a list of Nodes, drawable in JavaFX (TextFlow)
     */
    val javaFXText: ObservableList<Node>
        get() = getJavaFXText(FXCollections.observableArrayList())

    /**
     * Sets a default color for all elements that don't have a color yet
     */
    fun applyDefaultColor(color: RGBColor)


    /**
     * Prepares the chat component for rendering (used in opengl)
     */
    fun prepareRender(startPosition: Vec2i, offset: Vec2i, renderWindow: RenderWindow, textElement: LabelNode, z: Int, setProperties: TextSetProperties, getProperties: TextGetProperties)

    companion object {

        @JvmOverloads
        fun of(raw: Any?, translator: Translator? = null, parent: TextComponent? = null, ignoreJson: Boolean = false): ChatComponent {
            if (raw == null) {
                return BaseComponent()
            }
            if (raw is ChatComponent) {
                return raw
            }
            if (raw is JsonObject) {
                return BaseComponent(translator, parent, raw)
            }
            val string = when (raw) {
                is JsonArray -> {
                    val component = BaseComponent()
                    for (part in raw) {
                        component.parts.add(of(part, translator, parent))
                    }
                    return component
                }
                is JsonPrimitive -> raw.asString
                else -> raw.toString()
            }
            if (!ignoreJson) {
                try {
                    return BaseComponent(translator, parent, JsonParser.parseString(string).asJsonObject)
                } catch (ignored: RuntimeException) {
                }
            }

            return BaseComponent(parent, string)
        }

        fun String.chat(): ChatComponent {
            return of(this)
        }
    }
}

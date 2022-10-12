/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger and contributors
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

import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import javafx.collections.ObservableList
import javafx.scene.Node
object EmptyComponent : ChatComponent {
    override val ansiColoredMessage: String get() = ""
    override val legacyText: String get() = ""
    override val message: String get() = ""

    override fun getJson(): Any = emptyList<Any>()

    override fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node> = nodes

    override fun setFallbackColor(color: RGBColor) = this

    override fun getTextAt(pointer: Int): TextComponent = throw IllegalArgumentException()

    override val length: Int get() = 0

    override fun cut(length: Int) = Unit

    override fun strikethrough() = this
    override fun obfuscate() = this
    override fun bold() = this
    override fun underline() = this
    override fun italic() = this
}

/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import com.fasterxml.jackson.core.JacksonException
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.json.Jackson
import javafx.scene.text.TextFlow

/**
 * Chat components are generally mutable while creating. Once you use it somewhere it is considered as non-mutable.
 */
interface ChatComponent {
    /**
     * @return Returns the message formatted with ANSI Formatting codes
     */
    val ansi: String

    /**
     * @return Returns the message formatted with minecraft formatting codes (§)
     */
    val legacy: String

    /**
     * @return Returns the unformatted message
     */
    val message: String


    fun toJson(): Any
    fun toNbt() = toJson()

    val textFlow: TextFlow
        get() {
            val textFlow = TextFlow()
            textFlow.text = this
            return textFlow
        }

    /**
     * @return The current text component at a specific pointer (char offset)
     */
    fun getTextAt(pointer: Int): TextComponent

    /**
     * The length in chars
     */
    val length: Int


    fun cut(length: Int)
    fun copy(): ChatComponent

    fun trim(): ChatComponent?


    fun strikethrough(): ChatComponent
    fun obfuscate(): ChatComponent
    fun bold(): ChatComponent
    fun underline(): ChatComponent
    fun italic(): ChatComponent
    fun setFallbackColor(color: RGBAColor): ChatComponent
    fun setFallbackColor(color: RGBColor) = setFallbackColor(color.rgba())


    companion object {
        val EMPTY: ChatComponent = EmptyComponent

        fun of(component: ChatComponent) = component
        fun of(component: TextFormattable) = component.toText()


        @JvmOverloads
        fun of(raw: Any? = null, translator: Translator? = null, parent: TextComponent? = null, ignoreJson: Boolean = false, restricted: Boolean = false): ChatComponent {
            if (raw == null) return EMPTY
            if (raw is ChatComponent) return raw
            if (raw is TextFormattable) return of(raw.toText())
            if (raw is Translatable && raw !is ResourceLocation) {
                return (translator ?: IntegratedLanguage.LANGUAGE).forceTranslate(raw.translationKey, parent, restricted = restricted)
            }

            when (raw) {
                is Map<*, *> -> return BaseComponent(translator, parent, raw.unsafeCast(), restricted).trim() ?: EmptyComponent
                is List<*> -> {
                    val component = BaseComponent()
                    for (part in raw) {
                        component += of(part, translator, parent, restricted = restricted).trim() ?: continue
                    }
                    return component.trim() ?: EmptyComponent
                }
            }
            return of(raw.toString(), translator, parent, ignoreJson, restricted)
        }

        @JvmOverloads
        fun of(string: String, translator: Translator? = null, parent: TextComponent? = null, ignoreJson: Boolean = false, restricted: Boolean = false): ChatComponent {
            if (string.isEmpty()) return EMPTY

            if (!ignoreJson) {
                for (codePoint in string.codePoints()) {
                    if (Character.isWhitespace(codePoint)) {
                        continue
                    }
                    if (codePoint != '{'.code && codePoint != '['.code) break

                    try {
                        val read: Any = Jackson.MAPPER.readValue(string, Any::class.java)
                        return of(read, translator, parent, ignoreJson = true, restricted).trim() ?: EmptyComponent
                    } catch (ignored: JacksonException) {
                        break
                    }
                }
            }

            return LegacyComponentReader.parse(parent, string.removeSurrounding("\""), restricted).trim() ?: EmptyComponent
        }

        fun String.chat(): ChatComponent {
            return of(this)
        }
    }
}

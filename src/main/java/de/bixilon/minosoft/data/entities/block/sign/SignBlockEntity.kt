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

package de.bixilon.minosoft.data.entities.block.sign

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.asJsonList
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class SignBlockEntity(session: PlaySession) : BlockEntity(session) {
    val front = SignTextProperties()
    val back = SignTextProperties()
    var waxed = false


    override fun updateNBT(nbt: Map<String, Any>) {
        nbt["is_waxed"]?.toBoolean()?.let { this.waxed = it }
        val front = nbt["front_text"]?.toJsonObject() ?: return updateLegacy(nbt)
        this.front.update(front, session)
        nbt["back_text"]?.toJsonObject()?.let { this.back.update(it, session) }
    }

    private fun updateLegacy(nbt: JsonObject) {
        this.front.update(nbt["Color"], nbt["GlowingText"])
        for (i in 1..LINES) {
            val line = nbt["Text$i"]?.toString() ?: continue

            front.text[i - 1] = ChatComponent.of(line, translator = session.language)
        }
    }

    operator fun get(side: SignSides) = when (side) {
        SignSides.FRONT -> front
        SignSides.BACK -> back
    }

    class SignTextProperties(
        var glowing: Boolean = false,
        var color: RGBColor? = null,
        val text: Array<ChatComponent> = Array(LINES) { EmptyComponent },
    ) {

        private fun parseText(line: Any?, session: PlaySession): ChatComponent {
            if (line is String && line.startsWith("\"")) {
                // TODO: minecraft 1.20.4 wraps it in "???
                return ChatComponent.of(line.removeSurrounding("\""))
            }
            return ChatComponent.of(line, translator = session.language)
        }

        fun update(data: JsonObject, session: PlaySession) {
            update(data["color"], data["has_glowing_text"])
            data["messages"]?.asJsonList()?.let {
                for ((index, line) in it.withIndex()) {
                    this.text[index] = parseText(line, session)
                }
            }
        }

        fun update(color: Any?, glowing: Any?) {
            this.color = color?.toString()?.lowercase()?.let { ChatColors.NAME_MAP[it]?.rgb() }
            this.glowing = glowing?.toBoolean() ?: false
        }
    }

    companion object : BlockEntityFactory<SignBlockEntity> {
        override val identifier: ResourceLocation = minecraft("sign")
        const val LINES = 4

        override fun build(session: PlaySession): SignBlockEntity {
            return SignBlockEntity(session)
        }
    }
}

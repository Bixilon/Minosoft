/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.sign.SignBlockEntityRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class SignBlockEntity(connection: PlayConnection) : MeshedBlockEntity(connection) {
    var lines: Array<ChatComponent> = Array(LINES) { ChatComponent.of("") }
    var color: RGBColor = ChatColors.BLACK
    var glowing = false


    override fun updateNBT(nbt: Map<String, Any>) {
        color = nbt["Color"]?.toString()?.lowercase()?.let { ChatColors.NAME_MAP[it] } ?: ChatColors.BLACK
        glowing = nbt["GlowingText"]?.toBoolean() ?: false
        for (i in 1..LINES) {
            val tag = nbt["Text$i"]?.toString() ?: continue

            lines[i - 1] = ChatComponent.of(tag, translator = connection.language)
        }
    }

    override fun createMeshedRenderer(context: RenderContext, blockState: BlockState, blockPosition: Vec3i): SignBlockEntityRenderer {
        return SignBlockEntityRenderer(this, context, blockState)
    }

    companion object : BlockEntityFactory<SignBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:sign")
        const val LINES = 4

        override fun build(connection: PlayConnection): SignBlockEntity {
            return SignBlockEntity(connection)
        }
    }
}

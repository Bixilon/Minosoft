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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.entities.block.SignBlockEntity
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreaterEquals
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import de.bixilon.minosoft.modding.event.events.OpenSignEditorEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.packets.c2s.play.block.SignTextC2SP

class SignEditorScreen(
    guiRenderer: GUIRenderer,
    val blockPosition: Vec3i,
) : Screen(guiRenderer), AbstractLayout<Element> {
    private val lines = Array(SignBlockEntity.LINES) { TextInputElement(guiRenderer, "< $it >", SignBlockEntity.MAX_LINE_LENGTH, parent = this) }
    private val backgroundElement = AtlasImageElement(guiRenderer, guiRenderer.atlasManager["minecraft:sign_front"])
    override var activeElement: Element? = lines[0]
    override var activeDragElement: Element? = null
    private var activeLine = 0


    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        offset += (size - LINES_SIZE) / 2
        backgroundElement.render(offset, consumer, options)

        for (line in lines) {
            line.render(offset, consumer, options)
            offset.y += Font.TOTAL_CHAR_HEIGHT
        }
    }

    private fun getText(): Array<ChatComponent> {
        val out: Array<ChatComponent?> = arrayOfNulls(SignBlockEntity.LINES)
        for ((index, line) in this.lines.withIndex()) {
            out[index] = ChatComponent.of(line.value)
        }
        return out.unsafeCast()
    }

    override fun onClose() {
        super.onClose()
        val text = getText()
        guiRenderer.connection.sendPacket(SignTextC2SP(blockPosition, text))
    }

    override fun getAt(position: Vec2i): Pair<Element, Vec2i>? {
        val start = (size - LINES_SIZE) / 2
        if (position isSmaller start) {
            return null
        }
        position -= start
        if (position isGreaterEquals LINES_SIZE) {
            return null
        }
        val line = position.y / Font.TOTAL_CHAR_HEIGHT
        position.y %= Font.TOTAL_CHAR_HEIGHT
        return Pair(this.lines[line], position)
    }

    private fun modifyActiveLine(modify: Int) {
        lines[activeLine].hideCursor()
        var activeLine = this.activeLine
        activeLine += modify % lines.size
        if (activeLine < 0) {
            activeLine = lines.size - activeLine
        }
        activeLine %= lines.size
        this.activeLine = activeLine
        this.activeElement = lines[activeLine]
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (type == KeyChangeTypes.RELEASE) {
            if (key == KeyCodes.KEY_UP) {
                modifyActiveLine(-1)
                return true
            } else if (key == KeyCodes.KEY_DOWN) {
                modifyActiveLine(1)
                return true
            }
        }
        return super<AbstractLayout>.onKey(key, type)
    }

    override fun tick() {
        lines[activeLine].tick()
    }

    companion object {
        private val LINES_SIZE = Vec2i(Font.TOTAL_CHAR_HEIGHT * SignBlockEntity.MAX_LINE_LENGTH, Font.TOTAL_CHAR_HEIGHT * SignBlockEntity.LINES)

        fun register(guiRenderer: GUIRenderer) {
            guiRenderer.connection.registerEvent(CallbackEventInvoker.of<OpenSignEditorEvent> { guiRenderer.gui.push(SignEditorScreen(guiRenderer, it.blockPosition)) })
        }
    }
}

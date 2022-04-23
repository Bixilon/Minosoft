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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.entities.block.SignBlockEntity
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreaterEquals
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.sign.SignBlockEntityRenderer
import de.bixilon.minosoft.modding.event.events.OpenSignEditorEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.packets.c2s.play.block.SignTextC2SP

class SignEditorScreen(
    guiRenderer: GUIRenderer,
    val blockPosition: Vec3i,
    val blockEntity: SignBlockEntity? = null,
) : Screen(guiRenderer), AbstractLayout<Element> {
    private val header = TextElement(guiRenderer, "Edit sign message", background = false, scale = 3.0f, parent = this)
    private val backgroundElement = ImageElement(guiRenderer, guiRenderer.atlasManager["minecraft:sign_front"]?.texture, uvStart = SIGN_UV_START, uvEnd = SIGN_UV_END, size = BACKGROUND_SIZE)
    private val lines = Array(SignBlockEntity.LINES) { TextInputElement(guiRenderer, "<- $it. ->", 256, scale = TEXT_SCALE, background = false, cutAtSize = true, parent = this).apply { prefMaxSize = Vec2i(SignBlockEntityRenderer.SIGN_MAX_WIDTH * TEXT_SCALE, Font.TOTAL_CHAR_HEIGHT * TEXT_SCALE) } }
    private val doneButton = ButtonElement(guiRenderer, "Done") { guiRenderer.gui.pop() }.apply { size = Vec2i(BACKGROUND_SIZE.x, size.y);parent = this@SignEditorScreen }
    override var activeElement: Element? = lines[0]
    override var activeDragElement: Element? = null
    private var activeLine = 0

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val size = size

        offset.y += size.y / 8
        header.render(offset + HorizontalAlignments.CENTER.getOffset(size, header.size), consumer, options)

        offset.y += size.y / 10
        backgroundElement.render(offset + HorizontalAlignments.CENTER.getOffset(size, backgroundElement.size), consumer, options)

        offset.y += (1.5f * BACKGROUND_SCALE).toInt()
        for (line in lines) {
            line.render(offset + HorizontalAlignments.CENTER.getOffset(size, line.size), consumer, options)
            offset.y += (Font.TOTAL_CHAR_HEIGHT * TEXT_SCALE).toInt()
        }
        offset.y += size.y / 8

        doneButton.render(offset + HorizontalAlignments.CENTER.getOffset(size, doneButton.size), consumer, options)
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
        val height = (Font.TOTAL_CHAR_HEIGHT * TEXT_SCALE).toInt()
        val line = position.y / height
        position.y %= height
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
        return lines[activeLine].onKey(key, type)
    }

    override fun onCharPress(char: Int): Boolean {
        return lines[activeLine].onCharPress(char)
    }


    override fun tick() {
        lines[activeLine].tick()
    }

    companion object {
        private val SIGN_UV_START = Vec2(0.5 / 16.0f, 1.0f / 32.0f)
        private val SIGN_UV_END = Vec2(6.5 / 16.0f, 7.0f / 32.0f)
        private const val TEXT_SCALE = 2.0f
        private val LINES_SIZE = Vec2i(SignBlockEntityRenderer.SIGN_MAX_WIDTH * TEXT_SCALE, Font.TOTAL_CHAR_HEIGHT * SignBlockEntity.LINES * TEXT_SCALE)

        private const val BACKGROUND_SCALE = 9
        private val BACKGROUND_SIZE = Vec2i(24, 12) * BACKGROUND_SCALE

        fun register(guiRenderer: GUIRenderer) {
            guiRenderer.connection.registerEvent(CallbackEventInvoker.of<OpenSignEditorEvent> { guiRenderer.gui.push(SignEditorScreen(guiRenderer, it.blockPosition, it.connection.world.getBlockEntity(it.blockPosition).nullCast())) })
        }
    }
}

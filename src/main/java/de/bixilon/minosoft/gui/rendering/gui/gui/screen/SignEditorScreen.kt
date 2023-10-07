/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.entities.block.sign.SignBlockEntity
import de.bixilon.minosoft.data.entities.block.sign.SignSides
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.sign.SignBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.sign.SignBlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.input.checkbox.SwitchElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout.Companion.getAtCheck
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.modding.event.events.OpenSignEditorEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.packets.c2s.play.block.SignTextC2SP

class SignEditorScreen(
    guiRenderer: GUIRenderer,
    val blockPosition: Vec3i,
    val side: SignSides,
    val blockState: BlockState? = guiRenderer.connection.world[blockPosition],
    val blockEntity: SignBlockEntity? = guiRenderer.connection.world.getBlockEntity(blockPosition).nullCast(),
) : Screen(guiRenderer), AbstractLayout<Element> {
    private val headerElement = TextElement(guiRenderer, "Edit sign message", background = null, properties = TextRenderProperties(scale = 3.0f), parent = this)
    private val positionElement = TextElement(guiRenderer, "at $blockPosition", background = null, parent = this)
    private val backgroundElement = getFront()
    private val lines = Array(SignBlockEntity.LINES) { TextInputElement(guiRenderer, blockEntity?.lines?.get(it)?.message ?: "", SIGN_MAX_CHARS, properties = TEXT_PROPERTIES, background = null, cutAtSize = true, parent = this) }
    private val doneButton = ButtonElement(guiRenderer, "Done") { guiRenderer.gui.pop() }.apply { size = Vec2(BACKGROUND_SIZE.x, size.y);parent = this@SignEditorScreen }
    private val lengthLimitSwitch = SwitchElement(guiRenderer, "Limit length", guiRenderer.connection.profiles.gui.sign.limitLength, parent = this) { guiRenderer.connection.profiles.gui.sign.limitLength = it; forceSilentApply() }
    override var activeElement: Element? = null
    override var activeDragElement: Element? = null
    private var activeLine = 0

    init {
        for (line in lines) {
            line.prefMaxSize = Vec2(SignBlockEntityRenderer.SIGN_MAX_WIDTH * TEXT_PROPERTIES.scale, TEXT_PROPERTIES.lineHeight)
            line.hideCursor()
        }
        forceSilentApply()
    }

    private fun getFallbackFront(): AtlasImageElement? {
        val atlas = guiRenderer.atlas[ATLAS]?.get("front") ?: return null
        return AtlasImageElement(guiRenderer, atlas, size = BACKGROUND_SIZE)
    }

    private fun getFront(): Element? {
        if (blockState?.block !is SignBlock) {
            return getFallbackFront()
        }
        val texture = (blockState.model ?: blockState.block.model).nullCast<BakedModel>()?.faces?.firstOrNull()?.firstOrNull()?.texture ?: return getFallbackFront() // TODO: test
        return ImageElement(guiRenderer, texture, uvStart = SIGN_UV_START, uvEnd = SIGN_UV_END, size = BACKGROUND_SIZE)
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        lengthLimitSwitch.render(offset + VerticalAlignments.BOTTOM.getOffset(size, lengthLimitSwitch.size), consumer, options)

        val size = size

        offset.y += size.y / 8
        headerElement.render(offset + HorizontalAlignments.CENTER.getOffset(size, headerElement.size), consumer, options)
        offset.y += headerElement.size.y

        offset.y += size.y / 100
        positionElement.render(offset + HorizontalAlignments.CENTER.getOffset(size, positionElement.size), consumer, options)
        offset.y += positionElement.size.y

        offset.y += size.y / 12
        backgroundElement?.render(offset + HorizontalAlignments.CENTER.getOffset(size, backgroundElement.size), consumer, options)

        offset.y += (1.8f * BACKGROUND_SCALE).toInt()
        for (line in lines) {
            line.render(offset + HorizontalAlignments.CENTER.getOffset(size, line.size), consumer, options)
            offset.y += TEXT_PROPERTIES.lineHeight + TEXT_PROPERTIES.lineSpacing
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
        guiRenderer.connection.sendPacket(SignTextC2SP(blockPosition, this.side, text))
    }

    override fun forceSilentApply() {
        super.forceSilentApply()

        for (line in lines) {
            line.prefMaxSize = Vec2(if (lengthLimitSwitch.state) SignBlockEntityRenderer.SIGN_MAX_WIDTH * TEXT_PROPERTIES.scale else SIGN_MAX_CHARS, line.prefMaxSize.y)
        }
    }

    override fun getAt(position: Vec2): Pair<Element, Vec2>? {
        val size = size

        if (position.y in size.y - lengthLimitSwitch.size.y..size.y) {
            position.y -= size.y - lengthLimitSwitch.size.y
            getAtCheck(position, lengthLimitSwitch, HorizontalAlignments.LEFT, false)?.let { return it }
        }

        position.y -= size.y / 8
        getAtCheck(position, headerElement, HorizontalAlignments.CENTER, true)?.let { return it }
        if (position.y < 0) {
            return null
        }

        position.y -= size.y / 100
        getAtCheck(position, positionElement, HorizontalAlignments.CENTER, true)?.let { return it }
        if (position.y < 0) {
            return null
        }
        position.y -= size.y / 12
        position.y -= (1.8f * BACKGROUND_SCALE).toInt()

        for (line in lines) {
            getAtCheck(position, line, HorizontalAlignments.CENTER, true, Vec2(SignBlockEntityRenderer.SIGN_MAX_WIDTH * TEXT_PROPERTIES.scale, TEXT_PROPERTIES.lineHeight))?.let { return it }
            if (position.y < 0) {
                return null
            }
        }
        position.y -= size.y / 8
        getAtCheck(position, doneButton, HorizontalAlignments.CENTER, true)?.let { return it }
        return null
    }

    private fun modifyActiveLine(modify: Int) {
        lines[activeLine].hideCursor()
        lines[activeLine].unmark()
        var activeLine = this.activeLine
        activeLine += modify % lines.size
        if (activeLine < 0) {
            activeLine += lines.size
        }
        activeLine %= lines.size
        this.activeLine = activeLine
        lines[activeLine].showCursor()
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (type == KeyChangeTypes.RELEASE) {
            if (key == KeyCodes.KEY_UP) {
                modifyActiveLine(-1)
                return true
            } else if (key == KeyCodes.KEY_DOWN || key == KeyCodes.KEY_ENTER || key == KeyCodes.KEY_KP_ENTER || key == KeyCodes.KEY_TAB) {
                modifyActiveLine(1)
                return true
            }
        }
        return lines[activeLine].onKey(key, type)
    }

    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        val (element, offset) = getAt(position) ?: return false
        val lineIndex = lines.indexOf(element)
        if (element is TextInputElement && lineIndex >= 0 && lineIndex != this.activeLine) {
            val activeLine = lines[this.activeLine]
            activeLine.hideCursor()
            activeLine.unmark()
            this.activeLine = lineIndex
            element.showCursor()
        }
        return element.onMouseAction(offset, button, action, count)
    }

    override fun onCharPress(char: Int): Boolean {
        return lines[activeLine].onCharPress(char)
    }


    override fun tick() {
        lines[activeLine].tick()
    }

    companion object {
        private val ATLAS = minecraft("block/sign")
        private val TEXT_PROPERTIES = TextRenderProperties(scale = 2.0f, allowNewLine = false)
        private val SIGN_UV_START = Vec2(0.5f / 16.0f, 1.0f / 32.0f)
        private val SIGN_UV_END = Vec2(6.5f / 16.0f, 7.0f / 32.0f)
        private const val SIGN_MAX_CHARS = 384

        private const val BACKGROUND_SCALE = 9
        private val BACKGROUND_SIZE = Vec2(24, 12) * BACKGROUND_SCALE

        fun register(guiRenderer: GUIRenderer) {
            guiRenderer.atlas.load(ATLAS)
            guiRenderer.connection.events.listen<OpenSignEditorEvent> { guiRenderer.gui.push(SignEditorScreen(guiRenderer, it.position, it.side)) }
        }
    }
}

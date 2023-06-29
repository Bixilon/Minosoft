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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.commands.nodes.ChatNode
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.chat.message.internal.InternalChatMessage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.node.NodeTextInputElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering

class ChatElement(guiRenderer: GUIRenderer) : AbstractChatElement(guiRenderer), LayoutedElement {
    private val chatProfile = profile.chat
    private val input = NodeTextInputElement(guiRenderer, ChatNode("", allowCLI = true), maxLength = connection.version.maxChatMessageSize).apply { parent = this@ChatElement }
    private val internal = InternalChatElement(guiRenderer).apply { parent = this@ChatElement }
    private val history: MutableList<String> = mutableListOf()
    private var historyIndex = -1
    private var active = false
        set(value) {
            field = value
            messages._active = value
            messages.forceSilentApply()
            historyIndex = history.size + 1
            forceApply()
        }
    override var skipDraw: Boolean
        // skips hud draw and draws it in gui stage
        get() = active || chatProfile.hidden
        set(value) {
            chatProfile.hidden = !value
        }
    override val activeWhenHidden: Boolean
        get() = true

    override val layoutOffset: Vec2
        get() = Vec2(0, guiRenderer.scaledSize.y - maxOf(messages.size.y, internal.size.y) - (LINES * TEXT_PROPERTIES.lineHeight) - CHAT_INPUT_MARGIN * 2)


    init {
        messages.prefMaxSize = Vec2(chatProfile.width, chatProfile.height)
        chatProfile::width.observeRendering(this, context = context) { messages.prefMaxSize = Vec2(it, messages.prefMaxSize.y) }
        chatProfile::height.observeRendering(this, context = context) { messages.prefMaxSize = Vec2(messages.prefMaxSize.x, it) }
        forceSilentApply()
        input.onChangeCallback = {
            while (input._value.startsWith(' ')) {
                input._value.deleteCharAt(0)
                input._pointer--
                input.onChange()
            }
        }
    }


    override fun init() {
        connection.events.listen<ChatMessageEvent> {
            if (it.message.type.position == ChatTextPositions.HOTBAR) {
                return@listen
            }
            if (it.message is InternalChatMessage && !profile.chat.internal.hidden) {
                // message will be displayed in internal chat
                return@listen
            }
            // TODO: offload on single thread
            messages += it.message.text
        }

        context.input.registerKeyCallback(
            "minosoft:open_chat".toResourceLocation(), KeyBinding(
                KeyActions.PRESS to setOf(KeyCodes.KEY_T),
            )
        ) { guiRenderer.gui.open(ChatElement) }

        context.input.registerKeyCallback(
            minosoft("open_command_chat"),
            KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_SLASH))
        ) {
            guiRenderer.gui.open(ChatElement)
            this.input.value = "/"
        }

        internal.init()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        var messagesYStart = 0.0f
        val messagesSize = messages.size
        val size = size
        if (!chatProfile.internal.hidden) {
            val internalSize = internal.size
            val internalStart = if (internalSize.y > messagesSize.y) {
                messagesYStart = internalSize.y - messagesSize.y
                0
            } else {
                messagesSize.y - internalSize.y
            }

            internal.render(offset + Vec2(size.x - internal.size.x, internalStart), consumer, options)
        }
        super.forceRender(offset + Vec2(0, messagesYStart), consumer, options)

        if (active) {
            input.render(offset + Vec2(CHAT_INPUT_MARGIN, size.y - (CHAT_INPUT_MARGIN + (LINES * TEXT_PROPERTIES.lineHeight))), consumer, options)
        }
    }

    override fun forceSilentApply() {
        messages.silentApply()
        _size = Vec2(guiRenderer.scaledSize.x, maxOf(messages.size.y, internal.size.y) + (LINES * TEXT_PROPERTIES.lineHeight) + CHAT_INPUT_MARGIN * 2)
        if (active) {
            input.prefMaxSize = Vec2(size.x - CHAT_INPUT_MARGIN * 2, (LINES * TEXT_PROPERTIES.lineHeight))
            input.forceSilentApply()
        }
        internal.forceSilentApply()
        cacheUpToDate = false
    }

    override fun onOpen() {
        active = true
        input.onOpen()
        messages.onOpen()
        if (!chatProfile.internal.hidden) {
            internal.onOpen()
        }
    }

    override fun onClose() {
        active = false
        input.value = ""
        input.onClose()
        messages.onClose()
        internal.onClose()
    }

    override fun onCharPress(char: Int): Boolean {
        if (char == '§'.code) {
            input.onCharPress('&'.code)
        } else {
            input.onCharPress(char)
        }
        return true
    }

    private fun submit() {
        val value = input.value
        input.submit()
        input.value = ""
        if (history.lastOrNull() != value) {
            // ToDo: Improve history
            history += value
        }
        historyIndex = history.size
        guiRenderer.gui.pop()
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (input.onKey(key, type)) {
            return true
        }
        if (type != KeyChangeTypes.RELEASE) {
            when (key) {
                KeyCodes.KEY_ENTER -> submit()
                KeyCodes.KEY_PAGE_UP -> messages.scrollOffset++
                KeyCodes.KEY_PAGE_DOWN -> messages.scrollOffset--
                KeyCodes.KEY_UP -> {
                    val size = history.size
                    if (historyIndex > size) {
                        historyIndex = size
                    }
                    if (historyIndex <= 0) {
                        return true
                    }
                    historyIndex--
                    input.value = history[historyIndex]
                }
                KeyCodes.KEY_DOWN -> {
                    val size = history.size
                    historyIndex++
                    if (historyIndex > size) {
                        return true
                    }
                    if (historyIndex == size) {
                        input.value = ""
                        return true
                    }
                    input.value = history[historyIndex]
                }

                else -> return super.onKey(key, type)
            }
        }

        return true
    }

    override fun getAt(position: Vec2): Pair<Element, Vec2>? {
        var messagesYStart = 0.0f
        val messagesSize = messages.size
        if (!chatProfile.internal.hidden) {
            val internalSize = internal.size
            val internalStart = if (internalSize.y > messagesSize.y) {
                messagesYStart = internalSize.y - messagesSize.y
                0
            } else {
                messagesSize.y - internalSize.y
            }
            val internalPosition = position - Vec2i(size.x - internalSize.x, internalStart)
            if (internalPosition.x in 0.0f..internalSize.x && internalPosition.y in 0.0f..internalSize.y) {
                return Pair(internal, internalPosition)
            }
        }

        if (position.x < CHAT_INPUT_MARGIN) {
            return null
        }
        val offset = Vec2(position)
        offset.y -= messagesYStart
        offset.x -= CHAT_INPUT_MARGIN

        if (offset.y < messagesSize.y) {
            if (offset.x > messagesSize.x) {
                return null
            }
            return Pair(messages, offset)
        }
        offset.y -= messagesSize.y

        if (offset.y < CHAT_INPUT_MARGIN) {
            return null
        }
        val inputSize = input.size
        if (offset.y < inputSize.y) {
            if (offset.x > inputSize.x) {
                return null
            }
            return Pair(input, offset)
        }
        return null
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
        super.onChildChange(child)
    }

    override fun tick() {
        super.tick()
        internal.tick()
        input.tick()
    }

    companion object : HUDBuilder<LayoutedGUIElement<ChatElement>>, GUIBuilder<LayoutedGUIElement<ChatElement>> {
        override val identifier: ResourceLocation = "minosoft:chat_hud".toResourceLocation()
        private val TEXT_PROPERTIES = TextRenderProperties()
        const val LINES = 3
        const val CHAT_INPUT_MARGIN = 2

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ChatElement> {
            return LayoutedGUIElement(ChatElement(guiRenderer))
        }
    }
}

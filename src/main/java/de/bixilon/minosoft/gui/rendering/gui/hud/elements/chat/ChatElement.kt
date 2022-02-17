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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatchRendering
import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class ChatElement(guiRenderer: GUIRenderer) : AbstractChatElement(guiRenderer) {
    private val chatProfile = profile.chat
    private val input = TextInputElement(guiRenderer, maxLength = connection.version.maxChatMessageSize).apply { parent = this@ChatElement }
    private val history: MutableList<String> = mutableListOf()
    private var historyIndex = -1
    private var active = false
        set(value) {
            field = value
            messages._active = value
            messages.forceSilentApply()
            historyIndex = -1
            forceApply()
        }
    override var skipDraw: Boolean
        get() = chatProfile.hidden || active
        set(value) {
            chatProfile.hidden = !value
        }
    override val activeWhenHidden: Boolean
        get() = true

    override val layoutOffset: Vec2i
        get() = Vec2i(0, guiRenderer.scaledSize.y - messages.size.y - CHAT_INPUT_HEIGHT - CHAT_INPUT_MARGIN * 2)

    init {
        messages.prefMaxSize = Vec2i(chatProfile.width, chatProfile.height)
        chatProfile::width.profileWatchRendering(this, profile = profile) { messages.prefMaxSize = Vec2i(it, messages.prefMaxSize.y) }
        chatProfile::height.profileWatchRendering(this, profile = profile) { messages.prefMaxSize = Vec2i(messages.prefMaxSize.x, it) }
        forceSilentApply()
        input.onChange = {
            while (input._value.startsWith(' ')) {
                input._value.deleteCharAt(0)
                input._pointer--
            }
        }
    }


    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ChatMessageReceiveEvent> {
            if (it.position == ChatTextPositions.ABOVE_HOTBAR) {
                return@of
            }
            DefaultThreadPool += { messages += it.message }
        })
        connection.registerEvent(CallbackEventInvoker.of<InternalMessageReceiveEvent> {
            if (!profile.chat.internal.hidden) {
                return@of
            }
            DefaultThreadPool += { messages += it.message }
        })

        renderWindow.inputHandler.registerKeyCallback("minosoft:open_chat".toResourceLocation(), KeyBinding(
            mapOf(
                KeyAction.PRESS to setOf(KeyCodes.KEY_T),
            ),
        )) { guiRenderer.gui.open(ChatElement) }
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        if (active) {
            input.render(offset + Vec2i(CHAT_INPUT_MARGIN, size.y - (CHAT_INPUT_MARGIN + CHAT_INPUT_HEIGHT)), consumer, options)
        }
    }

    override fun forceSilentApply() {
        messages.silentApply()
        _size = Vec2i(0, messages.size.y + CHAT_INPUT_HEIGHT + CHAT_INPUT_MARGIN * 2)
        if (active) {
            _size.x = guiRenderer.scaledSize.x
            input.prefMaxSize = Vec2i(size.x - CHAT_INPUT_MARGIN * 2, CHAT_INPUT_HEIGHT)
            input.forceSilentApply()
        } else {
            _size.x = messages.prefMaxSize.x
        }
        cacheUpToDate = false
    }

    override fun onOpen() {
        active = true
        input.onOpen()
        messages.onOpen()
        if (!chatProfile.internal.hidden) {
            guiRenderer.gui.push(InternalChatElement) // also open internal chat paralell
        }
    }

    override fun onClose() {
        active = false
        input.value = ""
        input.onClose()
        messages.onClose()
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
        if (value.isNotBlank()) {
            connection.util.sendChatMessage(value)
        }
        input.value = ""
        if (history.lastOrNull() != value) {
            // ToDo: Improve history
            history += value
        }
        historyIndex = history.size
        guiRenderer.gui.pop()
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (type != KeyChangeTypes.RELEASE) {
            when (key) {
                KeyCodes.KEY_ENTER -> {
                    submit()
                    return true
                }
                KeyCodes.KEY_PAGE_UP -> {
                    messages.scrollOffset++
                    return true
                }
                KeyCodes.KEY_PAGE_DOWN -> {
                    messages.scrollOffset--
                    return true
                }
                KeyCodes.KEY_UP -> {
                    if (historyIndex <= 0) {
                        return true
                    }
                    val size = history.size
                    if (historyIndex > size) {
                        historyIndex = size
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
                else -> {}
            }
        }
        input.onKey(key, type)

        return true
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions): Boolean {
        val pair = getAt(position) ?: return false
        pair.first.onMouseAction(pair.second, button, action)
        return true
    }

    private fun getAt(position: Vec2i): Pair<Element, Vec2i>? {
        if (position.x < CHAT_INPUT_MARGIN) {
            return null
        }
        val offset = Vec2i(position)
        offset.x -= CHAT_INPUT_MARGIN

        val messagesSize = messages.size
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
    }

    override fun tick() {
        super.tick()
        input.tick()
    }

    companion object : HUDBuilder<LayoutedGUIElement<ChatElement>>, GUIBuilder<LayoutedGUIElement<ChatElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:chat_hud".toResourceLocation()
        const val CHAT_INPUT_HEIGHT = Font.TOTAL_CHAR_HEIGHT * 3 + Font.CHAR_MARGIN * 2
        const val CHAT_INPUT_MARGIN = 2

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ChatElement> {
            return LayoutedGUIElement(ChatElement(guiRenderer))
        }
    }
}

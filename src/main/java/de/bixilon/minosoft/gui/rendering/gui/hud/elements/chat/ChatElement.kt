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

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatchRendering
import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextFlowElement
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.input.InputSpecialKey
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class ChatElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable, Drawable {
    private val connection = renderWindow.connection
    private val profile = connection.profiles.hud
    private val chatProfile = profile.chat
    private val messages = TextFlowElement(guiRenderer, 20000).apply { parent = this@ChatElement }
    private val input = TextInputElement(guiRenderer, maxLength = 256)
    private var active = false
        set(value) {
            field = value
            messages.active = value
            forceApply()
        }
    override var skipDraw: Boolean
        get() = chatProfile.hidden || active
        set(value) {
            chatProfile.hidden = !value
        }

    override val layoutOffset: Vec2i
        get() = Vec2i(0, guiRenderer.scaledSize.y - messages.size.y - CHAT_INPUT_HEIGHT - CHAT_INPUT_MARGIN * 2)

    init {
        messages.prefMaxSize = Vec2i(chatProfile.width, chatProfile.height)
        chatProfile::width.profileWatchRendering(this, profile = profile) { messages.prefMaxSize = Vec2i(it, messages.prefMaxSize.y) }
        chatProfile::height.profileWatchRendering(this, profile = profile) { messages.prefMaxSize = Vec2i(messages.prefMaxSize.x, it) }
        forceSilentApply()
    }


    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ChatMessageReceiveEvent> {
            if (it.position == ChatTextPositions.ABOVE_HOTBAR) {
                return@of
            }
            messages += it.message
        })
        connection.registerEvent(CallbackEventInvoker.of<InternalMessageReceiveEvent> {
            if (!profile.chat.internal.hidden) {
                return@of
            }
            messages += it.message
        })

        renderWindow.inputHandler.registerKeyCallback("minosoft:open_chat".toResourceLocation(), KeyBinding(
            mapOf(
                KeyAction.PRESS to setOf(KeyCodes.KEY_T),
            ),
        )) { guiRenderer.gui.open(ChatElement) }
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        var maxZ = messages.render(offset + Vec2i(CHAT_INPUT_MARGIN, 0), z, consumer, options)
        if (active) {
            maxZ = maxOf(maxZ, input.render(offset + Vec2i(CHAT_INPUT_MARGIN, size.y - (CHAT_INPUT_MARGIN + CHAT_INPUT_HEIGHT)), z, consumer, options))
        }

        return maxZ
    }

    override fun forceSilentApply() {
        _size = Vec2i(0, messages.size.y + CHAT_INPUT_HEIGHT + CHAT_INPUT_MARGIN * 2)
        if (active) {
            _size.x = guiRenderer.scaledSize.x
            input.prefMaxSize = Vec2i(size.x - CHAT_INPUT_MARGIN * 2, CHAT_INPUT_HEIGHT)
            input.forceSilentApply()
        } else {
            _size.x = messages.prefMaxSize.x
        }
        messages.silentApply()
    }

    override fun onOpen() {
        active = true
    }

    override fun onClose() {
        active = false
        input.value = ""
    }

    override fun onHide() {
        active = false
        input.value = ""
    }

    override fun onCharPress(char: Int) {
        if (char == '§'.code) {
            return input.onCharPress('&'.code)
        }
        input.onCharPress(char)
    }

    override fun onSpecialKey(key: InputSpecialKey, type: KeyChangeTypes) {
        if (key == InputSpecialKey.KEY_ENTER && type == KeyChangeTypes.PRESS) {
            if (input.value.isNotBlank()) {
                connection.sendPacket(ChatMessageC2SP(input.value))
                input.value = ""
            }
            guiRenderer.gui.pop()
        }
        input.onSpecialKey(key, type)
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
    }

    override fun tick() {
        messages.tick()
        input.tick()
    }


    companion object : HUDBuilder<LayoutedGUIElement<ChatElement>>, GUIBuilder<LayoutedGUIElement<ChatElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:chat_hud".toResourceLocation()
        private const val CHAT_INPUT_HEIGHT = Font.TOTAL_CHAR_HEIGHT * 3 + Font.CHAR_MARGIN * 2
        private const val CHAT_INPUT_MARGIN = 2

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ChatElement> {
            return LayoutedGUIElement(ChatElement(guiRenderer))
        }
    }
}

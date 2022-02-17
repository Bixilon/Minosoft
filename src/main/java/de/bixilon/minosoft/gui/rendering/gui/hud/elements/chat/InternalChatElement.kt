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
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatchRendering
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class InternalChatElement(guiRenderer: GUIRenderer) : AbstractChatElement(guiRenderer) {
    private val chatProfile = profile.chat.internal
    private var active = false
        set(value) {
            field = value
            messages._active = value
            messages.forceSilentApply()
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
        get() = messages.size.let { Vec2i(guiRenderer.scaledSize.x - it.x, guiRenderer.scaledSize.y - it.y - ChatElement.CHAT_INPUT_HEIGHT - ChatElement.CHAT_INPUT_MARGIN * 2) }

    init {
        messages.prefMaxSize = Vec2i(chatProfile.width, chatProfile.height)
        chatProfile::width.profileWatchRendering(this, profile = profile) { messages.prefMaxSize = Vec2i(it, messages.prefMaxSize.y) }
        chatProfile::height.profileWatchRendering(this, profile = profile) { messages.prefMaxSize = Vec2i(messages.prefMaxSize.x, it) }
        forceSilentApply()
    }


    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<InternalMessageReceiveEvent> {
            if (profile.chat.internal.hidden) {
                return@of
            }
            DefaultThreadPool += { messages += it.message }
        })
    }

    override fun forceSilentApply() {
        messages.silentApply()
        _size = Vec2i(messages.prefMaxSize.x, messages.size.y + ChatElement.CHAT_INPUT_HEIGHT + ChatElement.CHAT_INPUT_MARGIN * 2)
        cacheUpToDate = false
    }

    override fun onOpen() {
        active = true
        messages.onOpen()
    }

    override fun onClose() {
        active = false
        messages.onClose()
        guiRenderer.gui.pop() // pop normal chat
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions) {
        val pair = getAt(position) ?: return
        pair.first.onMouseAction(pair.second, button, action)
    }

    private fun getAt(position: Vec2i): Pair<Element, Vec2i>? {
        if (position.x < ChatElement.CHAT_INPUT_MARGIN) {
            return null
        }
        val offset = Vec2i(position)
        offset.x -= ChatElement.CHAT_INPUT_MARGIN

        val messagesSize = messages.size
        if (offset.y < messagesSize.y) {
            if (offset.x > messagesSize.x) {
                return null
            }
            return Pair(messages, offset)
        }
        offset.y -= messagesSize.y
        return null
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
    }

    companion object : HUDBuilder<LayoutedGUIElement<InternalChatElement>>, GUIBuilder<LayoutedGUIElement<InternalChatElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:internal_chat_hud".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<InternalChatElement> {
            return LayoutedGUIElement(InternalChatElement(guiRenderer))
        }
    }
}

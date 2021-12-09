/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatchRendering
import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextFlowElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.InternalMessageReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class ChatHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<TextFlowElement>(hudRenderer) {
    private val connection = renderWindow.connection
    private val profile = connection.profiles.hud
    private val chatProfile = profile.chat
    override val layout = TextFlowElement(hudRenderer, 20000)
    override var enabled: Boolean
        get() = !chatProfile.hidden
        set(value) {
            chatProfile.hidden = !value
        }


    override val layoutOffset: Vec2i
        get() = Vec2i(2, hudRenderer.scaledSize.y - layout.size.y - BOTTOM_OFFSET)

    init {
        layout.prefMaxSize = Vec2i(chatProfile.width, chatProfile.height)
        chatProfile::width.profileWatchRendering(this, profile = profile) { layout.prefMaxSize = Vec2i(it, layout.prefMaxSize.y) }
        chatProfile::height.profileWatchRendering(this, profile = profile) { layout.prefMaxSize = Vec2i(layout.prefMaxSize.x, it) }
    }


    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ChatMessageReceiveEvent> {
            if (it.position == ChatTextPositions.ABOVE_HOTBAR) {
                return@of
            }
            layout += it.message
        })
        connection.registerEvent(CallbackEventInvoker.of<InternalMessageReceiveEvent> {
            if (!profile.chat.internal.hidden) {
                return@of
            }
            layout += it.message
        })
    }


    companion object : HUDBuilder<ChatHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:chat_hud".toResourceLocation()
        private const val BOTTOM_OFFSET = 30

        override fun build(hudRenderer: HUDRenderer): ChatHUDElement {
            return ChatHUDElement(hudRenderer)
        }
    }
}

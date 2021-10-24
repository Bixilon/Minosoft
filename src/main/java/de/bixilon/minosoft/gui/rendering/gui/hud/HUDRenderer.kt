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

package de.bixilon.minosoft.gui.rendering.gui.hud

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasManager
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat.ChatHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat.InternalMessagesHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.HotbarHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.BreakProgressHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.CrosshairHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.DebugHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.WorldInfoHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab.TabListHUDElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class HUDRenderer(
    val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    val shader = renderWindow.renderSystem.createShader("minosoft:hud".toResourceLocation())
    private lateinit var mesh: GUIMesh
    var scaledSize: Vec2i = renderWindow.window.size
    var matrix: Mat4 = Mat4()
    private var enabled = true
    var matrixChange = true
        private set

    private val hudElements: MutableMap<ResourceLocation, HUDElement> = synchronizedMapOf()

    private var lastTickTime = 0L

    val atlasManager = HUDAtlasManager(this)

    fun registerElement(hudBuilder: HUDBuilder<*>) {
        val hudElement = hudBuilder.build(this)
        hudElements[hudBuilder.RESOURCE_LOCATION] = hudElement

        val toggleKeyBinding = hudBuilder.ENABLE_KEY_BINDING ?: return
        val toggleKeyBindingName = hudBuilder.ENABLE_KEY_BINDING_NAME ?: return

        // ToDo: Default disabled elements like the debug screen?

        renderWindow.inputHandler.registerKeyCallback(toggleKeyBindingName, toggleKeyBinding) { hudElement.enabled = it }
    }

    private fun registerDefaultElements() {
        registerElement(DebugHUDElement)
        if (Minosoft.config.config.game.hud.crosshair.enabled) {
            registerElement(CrosshairHUDElement)
        }
        if (Minosoft.config.config.game.hud.chat.enabled) {
            registerElement(ChatHUDElement)
        }
        if (Minosoft.config.config.game.hud.internalMessages.enabled) {
            registerElement(InternalMessagesHUDElement)
        }
        registerElement(TabListHUDElement)
        registerElement(BreakProgressHUDElement)
        registerElement(HotbarHUDElement)
        registerElement(WorldInfoHUDElement)
    }

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            scaledSize = Vec2i(Vec2(it.size) / Minosoft.config.config.game.hud.scale)
            matrix = glm.ortho(0.0f, scaledSize.x.toFloat(), scaledSize.y.toFloat(), 0.0f)
            matrixChange = true

            for (element in hudElements.toSynchronizedMap().values) {
                if (element is LayoutedHUDElement<*>) {
                    element.layout.silentApply()
                }
                element.apply()
            }
        })
        atlasManager.init()

        registerDefaultElements()

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.init()
        }

        renderWindow.inputHandler.registerKeyCallback("minosoft:enable_hud".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.STICKY_INVERTED to mutableSetOf(KeyCodes.KEY_F1),
            ),
        )) { enabled = it }
    }

    override fun postInit() {
        atlasManager.postInit()
        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.postInit()
        }
    }

    override fun postDraw() {
        if (!enabled) {
            return
        }
        if (this::mesh.isInitialized) {
            mesh.unload()
        }

        mesh = GUIMesh(renderWindow, matrix)
        val hudElements = hudElements.toSynchronizedMap().values

        val time = System.currentTimeMillis()
        if (time - lastTickTime > ProtocolDefinition.TICK_TIME) {
            for (element in hudElements) {
                if (!element.enabled) {
                    continue
                }
                element.tick()
                if (element is Pollable) {
                    if (element.poll()) {
                        element.apply()
                    }
                }
            }

            lastTickTime = time
        }

        renderWindow.renderSystem.reset()
        renderWindow.renderSystem.clear(IntegratedBufferTypes.DEPTH_BUFFER)

        var z = 0
        for (element in hudElements) {
            if (!element.enabled) {
                continue
            }
            if (element is Drawable) {
                element.draw()
            }
            if (element is LayoutedHUDElement<*>) {
                z += element.layout.render(element.layoutOffset, z, mesh, null)
            }
        }


        renderWindow.renderSystem.reset()
        renderWindow.renderSystem.clear(IntegratedBufferTypes.DEPTH_BUFFER)

        mesh.load()
        shader.use()
        mesh.draw()

        if (matrixChange) {
            matrixChange = false
        }
    }

    operator fun <T : LayoutedHUDElement<*>> get(hudBuilder: HUDBuilder<*>): T? {
        return hudElements[hudBuilder.RESOURCE_LOCATION].unsafeCast()
    }

    companion object : RendererBuilder<HUDRenderer> {
        override val RESOURCE_LOCATION = "minosoft:hud_renderer".toResourceLocation()

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): HUDRenderer {
            return HUDRenderer(connection, renderWindow)
        }
    }
}

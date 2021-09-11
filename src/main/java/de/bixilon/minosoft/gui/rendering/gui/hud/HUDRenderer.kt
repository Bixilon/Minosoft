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
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.hud.DebugHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.hud.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.hud.HUDElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class HUDRenderer(
    val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private val shader = renderWindow.renderSystem.createShader("minosoft:hud".toResourceLocation())
    private lateinit var mesh: GUIMesh
    var scaledSize: Vec2i = renderWindow.window.size
    private var matrix: Mat4 = Mat4()
    private var enabled = true

    private val hudElements: MutableMap<ResourceLocation, HUDElement<*>> = synchronizedMapOf()

    private var lastTickTime = 0L

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
    }

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            scaledSize = Vec2i(Vec2(it.size) / Minosoft.config.config.game.hud.scale)
            matrix = glm.ortho(0.0f, scaledSize.x.toFloat(), scaledSize.y.toFloat(), 0.0f)

            for (element in hudElements.toSynchronizedMap().values) {
                element.layout?.onParentChange()
            }
        })
        registerDefaultElements()

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.init()
        }

        renderWindow.inputHandler.registerKeyCallback("minosoft:enable_hud".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.STICKY_INVERTED to mutableSetOf(KeyCodes.KEY_F1),
            ),
        )) {
            Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Toggled hud: $it" }
            enabled = it
        }
    }

    override fun postInit() {
        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.postInit()
        }
    }

    override fun draw() {
        if (!enabled) {
            return
        }
        renderWindow.renderSystem.reset()
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
            }

            lastTickTime = time
        }
        // ToDo: size > maxSize


        for (element in hudElements) {
            if (!element.enabled) {
                continue
            }
            element.layout?.render(element.layoutOffset ?: Vec2i.EMPTY, 0, mesh)
            element.draw(mesh)
        }

        mesh.load()


        shader.use()
        mesh.draw()
    }

    companion object : RendererBuilder<HUDRenderer> {
        override val RESOURCE_LOCATION = "minosoft:hud_renderer".toResourceLocation()

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): HUDRenderer {
            return HUDRenderer(connection, renderWindow)
        }
    }
}

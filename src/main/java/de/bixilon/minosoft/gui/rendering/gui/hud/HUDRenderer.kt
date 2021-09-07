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
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.hud.DebugHUD
import de.bixilon.minosoft.gui.rendering.gui.hud.hud.HUD
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
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
    var scaledSize: Vec2 = renderWindow.window.sizef
    private var matrix: Mat4 = Mat4()

    val hud: MutableList<HUD<*>> = synchronizedListOf(
        DebugHUD(this),
    )

    private var lastTickTime = 0L

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            scaledSize = Vec2(it.size) / Minosoft.config.config.game.hud.scale
            matrix = glm.ortho(0.0f, scaledSize.x, scaledSize.y, 0.0f)
        })

        for (hud in this.hud) {
            hud.init()
        }
    }

    override fun postInit() {
        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)

        for (hud in this.hud) {
            hud.postInit()
        }
    }

    override fun draw() {
        renderWindow.renderSystem.reset()
        if (this::mesh.isInitialized) {
            mesh.unload()
        }

        mesh = GUIMesh(renderWindow, matrix)

        val time = System.currentTimeMillis()
        if (time - lastTickTime > ProtocolDefinition.TICK_TIME) {
            for (hud in this.hud) {
                hud.tick()
            }

            lastTickTime = time
        }
        // ToDo: size > maxSize


        for (hud in this.hud) {
            val z = 0
            val offset = Vec2i.EMPTY // ToDo: Element positioning

            hud.layout?.render(offset, z, mesh)
            hud.draw(offset, z, mesh)
        }

        mesh.load()


        shader.use()
        mesh.draw()
    }

    companion object : RendererBuilder<HUDRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:hud_renderer")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): HUDRenderer {
            return HUDRenderer(connection, renderWindow)
        }
    }
}

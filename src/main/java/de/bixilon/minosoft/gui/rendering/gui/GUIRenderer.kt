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

package de.bixilon.minosoft.gui.rendering.gui

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatchRendering
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasManager
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIManager
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDManager
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.phases.OtherDrawable
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i

class GUIRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, InputHandler, OtherDrawable {
    private val profile = connection.profiles.hud
    override val renderSystem = renderWindow.renderSystem
    var scaledSize: Vec2i = renderWindow.window.size
    val gui = GUIManager(this)
    val hud = HUDManager(this)
    var matrix: Mat4 = Mat4()
        private set
    var matrixChange = true
    override val framebuffer: Framebuffer
        get() = renderWindow.framebufferManager.gui.framebuffer
    override val polygonMode: PolygonModes
        get() = renderWindow.framebufferManager.gui.polygonMode
    val shader = renderWindow.renderSystem.createShader("minosoft:hud".toResourceLocation())
    val atlasManager = AtlasManager(renderWindow)

    val currentCursorPosition: Vec2i
        get() = Vec2i(renderWindow.inputHandler.currentMousePosition).scale()

    override fun init(latch: CountUpAndDownLatch) {
        atlasManager.init()
        gui.init()
        hud.init()
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        atlasManager.postInit()
        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)
        renderWindow.textureManager.staticTextures.animator.use(shader)

        connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> { recalculateMatrices(it.size) })
        profile::scale.profileWatchRendering(this, profile = profile) { recalculateMatrices(scale = it) }

        gui.postInit()
        hud.postInit()
    }

    private fun recalculateMatrices(windowSize: Vec2i = renderWindow.window.size, scale: Float = profile.scale) {
        scaledSize = windowSize.scale(scale)
        matrix = glm.ortho(0.0f, scaledSize.x.toFloat(), scaledSize.y.toFloat(), 0.0f)
        matrixChange = true

        gui.onMatrixChange()
        hud.onMatrixChange()
    }

    fun setup() {
        renderSystem.reset(
            blending = true,
            sourceRGB = BlendingFunctions.SOURCE_ALPHA,
            destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
            sourceAlpha = BlendingFunctions.ONE,
            destinationAlpha = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
        )
        shader.use()
    }

    override fun onMouseMove(position: Vec2i) {
        gui.onMouseMove(position.scale())
    }

    override fun onCharPress(char: Int) {
        gui.onCharPress(char)
    }

    override fun onKeyPress(type: KeyChangeTypes, key: KeyCodes) {
        gui.onKeyPress(type, key)
    }

    override fun drawOther() {
        var z = 0
        z += hud.draw(z) + 1
        z += gui.draw(z) + 1
        if (this.matrixChange) {
            this.matrixChange = false
        }
    }

    fun Vec2i.scale(scale: Float = profile.scale): Vec2i {
        val output = Vec2i(this)
        // ToDo: This is just a dirty workaround and does not fix the problem at all
        while (output.x % scale.toInt() != 0) {
            output.x--
            if (output.x < 0) {
                output.x = 0
            }
        }
        while (output.y % scale.toInt() != 0) {
            output.y--
            if (output.y < 0) {
                output.y = 0
            }
        }
        return output / scale
    }

    fun isKeyDown(vararg keyCodes: KeyCodes): Boolean {
        return renderWindow.inputHandler.isKeyDown(*keyCodes)
    }

    fun isKeyDown(modifier: ModifierKeys): Boolean {
        return renderWindow.inputHandler.isKeyDown(modifier)
    }

    companion object : RendererBuilder<GUIRenderer> {
        override val RESOURCE_LOCATION = "minosoft:gui".toResourceLocation()

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): GUIRenderer {
            return GUIRenderer(connection, renderWindow)
        }
    }
}

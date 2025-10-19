/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.light.RenderLight
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererManager
import de.bixilon.minosoft.gui.rendering.shader.ShaderManager
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalManager
import de.bixilon.minosoft.gui.rendering.stats.AbstractRenderStats
import de.bixilon.minosoft.gui.rendering.stats.ExperimentalRenderStats
import de.bixilon.minosoft.gui.rendering.stats.RenderStats
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystemFactory
import de.bixilon.minosoft.gui.rendering.system.window.WindowFactory
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class RenderContext(
    val session: PlaySession,
    val rendering: Rendering,
) {
    val profile = session.profiles.rendering
    val preferQuads = profile.advanced.preferQuads

    val window = WindowFactory.factory?.create(this) ?: throw IllegalStateException("Expected a window factory, but none is set.")
    val system = RenderSystemFactory.factory?.create(this) ?: throw IllegalStateException("Expected a rendering api factory, but none is set.")
    val camera = Camera(this)

    val input = InputManager(this)
    val screenshotTaker = ScreenshotTaker(this)
    val tints = TintManager(session)
    val textures = system.createTextureManager()

    val queue = Queue()

    val shaders = ShaderManager(this)
    val framebuffer = FramebufferManager(this)
    val renderer = RendererManager(this)
    val models = ModelLoader(this)

    val light = RenderLight(this)

    val skeletal = SkeletalManager(this)

    var renderStats: AbstractRenderStats = RenderStats()
        private set

    var font: FontManager = unsafeNull()


    val thread: Thread = unsafeNull()

    var state by observed(RenderingStates.LOADING)

    init {
        profile.experimental::fps.observe(this, true) { renderStats = if (it) ExperimentalRenderStats() else RenderStats() }
    }
}

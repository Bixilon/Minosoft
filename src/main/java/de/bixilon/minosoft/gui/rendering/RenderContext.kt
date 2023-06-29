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

package de.bixilon.minosoft.gui.rendering

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferManager
import de.bixilon.minosoft.gui.rendering.input.key.manager.InputManager
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererManager
import de.bixilon.minosoft.gui.rendering.shader.ShaderManager
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalManager
import de.bixilon.minosoft.gui.rendering.stats.AbstractRenderStats
import de.bixilon.minosoft.gui.rendering.stats.ExperimentalRenderStats
import de.bixilon.minosoft.gui.rendering.stats.RenderStats
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystemFactory
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindowFactory
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.util.ScreenshotTaker
import de.bixilon.minosoft.gui.rendering.world.light.RenderLight
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class RenderContext(
    val connection: PlayConnection,
    val rendering: Rendering,
) {
    val profile = connection.profiles.rendering
    val preferQuads = profile.advanced.preferQuads

    val window = BaseWindowFactory.create(this)
    val renderSystem = RenderSystemFactory.create(this)
    val camera = Camera(this)

    val inputManager = InputManager(this)
    val screenshotTaker = ScreenshotTaker(this)
    val tintManager = TintManager(connection)
    val textureManager = renderSystem.createTextureManager()

    val queue = Queue()

    val shaderManager = ShaderManager(this)
    val framebufferManager = FramebufferManager(this)
    val renderer = RendererManager(this)
    val modelLoader = ModelLoader(this)

    val light = RenderLight(this)

    val skeletalManager = SkeletalManager(this)

    lateinit var renderStats: AbstractRenderStats
        private set

    var font: FontManager = unsafeNull()


    val thread: Thread = unsafeNull()

    var state by observed(RenderingStates.LOADING)

    init {
        profile.experimental::fps.observe(this, true) { renderStats = if (it) ExperimentalRenderStats() else RenderStats() }
    }
}

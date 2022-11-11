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

package de.bixilon.minosoft.gui.rendering.sky

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.dimension.effects.OverworldEffects
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.sky.box.SkyboxRenderer
import de.bixilon.minosoft.gui.rendering.sky.planet.MoonRenderer
import de.bixilon.minosoft.gui.rendering.sky.planet.SunRenderer
import de.bixilon.minosoft.gui.rendering.sky.planet.scatter.SunScatterRenderer
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.phases.PreDrawable
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class SkyRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, PreDrawable, AsyncRenderer {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    override val framebuffer: Framebuffer? = null
    override val polygonMode: PolygonModes = PolygonModes.DEFAULT
    private val renderer: MutableList<SkyChildRenderer> = mutableListOf()
    var effects by watched(connection.world.dimension?.effects ?: OverworldEffects)
    var matrix by watched(Mat4())
    val profile = connection.profiles.rendering.sky
    var time = connection.world.time
        private set
    private var updateTime: Boolean = true

    val box = SkyboxRenderer(this)
    val sun = SunRenderer(this)
    val sunScatter = SunScatterRenderer(this, sun)
    val moon = MoonRenderer(this)

    override fun init(latch: CountUpAndDownLatch) {
        box.register()
        sunScatter.register()
        sun.register()
        moon.register()

        for (renderer in renderer) {
            renderer.init()
        }
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        for (renderer in renderer) {
            renderer.postInit()
        }
        connection.world::time.observe(this) { updateTime = true }
        connection.events.listen<CameraMatrixChangeEvent> {
            matrix = it.projectionMatrix * it.viewMatrix.toMat3().toMat4()
        }
        connection.world::dimension.observe(this) { effects = it?.effects ?: OverworldEffects }
    }

    override fun prepareDrawAsync() {
        if (updateTime) {
            this.time = connection.world.time
            for (renderer in renderer) {
                renderer.onTimeUpdate(time)
            }
            updateTime = false
        }
        for (renderer in renderer) {
            renderer.updateAsync()
        }
    }

    override fun prePrepareDraw() {
        for (renderer in renderer) {
            renderer.update()
        }
    }

    override fun drawPre() {
        renderWindow.renderSystem.reset(depth = DepthFunctions.LESS_OR_EQUAL, depthMask = false)
        for (renderer in renderer) {
            renderer.draw()
        }
    }

    private fun <T : SkyChildRenderer> T.register(): T {
        renderer += this
        return this
    }

    companion object : RendererBuilder<SkyRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:sky")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): SkyRenderer {
            return SkyRenderer(connection, renderWindow)
        }
    }
}

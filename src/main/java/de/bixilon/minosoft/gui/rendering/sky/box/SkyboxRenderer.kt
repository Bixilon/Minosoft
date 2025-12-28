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

package de.bixilon.minosoft.gui.rendering.sky.box

import de.bixilon.kutil.hash.HashUtil.murmur64
import de.bixilon.kutil.observer.set.SetObserver.Companion.observeSet
import de.bixilon.minosoft.data.entities.entities.LightningBolt
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkDataUpdate
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.gui.rendering.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.sky.SkyChildRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.box.color.SkyboxColorType
import de.bixilon.minosoft.gui.rendering.sky.box.normal.SkyboxNormalType
import de.bixilon.minosoft.gui.rendering.sky.box.texture.SkyboxTextureType
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.Backports.nextFloatPort
import java.util.*

class SkyboxRenderer(
    private val sky: SkyRenderer,
) : SkyChildRenderer {
    val color = SkyboxColor(sky)

    private val normalType = SkyboxNormalType(sky)
    private val colorType = SkyboxColorType(sky)
    private val textureType = SkyboxTextureType(sky)

    private var time: WorldTime = sky.context.session.world.time


    private var day = -1L
    var intensity = 1.0f
        private set

    init {
        // ToDo: Sync with lightmap, lightnings, etc
        sky.context.session.world.entities::entities.observeSet(this) {
            val lightnings = it.adds.filterIsInstance<LightningBolt>()
            if (lightnings.isEmpty()) return@observeSet
            color.onStrike(lightnings.maxOf { it.duration.duration })
        }

        sky.context.session.events.listen<WorldUpdateEvent> {
            if (it.update !is ChunkDataUpdate) return@listen
            if (!it.update.chunk.neighbours.complete) return@listen
            color.updateBase()
        }
        sky.context.session.events.listen<CameraPositionChangeEvent> {
            color.updateBase()
        }
    }

    override fun onTimeUpdate(time: WorldTime) {
        this.time = time
        if (day != time.day) {
            this.day = time.day
            this.intensity = Random(time.day.murmur64()).nextFloatPort(0.3f, 1.0f)
        }
    }


    override fun init() {
        normalType.init()
        textureType.init()
        colorType.init()
    }

    override fun postInit() {
        normalType.postInit()
        textureType.postInit()
        colorType.postInit()
    }

    override fun updateAsync() {
        color.update()
    }

    override fun draw() {
        val effects = sky.effects

        val type = when {
            effects.fixedTexture != null -> textureType
            effects.sun -> normalType
            else -> colorType
        }

        type.draw()
    }
}

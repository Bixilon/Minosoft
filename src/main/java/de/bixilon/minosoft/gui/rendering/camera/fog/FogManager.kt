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

package de.bixilon.minosoft.gui.rendering.camera.fog

import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.registries.dimension.effects.FogEffects
import de.bixilon.minosoft.data.registries.effects.vision.VisionEffect
import de.bixilon.minosoft.data.text.formatting.color.ColorInterpolation.interpolateSine
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.AbstractShader
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.types.FogShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class FogManager(
    private val context: RenderContext,
) {
    private val player = context.session.player

    private var interpolation = FogInterpolationStart()
    val state = FogState()
    private var options: FogOptions? = null


    private var shaderRevision = -1

    fun draw() {
        update()
        if (interpolation.change != TimeUtil.NULL) {
            interpolate()
        }
        updateShaders()
    }

    private fun getOptions(effects: FogEffects): FogOptions? {
        val fluid = player.physics.submersion.eye

        return when {
            fluid is FoggedFluid -> fluid.getFogOptions(context.session.world, player.physics.positionInfo)
            player.effects[VisionEffect.Blindness] != null -> VisionEffect.Blindness.FOG_OPTIONS
            // TODO: void fog (if under minY)
            // TODO: powder snow
            else -> {
                val end = (context.session.world.view.viewDistance - 1.0f) * ChunkSize.SECTION_WIDTH_X
                val distance = end / 10.0f

                FogOptions(effects.start * (end - distance), end)
            }
        }
    }

    private fun update() {
        val effects = context.session.world.dimension.effects.fog
        val enabled = effects != null && context.session.profiles.rendering.fog.enabled
        if (state.enabled != enabled) {
            state.revision++
        }
        state.enabled = enabled
        if (!enabled) return

        val options = getOptions(effects!!)
        if (this.options == options) {
            return
        }

        this.options = options
        state.revision++
        save()
        context.camera.matrix.invalidate()
    }

    private fun save() {
        val time = now()
        interpolate(time)
        interpolation.change = time
        interpolation.start = state.start
        interpolation.end = state.end
        interpolation.color = state.color
    }

    private fun interpolate(time: ValueTimeMark = now()) {
        val delta = time - interpolation.change
        val progress = (delta / INTERPOLATE_DURATION).toFloat()
        state.start = interpolateLinear(progress, interpolation.start, options?.start ?: 0.0f)
        state.end = interpolateLinear(progress, interpolation.end, options?.end ?: 0.0f)

        val sourceColor = interpolation.color ?: options?.color?.rgba() ?: Colors.TRANSPARENT
        val targetColor = options?.color?.rgba() ?: Colors.TRANSPARENT
        var color: RGBAColor? = interpolateSine(progress, sourceColor, targetColor)
        if (color == Colors.TRANSPARENT) {
            color = null
        }
        state.color = color
        if (progress >= 1.0f) {
            interpolation.change = TimeUtil.NULL // this avoid further interpolations with the same data
            interpolation.color = options?.color?.rgba()
        }

        state.revision++
        context.camera.matrix.invalidate()
    }


    private fun updateShaders() {
        val revision = state.revision

        if (revision == this.shaderRevision) {
            return
        }

        val start = state.start * state.start
        val end = state.end * state.end
        val distance = end - start
        val color = state.color
        val flags = state.flags()

        for (shader in context.system.shaders) {
            if (shader !is FogShader || shader.fog != this) {
                continue
            }
            shader.update(start, end, distance, color, flags)
        }
        this.shaderRevision = revision
    }

    fun use(shader: AbstractShader) {
        val start = state.start * state.start
        val end = state.end * state.end
        val distance = end - start
        val color = state.color
        val flags = state.flags()

        shader.update(start, end, distance, color, flags)
    }

    private fun AbstractShader.update(start: Float, end: Float, distance: Float, color: RGBAColor?, flags: Int) {
        use()

        val native = native

        native["uFogStart"] = start
        native["uFogEnd"] = end
        native["uFogDistance"] = distance

        color?.let { native["uFogColor"] = it }
        native.setUInt("uFogFlags", flags)
    }


    companion object {
        private val INTERPOLATE_DURATION = 300.milliseconds
    }
}

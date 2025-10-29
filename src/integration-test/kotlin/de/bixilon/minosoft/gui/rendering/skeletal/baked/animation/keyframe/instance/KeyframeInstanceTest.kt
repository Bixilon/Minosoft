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

package de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.AnimationResult
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types.KeyframeData
import it.unimi.dsi.fastutil.floats.FloatArrayList
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import kotlin.time.Duration.Companion.seconds

@Test(groups = ["skeletal", "rendering"])
class KeyframeInstanceTest {

    fun `too less frames`() {
        val instance = Instance(AnimationLoops.ONCE, mapOf(0.0f to 0.0f))
        instance.transform(1.0f)
        assertEquals(instance.entries, FloatArrayList.of())
    }

    fun `once with 2 keyframes`() {
        val instance = Instance(AnimationLoops.ONCE, mapOf(
            0.0f to 0.0f,
            2.0f to 5.0f,
        ))

        val ended = arrayOf(
            instance.transform(0.0f),
            instance.transform(0.2f),
            instance.transform(0.5f),
            instance.transform(1.0f),
            instance.transform(2.0f),
        )
        ended.assertEnded()

        assertEquals(instance.entries, FloatArrayList.of(
            0.0f,
            0.5f,
            1.25f,
            2.5f,
            // 5.0f,
        ))
    }

    fun `once with 3 keyframes`() {
        val instance = Instance(AnimationLoops.ONCE, mapOf(
            0.0f to 0.0f,
            2.0f to 5.0f,
            3.0f to 10.0f,
        ))

        val ended = arrayOf(
            instance.transform(0.0f),
            instance.transform(1.0f),
            instance.transform(2.0f),
            instance.transform(2.5f),
            instance.transform(3.0f),
        )
        ended.assertEnded()

        assertEquals(instance.entries, FloatArrayList.of(
            0.0f,
            2.5f,
            5.0f,
            7.5f,
            // 10.0f
        ))
    }

    fun `hold with 2 keyframes`() {
        val instance = Instance(AnimationLoops.HOLD, mapOf(
            0.0f to 0.0f,
            2.0f to 5.0f,
        ))

        val ended = arrayOf(
            instance.transform(0.0f),
            instance.transform(1.0f),
            instance.transform(2.0f),
            instance.transform(3.0f),
            instance.transform(300.0f),
        )
        ended.assertNotOver()

        assertEquals(instance.entries, FloatArrayList.of(
            0.0f,
            2.5f,
            5.0f,
            5.0f,
            5.0f,
        ))
    }

    fun `hold with 3 keyframes`() {
        val instance = Instance(AnimationLoops.HOLD, mapOf(
            0.0f to 0.0f,
            2.0f to 5.0f,
            3.0f to 7.0f,
        ))

        val ended = arrayOf(
            instance.transform(0.0f),
            instance.transform(1.0f),
            instance.transform(2.0f),
            instance.transform(2.5f),
            instance.transform(3.0f),
            instance.transform(4.0f),
            instance.transform(300.0f),
        )
        ended.assertNotOver()

        assertEquals(instance.entries, FloatArrayList.of(
            0.0f,
            2.5f,
            5.0f,
            6.0f,
            7.0f,
            7.0f,
            7.0f,
        ))
    }

    fun `loop with 2 keyframes`() {
        val instance = Instance(AnimationLoops.LOOP, mapOf(
            0.0f to 0.0f,
            2.0f to 5.0f,
        ))

        val ended = arrayOf(
            instance.transform(0.0f),
            instance.transform(1.0f),
            instance.transform(2.0f),

            instance.transform(3.0f),
            instance.transform(4.0f),

            instance.transform(5.0f),
            instance.transform(5.5f),
        )
        ended.assertNotOver()

        assertEquals(instance.entries, FloatArrayList.of(
            0.0f,
            2.5f,
            0.0f,

            2.5f,
            0.0f,

            2.5f,
            3.75f,
        ))
    }

    fun `loop with 3 keyframes`() {
        val instance = Instance(AnimationLoops.LOOP, mapOf(
            0.0f to 0.0f,
            2.0f to 5.0f,
            3.0f to 7.0f,
            4.0f to 0.0f,
        ))

        val ended = arrayOf(
            instance.transform(0.0f),
            instance.transform(1.0f),
            instance.transform(2.0f),
            instance.transform(3.0f),
            instance.transform(3.5f),
            instance.transform(4.0f),

            instance.transform(5.0f),
            instance.transform(5.5f),
            instance.transform(6.0f),

            instance.transform(9.0f),
        )
        ended.assertNotOver()

        assertEquals(instance.entries, FloatArrayList.of(
            0.0f,
            2.5f,
            5.0f,
            7.0f,
            3.5f,
            0.0f,

            2.5f,
            3.75f,
            5.0f,
            2.5f,
        ))
    }

    fun `unstarted animation`() {
        val instance = Instance(AnimationLoops.ONCE, mapOf(
            10.0f to 0.0f,
            12.0f to 5.0f,
        ))

        val ended = arrayOf(
            instance.transform(0.0f),
            instance.transform(1.0f),
            instance.transform(9.9f),
            instance.transform(10.0f),
            instance.transform(11.0f),
            instance.transform(12.0f),
        )
        ended.assertEnded()

        assertEquals(instance.entries, FloatArrayList.of(
            0.0f,
            2.5f,
        ))
    }

    private fun Array<AnimationResult>.assertNotOver() {
        for (entry in this) {
            assertEquals(entry, AnimationResult.CONTINUE)
        }
    }

    private fun Array<AnimationResult>.assertEnded() {
        if (this.isEmpty()) throw IllegalArgumentException("Empty!")
        for ((index, entry) in this.withIndex()) {
            if (index + 1 == this.size) break
            assertEquals(entry, AnimationResult.CONTINUE, "Expected animation to not have ended yet at $index")
        }
        assertEquals(this.last(), AnimationResult.ENDED, "Expected animation to have ended!")
    }


    private class Instance(loop: AnimationLoops, data: Map<Float, Float>) : KeyframeInstance<Float>(data.entries.map { KeyframeData(it.key.toDouble().seconds, it.value) }.sorted(), loop) {
        val entries = FloatArrayList()

        override fun apply(value: Float, transform: TransformInstance) {
            entries += value
        }

        override fun interpolate(delta: Float, previous: Float, next: Float): Float {
            return interpolateLinear(delta, previous, next)
        }

        fun transform(seconds: Float): AnimationResult {
            return transform(seconds.toDouble().seconds, TransformInstance(0, Vec3f.EMPTY, emptyMap()))
        }
    }
}

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

package de.bixilon.minosoft.gui.rendering.models.block.state.render

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WeightedBlockRender.WeightedEntry
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertSame
import org.testng.annotations.Test
import java.util.*

@Test(groups = ["rendering"])
class WeightedBlockRenderTest {
    private val get = WeightedBlockRender::class.java.getDeclaredMethod("getModel", Random::class.java, BlockPosition::class.java).apply { setUnsafeAccessible() }
    private val modelA = BakedModel::class.java.allocate().apply { this::properties.forceSet(arrayOfNulls(Directions.SIZE)) }
    private val modelB = BakedModel::class.java.allocate().apply { this::properties.forceSet(arrayOfNulls(Directions.SIZE)) }

    private val position = BlockPosition(1, 2, 3)

    private fun WeightedBlockRender.getModel(random: Random?, position: BlockPosition): BakedModel {
        return get.invoke(this, random, position) as BakedModel
    }

    private fun create(models: Array<WeightedEntry>): WeightedBlockRender {
        return WeightedBlockRender(models, models.sumOf { it.weight })
    }

    fun `single model no random`() {
        val render = create(arrayOf(WeightedEntry(3, modelA)))

        assertSame(render.getModel(null, position), modelA)
    }

    fun `single model random`() {
        val render = create(arrayOf(WeightedEntry(3, modelA)))

        assertSame(render.getModel(Random(), position), modelA)
    }

    fun `two models no random`() {
        val render = create(arrayOf(WeightedEntry(3, modelA), WeightedEntry(3, modelB)))

        assertSame(render.getModel(null, position), modelA)
    }

    fun `two models random`() {
        val render = create(arrayOf(WeightedEntry(3, modelA), WeightedEntry(3, modelB)))

        assertSame(render.getModel(Random(0L), position), modelB)
    }

    fun `two models high weight random`() {
        val render = create(arrayOf(WeightedEntry(100, modelA), WeightedEntry(100, modelB)))

        assertSame(render.getModel(Random(0L), position), modelB)
    }

    fun `two models random 2`() {
        val render = create(arrayOf(WeightedEntry(3, modelA), WeightedEntry(3, modelB)))

        assertSame(render.getModel(Random(0L), BlockPosition(3, 1, 1)), modelA)
    }

    fun `two models high weight random 2`() {
        val render = create(arrayOf(WeightedEntry(100, modelA), WeightedEntry(100, modelB)))

        assertSame(render.getModel(Random(0L), BlockPosition(1, 1, 1)), modelA)
    }

    /*    fun `benchmark models random`() {
            val render = create(arrayOf(WeightedEntry(1, modelA), WeightedEntry(1, modelB), WeightedEntry(1, modelA), WeightedEntry(1, modelB)))
    
            val random = Random(0L)
            val time = measureTime {
                for (i in 0 until 499999999) {
                    render.getModel(random, position)
                }
            }
            println("Took: ${time.inWholeNanoseconds.formatNanos()}")
        }
     */
}

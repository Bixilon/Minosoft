/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.queue.loading.loader

import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue.Companion.BATCH_SIZE
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererUtil.maxBusyTime

class SyncLoader(
    val loader: MeshLoadingQueue,
) : AbstractMeshLoader {
    private var queue = false


    override fun work() {
        if (!queue) return
        val start = now()
        val maxTime = loader.renderer.maxBusyTime

        var index = 0
        while (true) {
            if (++index % BATCH_SIZE == 0 && now() - start >= maxTime) break
            val mesh = loader.take()
            if (mesh == null) {
                this.queue = false
                break
            }

            loader.renderer.context.profiler("load$index") { mesh.load() }

            loader.renderer.loaded += mesh
        }
    }

    override fun queue() {
        this.queue = true
    }
}

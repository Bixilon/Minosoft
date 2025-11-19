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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.chunk.mesh.cache.ChunkMeshCache
import de.bixilon.minosoft.gui.rendering.chunk.mesh.details.ChunkMeshDetails
import de.bixilon.minosoft.gui.rendering.chunk.mesher.fluid.FluidSectionMesher

class ChunkMesher(
    private val renderer: ChunkRenderer,
) {
    private val profile = renderer.context.session.profiles.block.lod
    private val solid = SolidSectionMesher(renderer.context)
    private val fluid = FluidSectionMesher(renderer.context)

    var details = IntInlineEnumSet<ChunkMeshDetails>()
        private set

    init {
        profile::enabled.observe(this) { updateDetails() }
        profile::minorVisualImpact.observe(this) { updateDetails() }
        profile::aggressiveCulling.observe(this) { updateDetails() }
        profile::darkCaveCulling.observe(this) { updateDetails() }

        updateDetails()
    }

    private fun updateDetails() {
        var details = IntInlineEnumSet<ChunkMeshDetails>()

        if (!profile.enabled) details = ChunkMeshDetails.ALL // TODO: kutil 1.30.1 +=

        if (!profile.minorVisualImpact) details += ChunkMeshDetails.MINOR_VISUAL_IMPACT
        if (!profile.aggressiveCulling) details += ChunkMeshDetails.AGGRESSIVE_CULLING
        if (!profile.darkCaveCulling) details += ChunkMeshDetails.DARK_CAVE_SURFACE


        if (details == this.details) return

        renderer.invalidate(renderer.world)
    }

    private fun getDetails(previous: IntInlineEnumSet<ChunkMeshDetails>?, position: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {
        if (previous == null) return ChunkMeshDetails.of(position, renderer.visibility.sectionPosition)

        return ChunkMeshDetails.update(previous, position, renderer.visibility.sectionPosition)
    }

    fun mesh(previous: ChunkMeshes?, cache: ChunkMeshCache, section: ChunkSection): ChunkMeshes? {
        if (section.blocks.isEmpty) return null

        val neighbours = section.chunk.neighbours
        val sectionNeighbours = section.neighbours
        if (!neighbours.complete) return null // TODO: Requeue the chunk? (But on a neighbour update the chunk gets queued again?)

        cache.unmark()

        val position = SectionPosition.of(section)

        val details = if (this.details.size > 0) this.details else getDetails(previous?.details, position) // TODO: kutil 1.30.1 +


        // TODO: put sizes of previous mesh (cache estimate)
        val mesh = ChunkMeshesBuilder(renderer.context, section, details)
        try {
            solid.mesh(section, cache, neighbours, sectionNeighbours, mesh)

            if (section.blocks.fluidCount > 0) {
                fluid.mesh(section, mesh)
            }
            cache.cleanup()
        } catch (error: Throwable) {
            mesh.drop()
            throw error
        }

        return mesh.build(position)
    }
}

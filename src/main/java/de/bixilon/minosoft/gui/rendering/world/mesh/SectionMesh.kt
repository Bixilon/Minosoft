package de.bixilon.minosoft.gui.rendering.world.mesh

import de.bixilon.minosoft.gui.rendering.world.WorldQueueItem

class SectionMesh(
    val solidMesh: WorldMesh?,
    val fluidMesh: WorldMesh?,
) {

    constructor(item: WorldQueueItem) : this(item.solidMesh, item.fluidMesh)

    fun unload() {
        solidMesh?.unload()
        fluidMesh?.unload()
    }
}

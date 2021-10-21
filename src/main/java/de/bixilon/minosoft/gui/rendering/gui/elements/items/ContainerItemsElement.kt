package de.bixilon.minosoft.gui.rendering.gui.elements.items

import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.Vec2iBinding
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import glm_.vec2.Vec2i

class ContainerItemsElement(
    hudRenderer: HUDRenderer,
    val container: Container,
    val slots: Map<Int, Vec2iBinding>, // ToDo: Use an array?
) : Element(hudRenderer) {
    override var cacheEnabled: Boolean = false


    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {

        for ((slot, item) in container.slots) {
            slots[slot]?.let {
                ItemElement(hudRenderer, it.size, item).render(offset + it.start, z, consumer)
            }
        }

        return 2
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }
}

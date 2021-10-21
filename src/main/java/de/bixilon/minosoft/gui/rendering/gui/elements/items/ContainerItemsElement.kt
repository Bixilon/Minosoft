package de.bixilon.minosoft.gui.rendering.gui.elements.items

import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.Vec2iBinding
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.vec2.Vec2i

class ContainerItemsElement(
    hudRenderer: HUDRenderer,
    val container: Container,
    val slots: Map<Int, Vec2iBinding>, // ToDo: Use an array?
) : Element(hudRenderer) {
    private val itemElements: MutableMap<Int, ItemElementData> = synchronizedMapOf()
    override var cacheEnabled: Boolean = false

    init {
        silentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        for ((_, data) in itemElements.toSynchronizedMap()) {
            data.element.render(offset + data.offset, z, consumer)
        }

        return 2
    }

    override fun silentApply(): Boolean {
        var changes = false
        for ((slot, binding) in slots) {
            val item = container.slots[slot]
            val data = itemElements[slot]

            if (data == null) {
                item ?: continue
                val element = ItemElement(
                    hudRenderer = hudRenderer,
                    size = binding.size,
                    item = item,
                )
                itemElements[slot] = ItemElementData(
                    element = element,
                    offset = binding.start,
                )
                // element.parent = this
                changes = true
            } else {
                if (data.element.item == item) {
                    if (data.element.poll()) {
                        changes = true
                    }
                } else {
                    data.element.item = item
                    changes = true
                }
            }
        }

        if (!changes) {
            return false
        }

        cacheUpToDate = false
        return true
    }

    override fun forceSilentApply() {
        silentApply()
    }


    private data class ItemElementData(
        val element: ItemElement,
        val offset: Vec2i,
    )
}

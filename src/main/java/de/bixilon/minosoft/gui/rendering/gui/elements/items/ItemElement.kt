package de.bixilon.minosoft.gui.rendering.gui.elements.items

import de.bixilon.kutil.general.BooleanUtil.decide
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.items.block.BlockItem
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2i

class ItemElement(
    hudRenderer: HUDRenderer,
    size: Vec2i,
    item: ItemStack?,
) : Element(hudRenderer), Pollable {
    private var count = -1
    private var countText = TextElement(hudRenderer, "", background = false, noBorder = true)

    var item: ItemStack? = item
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
            cacheUpToDate = false
        }

    init {
        _size = size
        forceApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        val item = item ?: return 0
        val size = size
        val countSize = countText.size
        countText.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, countSize.x), VerticalAlignments.BOTTOM.getOffset(size.y, countSize.y)), z + 1, consumer, options)

        var color = ChatColors.WHITE
        if (item.item is BlockItem) {
            item.item.block?.defaultState?.material?.color?.let { color = it }
        }

        val image = ColorElement(hudRenderer, _size, color)

        image.render(offset, z + 1, consumer, options)

        // ToDo: Render model
        return TextElement.LAYERS + 1
    }

    override fun poll(): Boolean {
        val item = item ?: return false
        val count = item.count
        if (this.count != count) {
            this.count = count
            return true
        }

        return false
    }

    override fun forceSilentApply() {
        countText.text = when {
            count < 0 -> TextComponent((count < -99).decide({ "-∞" }, { count }), color = ChatColors.RED) // No clue why I do this...
            count == 0 -> TextComponent("0", color = ChatColors.YELLOW)
            count == 1 -> TextComponent("")
            count > ProtocolDefinition.ITEM_STACK_MAX_SIZE -> TextComponent((count > 99).decide({ "∞" }, { count }), color = ChatColors.RED)
            else -> TextComponent(count)
        }

        cacheUpToDate = false
    }

    override fun toString(): String {
        return item.toString()
    }
}

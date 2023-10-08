/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.enchanting

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.container.types.EnchantingContainer
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.ContainerGUIFactory
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.LabeledContainerScreen
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.isSmaller
import kotlin.reflect.KClass

class EnchantingContainerScreen(guiRenderer: GUIRenderer, container: EnchantingContainer, val atlas: Atlas? = guiRenderer.atlas[ATLAS]) : LabeledContainerScreen<EnchantingContainer>(guiRenderer, container, atlas["container"]) {
    private val cards: Array<EnchantmentButtonElement> = Array(EnchantingContainer.ENCHANTING_OPTIONS) { EnchantmentButtonElement(guiRenderer, this, atlas["level_${it + 1}"], atlas["level_${it + 1}_disabled"], it) }
    private val cardAreas = Array(EnchantingContainer.ENCHANTING_OPTIONS) { atlasElement?.areas?.get("card_$it") }


    init {
        container::propertiesRevision.observe(this) { forceApply() }
        container::revision.observe(this) { forceApply() }
    }

    override fun forceRenderContainerScreen(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRenderContainerScreen(offset, consumer, options)

        for ((index, card) in cards.withIndex()) {
            val area = cardAreas.getOrNull(index) ?: continue
            card.render(offset + area.start, consumer, options)
        }
    }

    override fun getContainerAt(position: Vec2): Pair<Element, Vec2>? {
        for ((index, area) in cardAreas.withIndex()) {
            if (area == null) {
                continue
            }
            if (position isSmaller area.start) {
                continue
            }
            if (position isGreater area.end) {
                continue
            }
            val innerPosition = position - area.start
            return Pair(cards[index], innerPosition)
        }
        return super.getContainerAt(position)
    }

    override fun forceSilentApply() {
        super.forceSilentApply()

        for (index in 0 until EnchantingContainer.ENCHANTING_OPTIONS) {
            val card = cards[index]
            card.update(!container.canEnchant(index), container.costs[index], container.enchantments[index], container.enchantmentLevels[index])
        }
    }

    companion object : ContainerGUIFactory<EnchantingContainerScreen, EnchantingContainer> {
        private val ATLAS = minecraft("container/enchanting")
        override val clazz: KClass<EnchantingContainer> = EnchantingContainer::class

        override fun register(gui: GUIRenderer) {
            gui.atlas.load(ATLAS)
        }

        override fun build(gui: GUIRenderer, container: EnchantingContainer): EnchantingContainerScreen {
            return EnchantingContainerScreen(gui, container)
        }
    }
}

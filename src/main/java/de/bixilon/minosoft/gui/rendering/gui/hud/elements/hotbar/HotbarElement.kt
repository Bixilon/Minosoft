/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.container.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.FadingTextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.left
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4iUtil.right
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageReceiveEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import java.lang.Integer.max

class HotbarElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable {
    val core = HotbarCoreElement(guiRenderer)

    val offhand = HotbarOffhandElement(guiRenderer)
    private var renderOffhand = false

    val hoverText = FadingTextElement(guiRenderer, text = "", fadeInTime = 300, stayTime = 3000, fadeOutTime = 500, background = false, noBorder = true)
    private var hoverTextShown = false

    private val itemText = FadingTextElement(guiRenderer, text = "", fadeInTime = 300, stayTime = 1500, fadeOutTime = 500, background = false, noBorder = true)
    private var lastItemStackNameShown: ItemStack? = null
    private var lastItemSlot = -1
    private var itemTextShown = true


    override val layoutOffset: Vec2i
        get() = size.let { Vec2i((guiRenderer.scaledSize.x - it.x) / 2, guiRenderer.scaledSize.y - it.y) }

    private var renderElements = setOf(
        itemText,
        hoverText,
    )

    init {
        core.parent = this
        itemText.parent = this
        hoverText.parent = this
        offhand.parent = this
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size

        if (hoverTextShown) {
            hoverText.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, hoverText.size.x), 0), consumer, options)
            offset.y += hoverText.size.y + HOVER_TEXT_OFFSET
        }
        if (itemTextShown) {
            itemText.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, itemText.size.x), 0), consumer, options)
            offset.y += itemText.size.y + ITEM_NAME_OFFSET
        }

        val coreOffset = offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, core.size.x), 0)

        if (renderOffhand) {
            val offhandOffset = Vec2i.EMPTY
            if (offhand.offArm == Arms.LEFT) {
                offhandOffset.x = -offhand.size.x - offhand.margin.right
            } else {
                offhandOffset.x = core.size.x + offhand.margin.left
            }
            offhandOffset.y = core.size.y - offhand.size.y
            offhand.render(coreOffset + offhandOffset, consumer, options)
        }

        core.render(coreOffset, consumer, options)
    }

    override fun forceSilentApply() {
        for (element in renderElements) {
            element.silentApply()
        }

        val size = Vec2i(core.size)

        renderOffhand = guiRenderer.renderWindow.connection.player.inventory[EquipmentSlots.OFF_HAND] != null

        if (renderOffhand) {
            size.x += offhand.size.x
            size.y = max(size.y, offhand.size.y)
        }


        if (renderWindow.connection.player.gamemode != Gamemodes.SPECTATOR) {
            itemTextShown = !itemText.hidden
            if (itemTextShown) {
                size.y += itemText.size.y + ITEM_NAME_OFFSET
                size.x = max(size.x, itemText.size.x)
            }
        } else {
            itemTextShown = false
        }

        hoverTextShown = !hoverText.hidden
        if (hoverTextShown) {
            size.y += hoverText.size.y + HOVER_TEXT_OFFSET
            size.x = max(size.x, hoverText.size.x)
        }

        _size = size
        cacheUpToDate = false
    }

    override fun silentApply(): Boolean {
        val itemSlot = guiRenderer.renderWindow.connection.player.selectedHotbarSlot
        val currentItem = guiRenderer.renderWindow.connection.player.inventory.getHotbarSlot(itemSlot)
        if (currentItem != lastItemStackNameShown || itemSlot != lastItemSlot) {
            lastItemStackNameShown = currentItem
            lastItemSlot = itemSlot
            currentItem?.displayName?.let { itemText._chatComponent = it;itemText.forceSilentApply() }
            if (currentItem == null) {
                itemText.hide()
            } else {
                itemText.show()
            }
        }

        forceSilentApply() // ToDo: Check stuff
        return true
    }

    override fun onChildChange(child: Element) {
        silentApply() // ToDo: Check
        parent?.onChildChange(this)
    }

    override fun tick() {
        silentApply()
        hoverText.tick()
        itemText.tick()
        core.tick()
        offhand.tick()
    }


    override fun postInit() {
        prefMaxSize = Vec2i(-1, -1)

        val connection = renderWindow.connection
        val player = connection.player

        player::experienceCondition.observeRendering(this) { core.experience.apply() }

        player.additional::gamemode.observeRendering(this) { forceApply() }

        player::selectedHotbarSlot.observeRendering(this) { core.base.apply() }

        connection.events.listen<ChatMessageReceiveEvent> {
            if (it.message.type.position != ChatTextPositions.HOTBAR) {
                return@listen
            }
            hoverText.text = it.message.text
            hoverText.show()
        }
    }


    companion object : HUDBuilder<LayoutedGUIElement<HotbarElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:hotbar".toResourceLocation()
        private const val HOVER_TEXT_OFFSET = 15
        private const val ITEM_NAME_OFFSET = 5

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<HotbarElement> {
            return LayoutedGUIElement(HotbarElement(guiRenderer))
        }
    }
}

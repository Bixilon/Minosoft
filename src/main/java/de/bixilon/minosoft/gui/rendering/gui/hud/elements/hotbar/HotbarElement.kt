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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.font.renderer.element.CharSpacing
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.fade.FadingTextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.fade.FadingTimes
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.health.HeartAtlas
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4fUtil.left
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4fUtil.right
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import kotlin.time.Duration.Companion.milliseconds

class HotbarElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable {
    val core = HotbarCoreElement(guiRenderer)

    val offhand = HotbarOffhandElement(guiRenderer)
    private var renderOffhand = false

    val hoverText = FadingTextElement(guiRenderer, text = "", FadingTimes(300.milliseconds, 3000.milliseconds, 500.milliseconds), background = null, properties = TextRenderProperties(charSpacing = CharSpacing.VERTICAL))
    private var hoverTextSize: Vec2f? = null

    private val itemText = FadingTextElement(guiRenderer, text = "", FadingTimes(300.milliseconds, 1500.milliseconds, 500.milliseconds), background = null, properties = TextRenderProperties(charSpacing = CharSpacing.VERTICAL))
    private var lastItemStackNameShown: ItemStack? = null
    private var lastItemSlot = -1
    private var itemTextSize: Vec2f? = null


    override val layoutOffset: Vec2f
        get() = size.let { Vec2f((guiRenderer.scaledSize.x - it.x) / 2, guiRenderer.scaledSize.y - it.y) }

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

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        val offset = MVec2f(offset)

        val hoverTextSize = hoverTextSize
        if (hoverTextSize != null) {
            hoverText.render(offset.unsafe + Vec2f(HorizontalAlignments.CENTER.getOffset(size.x, hoverTextSize.x), 0.0f), consumer, options)
            offset.y += hoverTextSize.y + HOVER_TEXT_OFFSET
        }
        val itemTextSize = itemTextSize
        if (itemTextSize != null) {
            itemText.render(offset.unsafe + Vec2f(HorizontalAlignments.CENTER.getOffset(size.x, itemTextSize.x), 0.0f), consumer, options)
            offset.y += itemTextSize.y + ITEM_NAME_OFFSET
        }

        val coreOffset = offset + Vec2f(HorizontalAlignments.CENTER.getOffset(size.x, core.size.x), 0.0f)

        if (renderOffhand) {
            val offhandOffset = MVec2f.EMPTY
            if (offhand.offArm == Arms.LEFT) {
                offhandOffset.x = -offhand.size.x - offhand.margin.right
            } else {
                offhandOffset.x = core.size.x + offhand.margin.left
            }
            offhandOffset.y = core.size.y - offhand.size.y
            offhand.render(coreOffset.unsafe + offhandOffset, consumer, options)
        }

        core.render(coreOffset.unsafe, consumer, options)
    }

    override fun forceSilentApply() {
        for (element in renderElements) {
            element.silentApply()
        }

        val size = MVec2f(core.size)

        renderOffhand = guiRenderer.context.session.player.items.inventory[EquipmentSlots.OFF_HAND] != null

        if (renderOffhand) {
            size.x += offhand.size.x
            size.y = maxOf(size.y, offhand.size.y)
        }


        val itemTextSize = itemText.size
        if (context.session.player.gamemode != Gamemodes.SPECTATOR && itemTextSize.length2() > 0.0f) {
            size.y += itemTextSize.y + ITEM_NAME_OFFSET
            size.x = maxOf(size.x, itemTextSize.x)
            this.itemTextSize = itemTextSize
        } else {
            this.itemTextSize = null
        }

        val hoverTextSize = hoverText.size
        if (hoverTextSize.length2() > 0.0f) {
            size.y += hoverTextSize.y + HOVER_TEXT_OFFSET
            size.x = maxOf(size.x, hoverTextSize.x)
            this.hoverTextSize = hoverTextSize
        } else {
            this.hoverTextSize = null
        }

        _size = size.unsafe
        cacheUpToDate = false
    }

    override fun silentApply(): Boolean {
        val items = guiRenderer.context.session.player.items
        val itemSlot = items.hotbar
        val currentItem = items.inventory.getHotbarSlot(itemSlot)
        if (currentItem != lastItemStackNameShown || itemSlot != lastItemSlot) {
            lastItemStackNameShown = currentItem
            lastItemSlot = itemSlot
            currentItem?.getDisplayName(context.session.language)?.let { itemText._chatComponent = it; itemText.forceSilentApply() }
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
        prefMaxSize = Vec2f(-1, -1)

        val session = context.session
        val player = session.player

        player::experienceCondition.observeRendering(this) { core.experience.apply() }

        player.additional::gamemode.observeRendering(this) { forceApply() }

        player.items::hotbar.observeRendering(this) { core.base.apply() }

        session.events.listen<ChatMessageEvent> {
            if (it.message.type.position != ChatTextPositions.HOTBAR) {
                return@listen
            }
            hoverText.text = it.message.text
            hoverText.show()
        }
    }


    companion object : HUDBuilder<LayoutedGUIElement<HotbarElement>> {
        override val identifier: ResourceLocation = "minosoft:hotbar".toResourceLocation()
        private const val HOVER_TEXT_OFFSET = 15.0f
        private const val ITEM_NAME_OFFSET = 5.0f


        override fun register(gui: GUIRenderer) {
            gui.atlas.load(HotbarAirElement.ATLAS)
            gui.atlas.load(HeartAtlas.ATLAS)
            gui.atlas.load(HotbarBaseElement.ATLAS)
            gui.atlas.load(HotbarProtectionElement.ATLAS)
            gui.atlas.load(HotbarHungerElement.ATLAS)
            gui.atlas.load(HotbarExperienceBarElement.ATLAS)
        }

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<HotbarElement> {
            return LayoutedGUIElement(HotbarElement(guiRenderer))
        }
    }
}

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

package de.bixilon.minosoft.gui.rendering.gui.hud

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderUtil.runAsync
import de.bixilon.minosoft.gui.rendering.gui.GUIElementDrawer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.bossbar.BossbarLayout
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat.ChatElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.HotbarElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.CrosshairHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.PerformanceHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.debug.DebugHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard.ScoreboardSideElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab.TabListElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.title.TitleElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.WawlaHUDElement
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class HUDManager(
    override val guiRenderer: GUIRenderer,
) : GUIElementDrawer, Initializable, AsyncDrawable, Drawable {
    val context = guiRenderer.context
    private val hudElements: LockMap<ResourceLocation, HUDElement> = lockMapOf()

    override var lastTickTime = 0L

    var enabled: Boolean = true

    private var values: Collection<HUDElement> = emptyList()

    fun register(hudBuilder: HUDBuilder<*>) {
        val hudElement = hudBuilder.build(guiRenderer)
        hudElements[hudBuilder.identifier] = hudElement

        val toggleKeyBinding = hudBuilder.ENABLE_KEY_BINDING ?: return
        val toggleKeyBindingName = hudBuilder.ENABLE_KEY_BINDING_NAME ?: return

        context.input.bindings.register(toggleKeyBindingName, toggleKeyBinding, pressed = hudBuilder.DEFAULT_ENABLED) { hudElement.enabled = it }
    }

    private fun registerDefaults() {
        for (builder in DEFAULT_ELEMENTS) {
            builder.register(guiRenderer)
        }
    }

    private fun buildDefaults() {
        val latch = SimpleLatch(DEFAULT_ELEMENTS.size)

        for (builder in DEFAULT_ELEMENTS) {
            context.runAsync { register(builder); latch.dec() }
        }
        latch.await()
    }

    fun onScreenChange() {
        hudElements.lock.acquire()
        for (element in hudElements.values) {
            if (element is LayoutedGUIElement<*>) {
                element.element.forceApply()
            }
            element.apply()
        }
        hudElements.lock.release()
    }

    override fun init() {
        registerDefaults()

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.init()
        }

        context.input.bindings.register(
            "minosoft:enable_hud".toResourceLocation(), KeyBinding(
                KeyActions.STICKY to setOf(KeyCodes.KEY_F1),
            ), pressed = enabled
        ) { enabled = it }
    }

    override fun postInit() {
        buildDefaults()

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.postInit()
            if (element is LayoutedGUIElement<*>) {
                element.initMesh()
            }
        }
    }

    override fun drawAsync() {
        hudElements.lock.acquire()
        this.values = hudElements.values
        hudElements.lock.release()
        tickElements(values)
        prepareElements(values)
    }

    override fun draw() {
        drawElements(values)
    }

    operator fun <T : HUDElement> get(hudBuilder: HUDBuilder<T>): T? {
        return hudElements[hudBuilder.identifier]?.unsafeCast()
    }


    companion object {
        val DEFAULT_ELEMENTS = listOf(
            DebugHUDElement,
            CrosshairHUDElement,
            BossbarLayout,
            ChatElement,

            TabListElement,
            HotbarElement,
            PerformanceHUDElement,
            TitleElement,
            ScoreboardSideElement,
            WawlaHUDElement,
        )
    }
}

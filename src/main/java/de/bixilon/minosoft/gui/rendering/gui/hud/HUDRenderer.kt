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

package de.bixilon.minosoft.gui.rendering.gui.hud

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatchRendering
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.AbstractGUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.bossbar.BossbarLayout
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat.ChatElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat.InternalMessagesElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.HotbarElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.BreakProgressHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.CrosshairHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.DebugHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.WorldInfoHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard.ScoreboardSideElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab.TabListElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.title.TitleElement
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.renderer.Drawable
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.phases.OtherDrawable
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class HUDRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OtherDrawable, AbstractGUIRenderer {
    private val profile = connection.profiles.hud
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    val shader = renderWindow.renderSystem.createShader("minosoft:hud".toResourceLocation())
    override var scaledSize: Vec2i = renderWindow.window.size
    override var matrix: Mat4 = Mat4()
    private var enabled = true
    override var matrixChange = true
        private set
    override val framebuffer: Framebuffer
        get() = renderWindow.framebufferManager.gui.framebuffer
    override val polygonMode: PolygonModes
        get() = renderWindow.framebufferManager.gui.polygonMode

    private val hudElements: MutableMap<ResourceLocation, HUDElement> = synchronizedMapOf()

    private var lastTickTime = 0L

    val atlasManager = renderWindow.atlasManager

    override val skipOther: Boolean
        get() = !enabled

    fun registerElement(hudBuilder: HUDBuilder<*>) {
        val hudElement = hudBuilder.build(this)
        hudElements[hudBuilder.RESOURCE_LOCATION] = hudElement

        val toggleKeyBinding = hudBuilder.ENABLE_KEY_BINDING ?: return
        val toggleKeyBindingName = hudBuilder.ENABLE_KEY_BINDING_NAME ?: return

        renderWindow.inputHandler.registerKeyCallback(toggleKeyBindingName, toggleKeyBinding, defaultPressed = hudBuilder.DEFAULT_ENABLED) { hudElement.enabled = it }
    }

    private fun registerDefaultElements() {
        registerElement(DebugHUDElement)
        registerElement(CrosshairHUDElement)
        registerElement(BossbarLayout)
        registerElement(ChatElement)

        registerElement(InternalMessagesElement)
        registerElement(BreakProgressHUDElement)
        registerElement(TabListElement)
        registerElement(HotbarElement)
        registerElement(WorldInfoHUDElement)
        registerElement(TitleElement)
        registerElement(ScoreboardSideElement)
    }

    private fun recalculateMatrices(windowSize: Vec2i = renderWindow.window.size, scale: Float = profile.scale) {
        scaledSize = Vec2i(Vec2(windowSize) / scale)
        matrix = glm.ortho(0.0f, scaledSize.x.toFloat(), scaledSize.y.toFloat(), 0.0f)
        matrixChange = true

        for (element in hudElements.toSynchronizedMap().values) {
            if (element is LayoutedHUDElement<*>) {
                element.elementLayout.silentApply()
            }
            element.apply()
        }
    }

    override fun init(latch: CountUpAndDownLatch) {
        connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> { recalculateMatrices(it.size) })
        profile::scale.profileWatchRendering(this, profile = profile) { recalculateMatrices(scale = it) }
        if (!atlasManager.initialized) {
            atlasManager.init()
        }

        registerDefaultElements()

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.init()
        }

        renderWindow.inputHandler.registerKeyCallback("minosoft:enable_hud".toResourceLocation(), KeyBinding(
            mapOf(
                KeyAction.STICKY to setOf(KeyCodes.KEY_F1),
            ),
        ), defaultPressed = enabled) { enabled = it }
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        atlasManager.postInit()
        shader.load()
        renderWindow.textureManager.staticTextures.use(shader)

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.postInit()
            if (element is LayoutedHUDElement<*>) {
                element.initMesh()
            }
        }
    }

    private fun setup() {
        renderWindow.renderSystem.reset(blending = true)
        shader.use()
    }

    override fun drawOther() {
        val hudElements = hudElements.toSynchronizedMap().values

        val time = TimeUtil.time
        if (time - lastTickTime > ProtocolDefinition.TICK_TIME) {
            for (element in hudElements) {
                if (!element.enabled) {
                    continue
                }
                element.tick()
                if (element is Pollable) {
                    if (element.poll()) {
                        element.apply()
                    }
                }
            }

            lastTickTime = time
        }

        var z = 0
        for (element in hudElements) {
            if (!element.enabled) {
                continue
            }
            if (element is Drawable && !element.skipDraw) {
                element.draw()
            }
            if (element is LayoutedHUDElement<*>) {
                z += element.prepare(z)
            }
        }

        setup()

        for (element in hudElements) {
            if (element !is LayoutedHUDElement<*> || !element.enabled || element.mesh.data.isEmpty) {
                continue
            }
            element.mesh.draw()
        }

        if (matrixChange) {
            matrixChange = false
        }
    }

    operator fun <T : HUDElement> get(hudBuilder: HUDBuilder<T>): T? {
        return hudElements[hudBuilder.RESOURCE_LOCATION]?.unsafeCast()
    }

    companion object : RendererBuilder<HUDRenderer> {
        override val RESOURCE_LOCATION = "minosoft:hud".toResourceLocation()

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): HUDRenderer {
            return HUDRenderer(connection, renderWindow)
        }
    }
}

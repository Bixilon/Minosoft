/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.profile.change.listener.SimpleChangeListener.Companion.listenRendering
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasManager
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.bossbar.BossbarHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat.ChatHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.chat.InternalMessagesHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.HotbarHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.BreakProgressHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.CrosshairHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.DebugHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.WorldInfoHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard.ScoreboardHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab.TabListHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.title.TitleHUDElement
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.OtherDrawable
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class HUDRenderer(
    val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, OtherDrawable {
    private val profile = connection.profiles.hud
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    val shader = renderWindow.renderSystem.createShader("minosoft:hud".toResourceLocation())
    var scaledSize: Vec2i = renderWindow.window.size
    var matrix: Mat4 = Mat4()
    private var enabled = true
    var matrixChange = true
        private set

    private val hudElements: MutableMap<ResourceLocation, HUDElement> = synchronizedMapOf()

    private var lastTickTime = 0L

    val atlasManager = HUDAtlasManager(this)

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
        registerElement(BossbarHUDElement)
        registerElement(ChatHUDElement)

        registerElement(InternalMessagesHUDElement)
        registerElement(BreakProgressHUDElement)
        registerElement(TabListHUDElement)
        registerElement(HotbarHUDElement)
        registerElement(WorldInfoHUDElement)
        registerElement(TitleHUDElement)
        registerElement(ScoreboardHUDElement)
    }

    private fun recalculateMatrices(windowSize: Vec2i = renderWindow.window.size, scale: Float = profile.scale) {
        scaledSize = Vec2i(Vec2(windowSize) / scale)
        matrix = glm.ortho(0.0f, scaledSize.x.toFloat(), scaledSize.y.toFloat(), 0.0f)
        matrixChange = true

        for (element in hudElements.toSynchronizedMap().values) {
            if (element is LayoutedHUDElement<*>) {
                element.layout.silentApply()
            }
            element.apply()
        }
    }

    override fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> { recalculateMatrices(it.size) })
        profile::scale.listenRendering(this, profile = profile) { recalculateMatrices(scale = it) }
        atlasManager.init()

        registerDefaultElements()

        for (element in this.hudElements.toSynchronizedMap().values) {
            element.init()
        }

        renderWindow.inputHandler.registerKeyCallback("minosoft:enable_hud".toResourceLocation(), KeyBinding(
            mutableMapOf(
                KeyAction.STICKY to mutableSetOf(KeyCodes.KEY_F1),
            ),
        ), defaultPressed = enabled) { enabled = it }
    }

    override fun postInit() {
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
        renderWindow.renderSystem.clear(IntegratedBufferTypes.DEPTH_BUFFER)
        shader.use()
    }

    override fun drawOther() {
        val hudElements = hudElements.toSynchronizedMap().values

        val time = KUtil.time
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

        renderWindow.renderSystem.clear(IntegratedBufferTypes.DEPTH_BUFFER)
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

    operator fun <T : LayoutedHUDElement<*>> get(hudBuilder: HUDBuilder<*>): T? {
        return hudElements[hudBuilder.RESOURCE_LOCATION].unsafeCast()
    }

    companion object : RendererBuilder<HUDRenderer> {
        override val RESOURCE_LOCATION = "minosoft:hud_renderer".toResourceLocation()

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): HUDRenderer {
            return HUDRenderer(connection, renderWindow)
        }
    }
}

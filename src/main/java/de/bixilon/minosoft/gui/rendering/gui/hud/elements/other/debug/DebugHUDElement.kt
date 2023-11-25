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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.debug

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.concurrent.Reference
import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.kutil.math.simple.FloatMath.rounded10
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.string.StringUtil.truncate
import de.bixilon.kutil.unit.UnitFormatter.formatBytes
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.grid.GridGrow
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.grid.GridLayout
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.LineSpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.AutoTextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.properties.MinosoftProperties
import de.bixilon.minosoft.properties.MinosoftPropertiesLoader
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.SystemInformation

class DebugHUDElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable {
    private val connection = context.connection
    private val layout = GridLayout(guiRenderer, Vec2i(3, 1)).apply { parent = this@DebugHUDElement }
    override val layoutOffset: Vec2 = Vec2.EMPTY


    init {
        layout.columnConstraints[0].apply {
            grow = GridGrow.NEVER
        }
        layout.columnConstraints[2].apply {
            grow = GridGrow.NEVER
            alignment = HorizontalAlignments.RIGHT
        }

        apply()
    }


    override fun postInit() {
        layout[Vec2i(0, 0)] = initLeft()
        layout[Vec2i(2, 0)] = initRight()

        this.prefMaxSize = Vec2(-1, Int.MAX_VALUE)
        this.ignoreDisplaySize = true
    }

    private fun initLeft(): Element {
        val layout = RowLayout(guiRenderer)
        layout.margin = Vec4(2)
        layout += TextElement(guiRenderer, TextComponent(RunConfiguration.APPLICATION_NAME, ChatColors.RED))
        layout += AutoTextElement(guiRenderer, 1) { "FPS §d${context.renderStats.smoothAvgFPS.rounded10}§r; t=§d${context.renderStats.avgDrawTime.avg.formatNanos().replace('µ', 'u')}" } // rendering of µ eventually broken
        context.renderer[ChunkRenderer]?.apply {
            layout += AutoTextElement(guiRenderer, 1) { "C v=${visible.sizeString}, l=${loaded.size.format()}, cQ=${culledQueue.size.format()}, q=${meshingQueue.size.format()}, pT=${meshingQueue.tasks.size.format()}/${meshingQueue.tasks.max.format()}, lQ=${loadingQueue.size.format()}/${meshingQueue.maxMeshesToLoad.format()}, w=${connection.world.chunks.chunks.size.format()}" }
        }

        layout += context.renderer[EntitiesRenderer]?.let {
            AutoTextElement(guiRenderer, 1) { BaseComponent("E v=", it.visibility.size, ",ov=", it.visibility.opaque.size, ",tv=", it.visibility.translucent.size, ", t=", it.renderers.size, ", w=", connection.world.entities.size) }
        } ?: AutoTextElement(guiRenderer, 1) { "E w=${connection.world.entities.size.format()}" }

        context.renderer[ParticleRenderer]?.apply {
            layout += AutoTextElement(guiRenderer, 1) { "P t=${size.format()}" }
        }

        val audioProfile = connection.profiles.audio

        layout += AutoTextElement(guiRenderer, 1) {
            BaseComponent().apply {
                this += "S "
                if (connection.profiles.audio.skipLoading || !audioProfile.enabled) {
                    this += "§cdisabled"
                } else {
                    val audioPlayer = context.rendering.audioPlayer

                    val sources = audioPlayer.sourcesCount

                    this += sources - audioPlayer.availableSources
                    this += " / "
                    this += sources
                }
            }
        }

        layout += LineSpacerElement(guiRenderer)

        layout += TextElement(guiRenderer, BaseComponent("Account ", connection.account.username))
        layout += TextElement(guiRenderer, BaseComponent("Address ", connection.address))
        layout += TextElement(guiRenderer, BaseComponent("Network version ", connection.version))
        layout += TextElement(guiRenderer, BaseComponent("Server brand ", connection.serverInfo.brand)).apply { connection.serverInfo::brand.observe(this@DebugHUDElement) { this.text = BaseComponent("Server brand ", it.truncate(50)) } }

        layout += LineSpacerElement(guiRenderer)


        connection.camera.entity.physics.apply {
            // ToDo: Only update when the position changes
            layout += AutoTextElement(guiRenderer, 1) { with(position) { "XYZ ${x.format()} / ${y.format()} / ${z.format()}" } }
            layout += AutoTextElement(guiRenderer, 1) { with(positionInfo.blockPosition) { "Block ${x.format()} ${y.format()} ${z.format()}" } }
            layout += AutoTextElement(guiRenderer, 1) { with(positionInfo) { "Chunk ${inSectionPosition.format()} in (${chunkPosition.x.format()} ${sectionHeight.format()} ${chunkPosition.y.format()})" } }
            layout += AutoTextElement(guiRenderer, 1) {
                val text = BaseComponent("Facing ")

                Directions.byDirection(rotation.front).apply {
                    text += this
                    text += " "
                    text += vector
                }

                rotation.apply {
                    text += " yaw=§d${yaw.rounded10}§r, pitch=§d${pitch.rounded10}"
                }

                text
            }
        }

        layout += LineSpacerElement(guiRenderer)

        val chunk = connection.player.physics.positionInfo.chunk

        if (chunk == null) {
            layout += DebugWorldInfo(guiRenderer)
        }

        layout += LineSpacerElement(guiRenderer)

        layout += TextElement(guiRenderer, BaseComponent("Gamemode ", connection.player.gamemode)).apply {
            connection.player.additional::gamemode.observe(this) { text = BaseComponent("Gamemode ", it) }
        }

        layout += TextElement(guiRenderer, BaseComponent("Difficulty ", connection.world.difficulty?.difficulty, ", locked=", connection.world.difficulty?.locked)).apply {
            connection.world::difficulty.observe(this) { text = BaseComponent("Difficulty ", it?.difficulty, ", locked=", it?.locked) }
        }

        layout += TextElement(guiRenderer, "Time TBA").apply {
            connection.world::time.observe(this, instant = true) {
                text = BaseComponent(
                    "Time ", it.time, " (", it.phase, ")", ", cycling=", it.cycling, "\n",
                    "Date ", "day=", it.day, " (", it.moonPhase, ")"
                )
            }
        }
        layout += TextElement(guiRenderer, "Weather TBA").apply {
            connection.world::weather.observe(this, instant = true) {
                text = BaseComponent("Weather r=", it.rain, ", t=", it.thunder)
            }
        }

        layout += AutoTextElement(guiRenderer, 1) { "Fun effect: " + context.framebuffer.world.`fun`.effect?.identifier.format() }

        return layout
    }

    private fun initRight(): Element {
        val layout = RowLayout(guiRenderer, HorizontalAlignments.RIGHT)
        layout.margin = Vec4(2)
        layout += TextElement(guiRenderer, "Java ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit", properties = RIGHT)
        layout += TextElement(guiRenderer, "OS ${SystemInformation.OS_TEXT}", properties = RIGHT)

        layout += LineSpacerElement(guiRenderer)

        layout += AutoTextElement(guiRenderer, 1, RIGHT) { "Allocation rate ${AllocationRate.allocationRate.formatBytes()}/s" }

        SystemInformation.RUNTIME.apply {
            layout += AutoTextElement(guiRenderer, 1, RIGHT) {
                val total = maxMemory()
                val used = totalMemory() - freeMemory()
                "Memory ${(used * 100.0 / total).rounded10}% ${used.formatBytes()} / ${total.formatBytes()}"
            }
            layout += AutoTextElement(guiRenderer, 1, RIGHT) {
                val total = maxMemory()
                val allocated = totalMemory()
                "Allocated ${(allocated * 100.0 / total).rounded10}% ${allocated.formatBytes()} / ${total.formatBytes()}"
            }
        }

        layout += LineSpacerElement(guiRenderer)

        layout += TextElement(guiRenderer, "CPU ${SystemInformation.PROCESSOR_TEXT}", properties = RIGHT)
        layout += TextElement(guiRenderer, "Memory ${SystemInformation.SYSTEM_MEMORY.formatBytes()}", properties = RIGHT)


        layout += LineSpacerElement(guiRenderer)

        layout += TextElement(guiRenderer, "Display <?>", properties = RIGHT).apply {
            guiRenderer.context.connection.events.listen<ResizeWindowEvent> {
                text = "Display ${it.size.x.format()}x${it.size.y.format()}"
            }
        }

        context.system.apply {
            layout += TextElement(guiRenderer, "GPU $gpuType", properties = RIGHT)
            layout += TextElement(guiRenderer, "Version $version", properties = RIGHT)
        }

        MinosoftProperties.git?.let {
            layout += LineSpacerElement(guiRenderer)

            MinosoftPropertiesLoader.apply {
                layout += TextElement(guiRenderer, "Git ${it.commitShort}/${it.branch}", properties = RIGHT)
            }
        }

        layout += LineSpacerElement(guiRenderer)

        layout += TextElement(guiRenderer, "${connection.events.size.format()}x listeners", properties = RIGHT)

        layout += LineSpacerElement(guiRenderer)

        layout += AutoTextElement(guiRenderer, 20, properties = RIGHT) { "Dynamic textures ${context.textures.dynamic.size.format()}/${context.textures.dynamic.capacity.format()}" }

        layout += LineSpacerElement(guiRenderer)

        context.connection.camera.target.apply {
            layout += AutoTextElement(guiRenderer, 1, properties = RIGHT) {
                // ToDo: Tags
                target ?: "No target"
            }
        }
        return layout
    }

    private class DebugWorldInfo(guiRenderer: GUIRenderer) : RowLayout(guiRenderer) {
        private val chunk = Reference<Chunk?>(null)
        private var lastChunk = Reference<Chunk?>(null)
        private val world = guiRenderer.context.connection.world
        private val entity = guiRenderer.context.connection.player

        // TODO: Cleanup this class

        init {
            showWait()
        }

        private fun showWait() {
            clear()
            this += TextElement(guiRenderer, "Waiting for chunk...")
        }

        private fun updateInformation() {
            entity.physics.positionInfo.apply {
                this@DebugWorldInfo.chunk.value = chunk

                // TODO: also try getting chunk prototype
                if ((chunk == null && lastChunk.value == null) || (chunk != null && lastChunk.value != null)) {
                    // No update, elements will update themselves
                    return
                }
                if (chunk == null) {
                    lastChunk.value = null
                    showWait()
                    return
                }
                clear()

                this@DebugWorldInfo += AutoTextElement(guiRenderer, 1) { BaseComponent("Sky properties ", entity.connection.world.dimension.effects) }
                this@DebugWorldInfo += AutoTextElement(guiRenderer, 1) { BaseComponent("Biome ", biome) }
                this@DebugWorldInfo += AutoTextElement(guiRenderer, 1) { with(entity.connection.world.getLight(entity.renderInfo.eyePosition.blockPosition)) { BaseComponent("Light block=", (this and SectionLight.BLOCK_LIGHT_MASK), ", sky=", ((this and SectionLight.SKY_LIGHT_MASK) shr 4)) } }
                this@DebugWorldInfo += AutoTextElement(guiRenderer, 1) { BaseComponent("Fully loaded: ", chunk.neighbours.complete) }

                lastChunk.value = chunk
            }
        }

        override fun tick() {
            // ToDo: Make event driven
            updateInformation()

            super.tick()
        }
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        layout.forceRender(offset, consumer, options)
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }

    override fun onChildChange(child: Element) {
        super.onChildChange(child)
        forceSilentApply()
    }

    override fun tick() {
        layout.tick()
    }

    companion object : HUDBuilder<LayoutedGUIElement<DebugHUDElement>> {
        override val identifier: ResourceLocation = "minosoft:debug_hud".toResourceLocation()
        override val ENABLE_KEY_BINDING_NAME: ResourceLocation = "minosoft:enable_debug_hud".toResourceLocation()
        override val DEFAULT_ENABLED: Boolean = false
        override val ENABLE_KEY_BINDING: KeyBinding = KeyBinding(
            KeyActions.STICKY to setOf(KeyCodes.KEY_F3),
        )
        private val RIGHT = TextRenderProperties(HorizontalAlignments.RIGHT)

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<DebugHUDElement> {
            return LayoutedGUIElement(DebugHUDElement(guiRenderer)).apply { enabled = false }
        }
    }
}

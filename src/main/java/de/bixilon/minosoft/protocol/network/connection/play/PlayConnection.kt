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

package de.bixilon.minosoft.protocol.network.connection.play

import de.bixilon.minosoft.assets.AssetsLoader
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.config.profile.ConnectionProfiles
import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.bossbar.BossbarManager
import de.bixilon.minosoft.data.commands.CommandRootNode
import de.bixilon.minosoft.data.language.LanguageManager
import de.bixilon.minosoft.data.physics.CollisionDetector
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.data.player.tab.TabList
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocationAble
import de.bixilon.minosoft.data.registries.recipes.Recipes
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.versions.MinecraftRegistryFixer
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager
import de.bixilon.minosoft.data.tags.DefaultTags
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.modding.event.events.ProtocolStateChangeEvent
import de.bixilon.minosoft.modding.event.events.RegistriesLoadEvent
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.clientsettings.ClientSettingsManager
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.LoginStartC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.*
import de.bixilon.minosoft.terminal.CLI
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.terminal.commands.commands.Command
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.time.TimeWorker
import de.bixilon.minosoft.util.task.time.TimeWorkerTask


class PlayConnection(
    val address: ServerAddress,
    val account: Account,
    val version: Version,
    val profiles: ConnectionProfiles = ConnectionProfiles(),
) : Connection() {
    val settingsManager = ClientSettingsManager(this)
    val registries = Registries()
    val recipes = Recipes()
    val world = World(this)
    val tabList = TabList()
    val scoreboardManager = ScoreboardManager(this)
    val bossbarManager = BossbarManager()

    @Deprecated(message = "PacketSender is deprecated")
    val sender = PacketSender(this)
    val serverInfo = ServerInfo()
    lateinit var assetsManager: AssetsManager
        private set
    val tags: MutableMap<ResourceLocation, Map<ResourceLocation, Tag<Any>>> = synchronizedMapOf()
    lateinit var language: LanguageManager

    var commandRootNode: CommandRootNode? = null


    var rendering: Rendering? = null
        private set
    lateinit var player: LocalPlayerEntity
        private set

    private lateinit var entityTickTask: TimeWorkerTask
    private lateinit var worldTickTask: TimeWorkerTask
    private lateinit var randomTickTask: TimeWorkerTask
    val collisionDetector = CollisionDetector(this)
    var retry = true

    var state = PlayConnectionStates.WAITING
        set(value) {
            field = value
            fireEvent(PlayConnectionStateChangeEvent(this, value))
        }

    override var error: Throwable?
        get() = super.error
        set(value) {
            super.error = value
            value?.let { state = PlayConnectionStates.ERROR }
            value?.report()
        }

    init {
        MinecraftRegistryFixer(this)
    }

    override var protocolState: ProtocolStates = ProtocolStates.DISCONNECTED
        set(value) {
            val previousConnectionState = protocolState
            field = value
            // handle callbacks
            fireEvent(ProtocolStateChangeEvent(this, previousConnectionState, value))
            when (value) {
                ProtocolStates.HANDSHAKING -> {
                    state = PlayConnectionStates.HANDSHAKING
                    ACTIVE_CONNECTIONS += this
                    for ((validators, invokers) in GlobalEventMaster.specificEventInvokers) {
                        var valid = false
                        for (serverAddress in validators) {
                            if (serverAddress.check(address)) {
                                valid = true
                                break
                            }
                        }
                        if (valid) {
                            registerEvents(*invokers.toTypedArray())
                        }
                    }


                    network.sendPacket(HandshakeC2SP(address, ProtocolStates.LOGIN, version.protocolId))
                    // after sending it, switch to next state
                    protocolState = ProtocolStates.LOGIN
                }
                ProtocolStates.LOGIN -> {
                    state = PlayConnectionStates.LOGGING_IN
                    this.network.sendPacket(LoginStartC2SP(this.player))
                }
                ProtocolStates.PLAY -> {
                    state = PlayConnectionStates.JOINING
                    // ToDO: Minosoft.CONNECTIONS[connectionId] = this

                    if (CLI.getCurrentConnection() == null) {
                        CLI.setCurrentConnection(this)
                    }
                    entityTickTask = TimeWorkerTask(ProtocolDefinition.TICK_TIME / 5) {
                        for (entity in world.entities) {
                            entity.tick()
                        }
                    }
                    TimeWorker += entityTickTask

                    worldTickTask = TimeWorkerTask(ProtocolDefinition.TICK_TIME, maxDelayTime = ProtocolDefinition.TICK_TIME / 2) {
                        world.tick()
                    }
                    TimeWorker += worldTickTask

                    randomTickTask = TimeWorkerTask(ProtocolDefinition.TICK_TIME, maxDelayTime = ProtocolDefinition.TICK_TIME / 2) {
                        world.randomTick()
                    }
                    TimeWorker += randomTickTask

                    registerEvent(CallbackEventInvoker.of<ChatMessageReceiveEvent> {
                        val additionalPrefix = when (it.position) {
                            ChatTextPositions.SYSTEM_MESSAGE -> "[SYSTEM] "
                            ChatTextPositions.ABOVE_HOTBAR -> "[HOTBAR] "
                            else -> ""
                        }
                        Log.log(LogMessageType.CHAT_IN, additionalPrefix = ChatComponent.of(additionalPrefix)) { it.message }
                    })
                }
                ProtocolStates.DISCONNECTED -> {
                    if (previousConnectionState.connected) {
                        wasConnected = true
                    }
                    // unregister all custom recipes
                    this.recipes.removeCustomRecipes()
                    //ToDo: Minosoft.CONNECTIONS.remove(connectionId)
                    if (CLI.getCurrentConnection() == this) {
                        CLI.setCurrentConnection(null)
                        Command.print("Disconnected from current connection!")
                    }
                    if (this::entityTickTask.isInitialized) {
                        TimeWorker.removeTask(entityTickTask)
                    }
                    if (this::worldTickTask.isInitialized) {
                        TimeWorker.removeTask(worldTickTask)
                    }
                    if (this::randomTickTask.isInitialized) {
                        TimeWorker.removeTask(randomTickTask)
                    }
                    state = PlayConnectionStates.DISCONNECTED
                    ACTIVE_CONNECTIONS -= this
                }
                else -> {
                }
            }
        }

    fun connect(latch: CountUpAndDownLatch = CountUpAndDownLatch(1)) {
        check(!wasConnected) { "Connection was already connected!" }
        try {
            state = PlayConnectionStates.LOADING
            fireEvent(RegistriesLoadEvent(this, registries, RegistriesLoadEvent.States.PRE))
            version.load(profiles.resources)
            registries.parentRegistries = version.registries

            assetsManager = AssetsLoader.create(profiles.resources, version)
            Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Downloading and verifying assets. This might take a while..." }
            assetsManager.load(latch)
            Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Assets verified!" }

            language = LanguageManager.load(profiles.connection.language ?: profiles.eros.general.language, version, assetsManager)

            fireEvent(RegistriesLoadEvent(this, registries, RegistriesLoadEvent.States.POST))
            player = LocalPlayerEntity(account, this)

            if (!RunConfiguration.DISABLE_RENDERING) {
                val renderer = Rendering(this)
                this.rendering = renderer
                val renderLatch = CountUpAndDownLatch(0, latch)
                renderer.init(renderLatch)
                renderLatch.awaitWithChange()
            }
            Log.log(LogMessageType.NETWORK_STATUS, level = LogLevels.INFO) { "Connecting to server: $address" }
            network.connect(address)
            state = PlayConnectionStates.ESTABLISHING
        } catch (exception: Throwable) {
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.FATAL) { exception }
            version.unload()
            if (this::assetsManager.isInitialized) {
                assetsManager.unload()
            }
            error = exception
            retry = false
        }
        latch.dec()
    }

    override fun getPacketId(packetType: PacketTypes.C2S): Int {
        // ToDo: Improve speed
        return version.c2sPackets[packetType.state]?.indexOf(packetType) ?: Protocol.getPacketId(packetType) ?: error("Can not find packet $packetType for $version")
    }

    override fun getPacketById(packetId: Int): PacketTypes.S2C {
        return version.s2cPackets[protocolState]?.getOrNull(packetId) ?: Protocol.getPacketById(protocolState, packetId) ?: let {
            // wtf, notchain sends play disconnect packet in login state...
            if (protocolState != ProtocolStates.LOGIN) {
                return@let null
            }
            val playPacket = version.s2cPackets[ProtocolStates.PLAY]?.getOrNull(packetId)
            if (playPacket == PacketTypes.S2C.PLAY_KICK) {
                return@let playPacket
            }
            null
        } ?: error("Can not find packet $packetId in $protocolState for $version")
    }

    override fun handlePacket(packet: S2CPacket) {
        if (!protocolState.connected) {
            return
        }
        try {
            packet.log(profiles.other.log.reducedProtocolLog)
            val event = PacketReceiveEvent(this, packet)
            if (fireEvent(event)) {
                return
            }
            if (packet is PlayS2CPacket) {
                packet.handle(this)
            }
        } catch (exception: Throwable) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.WARN) { exception }
        }
    }

    fun inTag(`object`: Any?, tagType: ResourceLocation, tag: ResourceLocation): Boolean {

        fun fallback(): Boolean {
            if (`object` !is ResourceLocationAble) {
                return false
            }
            return DefaultTags.TAGS[tagType]?.get(tag)?.contains(`object`.resourceLocation) == true
        }

        (tags[tagType] ?: return fallback()).let { map ->
            (map[tag] ?: return fallback()).let {
                return it.entries.contains(`object`)
            }
        }
    }

    companion object {
        val ACTIVE_CONNECTIONS: MutableSet<PlayConnection> = synchronizedSetOf()
    }
}

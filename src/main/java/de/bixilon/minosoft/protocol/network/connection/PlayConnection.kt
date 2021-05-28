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

package de.bixilon.minosoft.protocol.network.connection

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.ChatTextPositions
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.assets.MultiAssetsManager
import de.bixilon.minosoft.data.commands.CommandRootNode
import de.bixilon.minosoft.data.mappings.MappingsLoadingException
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.recipes.Recipes
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.data.mappings.versions.Version
import de.bixilon.minosoft.data.physics.CollisionDetector
import de.bixilon.minosoft.data.player.Player
import de.bixilon.minosoft.data.player.tab.TabList
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.EventInvoker
import de.bixilon.minosoft.modding.event.events.ChatMessageReceiveEvent
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.LoginStartC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.*
import de.bixilon.minosoft.terminal.CLI
import de.bixilon.minosoft.terminal.commands.commands.Command
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
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
) : Connection() {
    val recipes = Recipes()
    val world = World(this)
    val tabList = TabList()
    val scoreboardManager = ScoreboardManager()
    val registries = Registries()
    val sender = PacketSender(this)
    lateinit var assetsManager: MultiAssetsManager
        private set
    val tags: MutableMap<ResourceLocation, Map<ResourceLocation, Tag<Any>>> = synchronizedMapOf()

    var commandRootNode: CommandRootNode? = null


    var rendering: Rendering? = null
        private set
    lateinit var player: Player
        private set

    lateinit var velocityHandlerTask: TimeWorkerTask
    private var velocityHandlerLastExecutionTime: Long = 0L
    lateinit var worldTickTask: TimeWorkerTask
    val collisionDetector = CollisionDetector(this)

    override var connectionState: ConnectionStates = ConnectionStates.DISCONNECTED
        set(value) {
            val previousConnectionState = connectionState
            field = value
            // handle callbacks
            fireEvent(ConnectionStateChangeEvent(this, previousConnectionState, connectionState))
            when (value) {
                ConnectionStates.HANDSHAKING -> {
                    for (eventManager in Minosoft.EVENT_MANAGERS) {
                        for ((addresses, specificEventListener) in eventManager.specificEventListeners) {
                            var valid = false
                            for (serverAddress in addresses) {
                                if (serverAddress.check(address)) {
                                    valid = true
                                    break
                                }
                            }
                            if (valid) {
                                eventListeners.addAll(specificEventListener)
                            }
                        }
                    }
                    eventListeners.sortWith { a: EventInvoker, b: EventInvoker ->
                        -(b.priority.ordinal - a.priority.ordinal)
                    }


                    network.sendPacket(HandshakeC2SP(address, ConnectionStates.LOGIN, version.protocolId))
                    // after sending it, switch to next state
                    // after sending it, switch to next state
                    connectionState = ConnectionStates.LOGIN
                }
                ConnectionStates.LOGIN -> {
                    this.network.sendPacket(LoginStartC2SP(this.player))
                }
                ConnectionStates.PLAY -> {
                    Minosoft.CONNECTIONS[connectionId] = this

                    if (CLI.getCurrentConnection() == null) {
                        CLI.setCurrentConnection(this)
                    }
                    velocityHandlerLastExecutionTime = System.currentTimeMillis()
                    velocityHandlerTask = TimeWorkerTask(ProtocolDefinition.TICK_TIME / 5) {
                        val currentTime = System.currentTimeMillis()
                        val deltaTime = currentTime - velocityHandlerLastExecutionTime
                        if (deltaTime > 0L) {
                            for (entity in world.entities) {
                                entity.tick(deltaTime)
                            }
                            rendering?.renderWindow?.inputHandler?.camera?.checkPosition()
                        }
                        velocityHandlerLastExecutionTime = currentTime
                    }
                    TimeWorker.addTask(velocityHandlerTask)

                    worldTickTask = TimeWorkerTask(ProtocolDefinition.TICK_TIME) {
                        world.realTick()
                    }
                    TimeWorker.addTask(worldTickTask)

                    registerEvent(CallbackEventInvoker.of<ChatMessageReceiveEvent> {
                        val additionalPrefix = when (it.position) {
                            ChatTextPositions.SYSTEM_MESSAGE -> "[SYSTEM] "
                            ChatTextPositions.ABOVE_HOTBAR -> "[HOTBAR] "
                            else -> ""
                        }
                        Log.log(LogMessageType.CHAT_IN, additionalPrefix = ChatComponent.of(additionalPrefix)) { it.message }
                    })
                }
                ConnectionStates.DISCONNECTED -> {
                    // unregister all custom recipes
                    this.recipes.removeCustomRecipes()
                    Minosoft.CONNECTIONS.remove(connectionId)
                    if (CLI.getCurrentConnection() == this) {
                        CLI.setCurrentConnection(null)
                        Command.print("Disconnected from current connection!")
                    }
                    if (this::velocityHandlerTask.isInitialized) {
                        TimeWorker.removeTask(velocityHandlerTask)
                    }
                    if (this::worldTickTask.isInitialized) {
                        TimeWorker.removeTask(worldTickTask)
                    }
                }
                else -> {
                }
            }
        }

    fun connect(latch: CountUpAndDownLatch) {
        try {
            version.load(latch) // ToDo: show gui loader
            assetsManager = MultiAssetsManager(version.assetsManager, Minosoft.MINOSOFT_ASSETS_MANAGER, Minosoft.MINECRAFT_FALLBACK_ASSETS_MANAGER)
            registries.parentRegistries = version.registries
            player = Player(account, this)

            if (!RenderConstants.DISABLE_RENDERING && !StaticConfiguration.HEADLESS_MODE) {
                val renderer = Rendering(this)
                this.rendering = renderer
                val renderLatch = CountUpAndDownLatch(0, latch)
                renderer.init(renderLatch)
                renderLatch.awaitWithChange()
            }
            Log.log(LogMessageType.NETWORK_STATUS, level = LogLevels.INFO) { "Connecting to server: $address" }
            network.connect(address)
        } catch (exception: Throwable) {
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.FATAL) { exception }
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.FATAL) { "Could not load version $version. This version seems to be unsupported" }
            version.unload()
            lastException = MappingsLoadingException("Mappings could not be loaded", exception)
            connectionState = ConnectionStates.FAILED_NO_RETRY
        }
        latch.dec()
    }


    override fun getPacketId(packetType: PacketTypes.C2S): Int {
        return version.getPacketId(packetType) ?: Protocol.getPacketId(packetType) ?: error("Can not find packet $packetType for $version")
    }

    override fun getPacketById(packetId: Int): PacketTypes.S2C {
        return version.getPacketById(connectionState, packetId) ?: Protocol.getPacketById(connectionState, packetId) ?: let {
            // wtf, notchain sends play disconnect packet in login state...
            if (connectionState != ConnectionStates.LOGIN) {
                return@let null
            }
            val playPacket = version.getPacketById(ConnectionStates.PLAY, packetId)
            if (playPacket == PacketTypes.S2C.PLAY_KICK) {
                return@let playPacket
            }
            null
        } ?: error("Can not find packet $packetId in $connectionState for $version")
    }

    override fun handlePacket(packet: S2CPacket) {
        if (!isConnected) {
            return
        }
        try {
            packet.log()
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

    val eventListenerSize: Int
        get() = eventListeners.size
}

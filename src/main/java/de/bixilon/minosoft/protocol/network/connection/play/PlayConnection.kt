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

package de.bixilon.minosoft.protocol.network.connection.play

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.worker.task.TaskWorker
import de.bixilon.kutil.concurrent.worker.task.WorkerTask
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.assets.AssetsLoader
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.commands.nodes.RootNode
import de.bixilon.minosoft.config.profile.ConnectionProfiles
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.bossbar.BossbarManager
import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.PlayerPrivateKey
import de.bixilon.minosoft.data.entities.entities.player.tab.TabList
import de.bixilon.minosoft.data.language.LanguageUtil
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.physics.CollisionDetector
import de.bixilon.minosoft.data.registries.fixer.MinecraftRegistryFixer
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager
import de.bixilon.minosoft.data.tags.DefaultTags
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionCreateEvent
import de.bixilon.minosoft.modding.event.events.loading.RegistriesLoadEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.modding.loader.LoadingPhases
import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.channel.ConnectionChannelHandler
import de.bixilon.minosoft.protocol.network.connection.play.channel.DefaultChannelHandlers
import de.bixilon.minosoft.protocol.network.connection.play.settings.ClientSettingsManager
import de.bixilon.minosoft.protocol.network.connection.play.tick.ConnectionTicker
import de.bixilon.minosoft.protocol.network.connection.play.util.ConnectionUtil
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.StartC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*


class PlayConnection(
    val address: ServerAddress,
    val account: Account,
    override val version: Version,
    val profiles: ConnectionProfiles = ConnectionProfiles(),
) : Connection() {
    val sessionId = KUtil.secureRandomUUID()
    val settingsManager = ClientSettingsManager(this)
    val registries = Registries()
    val world = World(this)
    val tabList = TabList()
    val scoreboardManager = ScoreboardManager(this)
    val bossbarManager = BossbarManager()
    val util = ConnectionUtil(this)
    val ticker = ConnectionTicker(this)
    val channels = ConnectionChannelHandler(this)
    val serverInfo = ServerInfo()

    lateinit var assetsManager: AssetsManager
        private set
    val tags: MutableMap<ResourceLocation, Map<ResourceLocation, Tag<Any>>> = synchronizedMapOf()
    lateinit var language: Translator


    @Deprecated("will be removed once split into modules")
    var rendering: Rendering? = null
        private set
    lateinit var player: LocalPlayerEntity
        private set

    val collisionDetector = CollisionDetector(this)
    var retry = true

    var state by observed(PlayConnectionStates.WAITING)

    var rootNode: RootNode? = null


    init {
        var errored = false
        this::error.observe(this) {
            if (errored || it == null) return@observe
            ERRORED_CONNECTIONS += this
            state = PlayConnectionStates.ERROR
            error.report()
            errored = true
        }
        MinecraftRegistryFixer.register(this)
        DefaultChannelHandlers.register(this)

        network::connected.observe(this) {
            if (it) {
                ACTIVE_CONNECTIONS += this
                ERRORED_CONNECTIONS -= this

                state = PlayConnectionStates.HANDSHAKING
                network.send(HandshakeC2SP(address, ProtocolStates.LOGIN, version.protocolId))
                // after sending it, switch to next state
                network.state = ProtocolStates.LOGIN
            } else {
                wasConnected = true
                assetsManager.unload()
                state = PlayConnectionStates.DISCONNECTED
                ACTIVE_CONNECTIONS -= this
                if (CLI.connection === this) {
                    CLI.connection = null
                }
            }
        }
        network::state.observe(this) { state ->
            when (state) {
                ProtocolStates.HANDSHAKING, ProtocolStates.STATUS -> throw IllegalStateException("Invalid state!")
                ProtocolStates.LOGIN -> {
                    this.state = PlayConnectionStates.LOGGING_IN
                    this.network.send(StartC2SP(this.player, this.sessionId))
                }

                ProtocolStates.PLAY -> {
                    this.state = PlayConnectionStates.JOINING

                    if (CLI.connection == null) {
                        CLI.connection = this
                    }

                    events.register(CallbackEventListener.of<ChatMessageEvent> {
                        val additionalPrefix = when (it.message.type.position) {
                            ChatTextPositions.SYSTEM -> "[SYSTEM] "
                            ChatTextPositions.HOTBAR -> "[HOTBAR] "
                            else -> ""
                        }
                        Log.log(LogMessageType.CHAT_IN, additionalPrefix = ChatComponent.of(additionalPrefix)) { it.message.text }
                    })
                }
            }
        }
        ticker.init()

        GlobalEventMaster.fire(PlayConnectionCreateEvent(this))
    }

    fun connect(latch: CountUpAndDownLatch = CountUpAndDownLatch(1)) {
        val count = latch.count
        check(!wasConnected) { "Connection was already connected!" }
        try {
            state = PlayConnectionStates.WAITING_MODS
            ModLoader.await(LoadingPhases.BOOT)

            state = PlayConnectionStates.LOADING_ASSETS
            var error: Throwable? = null
            val taskWorker = TaskWorker(errorHandler = { _, exception -> if (error == null) error = exception }, criticalErrorHandler = { _, exception -> if (error == null) error = exception })
            taskWorker += {
                events.fire(RegistriesLoadEvent(this, registries, RegistriesLoadEvent.States.PRE))
                registries.parent = version.load(profiles.resources, latch)
                events.fire(RegistriesLoadEvent(this, registries, RegistriesLoadEvent.States.POST))
            }

            taskWorker += {
                Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Downloading and verifying assets. This might take a while..." }
                assetsManager = AssetsLoader.create(profiles.resources, version, latch)
                assetsManager.load(latch)
                Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Assets verified!" }
            }
            var privateKey: PlayerPrivateKey? = null
            if (version.requiresSignedChat && !profiles.connection.signature.disableKeys) {
                taskWorker += WorkerTask(optional = true) {
                    val minecraftKey = account.fetchKey(latch) ?: return@WorkerTask
                    minecraftKey.requireSignature(account.uuid)
                    privateKey = PlayerPrivateKey(
                        expiresAt = minecraftKey.expiresAt,
                        signature = minecraftKey.getSignature(version.versionId),
                        private = minecraftKey.pair.private,
                        public = minecraftKey.pair.public,
                    )
                }
            }
            taskWorker.work(latch)
            error?.let { throw it }

            state = PlayConnectionStates.LOADING

            language = LanguageUtil.load(profiles.connection.language ?: profiles.eros.general.language, version, assetsManager)

            player = LocalPlayerEntity(account, this, privateKey)
            settingsManager.initSkins()

            if (!RunConfiguration.DISABLE_RENDERING) {
                val rendering = Rendering(this)
                this.rendering = rendering
                val renderLatch = CountUpAndDownLatch(0, latch)
                rendering.start(renderLatch)
                renderLatch.awaitWithChange()
            }
            latch.dec() // remove initial value
            Log.log(LogMessageType.NETWORK_STATUS, level = LogLevels.INFO) { "Connecting to server: $address" }
            network.connect(address, profiles.other.nativeNetwork)
            state = PlayConnectionStates.ESTABLISHING
        } catch (exception: Throwable) {
            Log.log(LogMessageType.VERSION_LOADING, level = LogLevels.FATAL) { exception }
            if (this::assetsManager.isInitialized) {
                assetsManager.unload()
            }
            error = exception
            retry = false
        }
        latch.count = count
    }

    @Deprecated("ToDo: Tag manager")
    fun inTag(`object`: Any?, tagType: ResourceLocation, tag: ResourceLocation): Boolean {

        fun fallback(): Boolean {
            if (`object` !is Identified) {
                return false
            }
            return DefaultTags.TAGS[tagType]?.get(tag)?.contains(`object`.identifier) == true
        }

        (tags[tagType] ?: return fallback()).let { map ->
            (map[tag] ?: return fallback()).let {
                return it.entries.contains(`object`)
            }
        }
    }

    override fun disconnect() {
        state = PlayConnectionStates.DISCONNECTED
    }

    companion object {
        val ACTIVE_CONNECTIONS: MutableSet<PlayConnection> = synchronizedSetOf()
        val ERRORED_CONNECTIONS: MutableSet<PlayConnection> = synchronizedSetOf()

        fun collectConnections(): Array<PlayConnection> {
            val result = ACTIVE_CONNECTIONS.toSynchronizedSet()
            result += ERRORED_CONNECTIONS.toSynchronizedSet()

            return result.toTypedArray()
        }
    }
}

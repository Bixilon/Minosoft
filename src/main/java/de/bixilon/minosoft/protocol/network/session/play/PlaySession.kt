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

package de.bixilon.minosoft.protocol.network.session.play

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.AbstractLatch.Companion.child
import de.bixilon.kutil.latch.CallbackLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.AssetsLoader
import de.bixilon.minosoft.assets.session.SessionAssetsManager
import de.bixilon.minosoft.camera.SessionCamera
import de.bixilon.minosoft.commands.nodes.SessionNode
import de.bixilon.minosoft.config.profile.SelectedProfiles
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.bossbar.BossbarManager
import de.bixilon.minosoft.data.chat.ChatTextPositions
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.SignatureKeyManagement
import de.bixilon.minosoft.data.entities.entities.player.tab.TabList
import de.bixilon.minosoft.data.language.LanguageUtil
import de.bixilon.minosoft.data.language.translate.Translator
import de.bixilon.minosoft.data.registries.fallback.tags.FallbackTags
import de.bixilon.minosoft.data.registries.fixer.RegistriesFixer
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.gui.rendering.RenderingOptions
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.modding.event.events.loading.RegistriesLoadEvent
import de.bixilon.minosoft.modding.event.events.session.play.PlaySessionCreateEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.modding.loader.phase.DefaultModPhases
import de.bixilon.minosoft.protocol.ServerConnection
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.channel.DefaultChannelHandlers
import de.bixilon.minosoft.protocol.network.session.play.channel.SessionChannelHandler
import de.bixilon.minosoft.protocol.network.session.play.settings.ClientSettingsManager
import de.bixilon.minosoft.protocol.network.session.play.tick.SessionTicker
import de.bixilon.minosoft.protocol.network.session.play.util.SessionUtil
import de.bixilon.minosoft.protocol.packets.c2s.handshake.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.login.StartC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.tags.TagManager
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.startInit
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.concurrent.atomic.AtomicInteger


class PlaySession(
    val connection: ServerConnection,
    val account: Account,
    override val version: Version,
    val profiles: SelectedProfiles = SelectedProfiles(),
) : Session() {
    val sessionId = KUtil.secureRandomUUID()
    val settingsManager = ClientSettingsManager(this)
    val registries = Registries(version = version)
    val world = World(this)
    val tabList = TabList()
    val scoreboard = ScoreboardManager(this)
    val bossbarManager = BossbarManager()
    val util = SessionUtil(this)
    val ticker = SessionTicker(this)
    val channels = SessionChannelHandler(this)
    val serverInfo = ServerInfo()
    val sequence = AtomicInteger(1)


    lateinit var assets: SessionAssetsManager
        private set
    lateinit var language: Translator


    @Deprecated("will be removed once split into modules")
    var rendering: Rendering? = null
        private set
    val player: LocalPlayerEntity = unsafeNull()
    val camera = SessionCamera(this)

    var retry = true

    var state by observed(PlaySessionStates.WAITING)

    var commands: SessionNode? = null
    var tags: TagManager = TagManager()
    val legacyTags: TagManager = unsafeNull()


    init {
        var errored = false
        this::error.observe(this) {
            if (it == null) return@observe
            Log.log(LogMessageType.GENERAL, LogLevels.FATAL) { it }
            if (errored) return@observe
            ERRORED_CONNECTIONS += this
            cleanupErrors()
            state = PlaySessionStates.ERROR
            error.report()
            errored = true
        }
        RegistriesFixer.register(this)
        DefaultChannelHandlers.register(this)

        connection::active.observe(this) {
            if (it) {
                ACTIVE_CONNECTIONS += this
                ERRORED_CONNECTIONS -= this

                if (connection is NetworkConnection) {
                    state = PlaySessionStates.HANDSHAKING
                    val address = connection.unsafeCast<NetworkConnection>().address
                    connection.send(HandshakeC2SP(address.hostname, address.port, HandshakeC2SP.Actions.PLAY, version.protocolId))
                    // after sending it, switch to next state
                    connection.state = ProtocolStates.LOGIN
                }
            } else {
                established = true
                assets.unload()
                state = PlaySessionStates.DISCONNECTED
                ACTIVE_CONNECTIONS -= this
                if (CLI.session === this) {
                    CLI.session = null
                }
            }
        }
        if (connection is NetworkConnection) {
            connection::state.observe(this) { state ->
                when (state) {
                    ProtocolStates.STATUS -> Broken("Invalid state!")
                    ProtocolStates.LOGIN -> {
                        this.state = PlaySessionStates.LOGGING_IN
                        world.biomes.init()
                        connection.send(StartC2SP(this.player, this.sessionId))
                    }

                    ProtocolStates.PLAY -> this.state = PlaySessionStates.JOINING
                    else -> Unit
                }
            }
        }
        ticker.init()

        GlobalEventMaster.fire(PlaySessionCreateEvent(this))

        events.listen<ChatMessageEvent> {
            val additionalPrefix = when (it.message.type.position) {
                ChatTextPositions.SYSTEM -> "[SYSTEM] "
                ChatTextPositions.HOTBAR -> "[HOTBAR] "
                else -> ""
            }
            Log.log(LogMessageType.CHAT_IN, level = if (it.message.type.position == ChatTextPositions.HOTBAR) LogLevels.VERBOSE else LogLevels.INFO, prefix = ChatComponent.of(additionalPrefix)) { it.message.text }
        }
        if (CLI.session == null) {
            CLI.session = this
        }
    }


    fun connect(latch: AbstractLatch? = null) {
        check(!established) { "Session was already connected!" }
        try {
            state = PlaySessionStates.WAITING_MODS
            DefaultModPhases.BOOT.await()

            state = PlaySessionStates.LOADING_ASSETS
            var error: Throwable? = null
            val worker = UnconditionalWorker(errorHandler = { exception -> if (error == null) error = exception })
            worker += {
                events.fire(RegistriesLoadEvent(this, registries, RegistriesLoadEvent.States.PRE))
                registries.parent = version.load(profiles.resources, latch.child(0))
                registries.fluid.updateWaterLava()
                events.fire(RegistriesLoadEvent(this, registries, RegistriesLoadEvent.States.POST))
                this::legacyTags.forceSet(FallbackTags.map(registries))
            }

            worker += {
                Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Downloading and verifying assets. This might take a while..." }
                assets = AssetsLoader.create(profiles.resources, version)
                assets.load(latch)
                Log.log(LogMessageType.ASSETS, LogLevels.INFO) { "Assets verified!" }
            }

            val keyManagement = SignatureKeyManagement(this, account)
            if (version.requiresSignedChat && !profiles.session.signature.disableKeys && connection is NetworkConnection) {
                worker += { keyManagement.init(latch) }
            }

            worker.work(latch)
            error?.let { throw it }

            state = PlaySessionStates.LOADING

            language = LanguageUtil.load(profiles.session.language ?: profiles.eros.general.language, version, assets)

            this::player.forceSet(LocalPlayerEntity(account, this, keyManagement))
            world.entities.add(null, null, player)
            settingsManager.initSkins()
            player.startInit()

            camera.init()


            if (RenderingOptions.disabled) {
                establish(latch)
            } else {
                establishRendering(latch)
            }
        } catch (exception: Throwable) {
            Log.log(LogMessageType.LOADING, level = LogLevels.FATAL) { exception }
            if (this::assets.isInitialized) {
                assets.unload()
            }
            error = exception
            retry = false
        }
    }

    private fun establish(latch: AbstractLatch?) {
        latch?.dec() // remove initial value
        state = PlaySessionStates.ESTABLISHING
        connection.connect(this)
    }

    private fun establishRendering(latch: AbstractLatch?) {
        val rendering = Rendering(this)
        this.rendering = rendering
        val renderLatch = CallbackLatch(1, latch)
        rendering.start(renderLatch)
        renderLatch.waitIfLess(2)
        renderLatch += latch@{
            if (renderLatch.count > 0) return@latch
            establish(latch)
        }
    }

    override fun terminate() {
        connection.disconnect()
        state = PlaySessionStates.DISCONNECTED
    }

    companion object {
        // TODO: heavy memory leak
        val ACTIVE_CONNECTIONS: MutableSet<PlaySession> = synchronizedSetOf()
        val ERRORED_CONNECTIONS: MutableSet<PlaySession> = synchronizedSetOf()

        fun collectSessions(): Array<PlaySession> {
            val result = ACTIVE_CONNECTIONS.toSynchronizedSet()
            result += ERRORED_CONNECTIONS.toSynchronizedSet()

            return result.toTypedArray()
        }

        private fun cleanupErrors() {
            while (ERRORED_CONNECTIONS.size > 5) {
                // we just keep 5 connections here, they are for crash reports
                ERRORED_CONNECTIONS.iterator().remove()
            }
        }
    }
}

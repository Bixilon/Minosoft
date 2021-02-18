package de.bixilon.minosoft.config.config

import de.bixilon.minosoft.config.config.account.AccountConfig
import de.bixilon.minosoft.config.config.chat.ChatConfig
import de.bixilon.minosoft.config.config.debug.DebugConfig
import de.bixilon.minosoft.config.config.download.DownloadConfig
import de.bixilon.minosoft.config.config.game.GameConfig
import de.bixilon.minosoft.config.config.general.GeneralConfig
import de.bixilon.minosoft.config.config.network.NetworkConfig
import de.bixilon.minosoft.config.config.server.ServerConfig

data class Config(
    val general: GeneralConfig = GeneralConfig(),
    val game: GameConfig = GameConfig(),
    val chat: ChatConfig = ChatConfig(),
    val network: NetworkConfig = NetworkConfig(),
    val account: AccountConfig = AccountConfig(),
    val server: ServerConfig = ServerConfig(),
    val download: DownloadConfig = DownloadConfig(),
    val debug: DebugConfig = DebugConfig(),
)

package de.bixilon.minosoft.gui.eros.main.play.server.type.types

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnectionStates
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.DelegateManager.listDelegate
import de.bixilon.minosoft.util.delegate.watcher.entry.ListDelegateWatcher.Companion.watchListFX
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

object CustomServerType : ServerType {
    override val icon: Ikon = FontAwesomeSolid.SERVER
    override val hidden: Boolean = false
    override val servers: MutableList<Server> by listDelegate(ErosProfileManager.selected.server.entries)
    override val translationKey: ResourceLocation = "minosoft:server_type.custom".toResourceLocation()

    init {
        ErosProfileManager.selected.server::entries.watchListFX(this) {
            while (it.next()) {
                this.servers += it.addedSubList
                this.servers -= it.removed
            }
        }
    }

    override fun refresh(cards: List<ServerCard>) {
        for (serverCard in cards) {
            serverCard.ping?.let {
                if (it.state != StatusConnectionStates.PING_DONE && it.state != StatusConnectionStates.ERROR) {
                    return@let
                }
                it.ping()
            }
        }
    }
}

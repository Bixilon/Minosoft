package de.bixilon.minosoft.gui.eros.main.play.server.type.types

import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.watcher.list.ListDataWatcher.Companion.watchedList
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

object LANServerType : ServerType {
    override val icon: Ikon = FontAwesomeSolid.NETWORK_WIRED
    override val hidden: Boolean
        get() = !LANServerListener.listening
    override var readOnly: Boolean = true
    override val servers: MutableList<Server> by watchedList(synchronizedListOf())
    override val translationKey: ResourceLocation = "minosoft:server_type.lan".toResourceLocation()

    override fun refresh(cards: List<ServerCard>) {
        LANServerListener.clear()
    }
}

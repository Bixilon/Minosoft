package de.bixilon.minosoft.config.config.account

import com.squareup.moshi.Json
import de.bixilon.minosoft.data.accounts.Account
import java.util.*

data class AccountConfig(
    var selected: String = "",
    @Json(name = "client_token")
    var clientToken: String = UUID.randomUUID().toString(),
    val entries: MutableMap<String, Account> = mutableMapOf(),
)

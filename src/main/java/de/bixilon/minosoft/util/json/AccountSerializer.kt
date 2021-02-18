package de.bixilon.minosoft.util.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.MojangAccount
import de.bixilon.minosoft.data.accounts.OfflineAccount

class AccountSerializer {
    @FromJson
    fun fromJson(json: Map<String, Any>): Account {
        return when (json["type"]!!) {
            "mojang" -> MojangAccount.deserialize(json)
            "offline" -> OfflineAccount.deserialize(json)
            "microsoft" -> MicrosoftAccount.deserialize(json)
            else -> throw IllegalArgumentException("Invalid account type: ${json["type"]}")
        }
    }

    @ToJson
    fun toJson(account: Account): Map<String, Any> {
        return account.serialize()
    }
}

package com.selfformat.goldpare.shared.database

import com.selfformat.goldpare.shared.cache.AppDatabase
import com.selfformat.goldpare.shared.models.APIXauPln
import com.selfformat.goldpare.shared.models.XauPln

internal class XauPlnDatabase(databaseDriverFactory: DatabaseDriverFactory) {
    private val db = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = db.appDatabaseQueries

    fun clearPlnXauDatabase() {
        dbQuery.transaction {
            dbQuery.removeXauPln()
        }
    }

    fun createGoldXau(it: APIXauPln) {
        dbQuery.transaction {
            insertXauPln(it)
        }
    }

    private fun insertXauPln(it: APIXauPln) {
        dbQuery.insertXauPln(
            timestamp = it.timestamp,
            metal = it.metal,
            currency = it.currency,
            exchange = it.exchange,
            symbol = it.symbol,
            openTime = it.openTime,
            price = it.price,
            ch = it.ch,
            ask = it.ask,
            bid = it.bid
        )
    }

    fun getXauPln(): List<XauPln> {
        return dbQuery.selectAllXauPln().executeAsList().map {
            XauPln(
                id = it.id,
                timestamp = it.timestamp,
                metal = it.metal,
                currency = it.currency,
                exchange = it.exchange,
                symbol = it.symbol,
                openTime = it.openTime,
                price = it.price,
                ch = it.ch,
                ask = it.ask,
                bid = it.bid
            )
        }
    }
}

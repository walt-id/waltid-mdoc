package id.walt.db

import id.walt.db.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.bridge.SLF4JBridgeHandler
import java.sql.Connection

object Db {

    fun connect() {
        Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    fun start() {
        connect()

        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.drop(Users)
            SchemaUtils.create(Users)
        }

        transaction {
            addLogger(StdOutSqlLogger)

            val user = Users.insertAndGetId {
                it[email] = "string@string.string"
                it[password] = "string"
            }

            println("Inserted: ${user.value}")
        }
    }
}

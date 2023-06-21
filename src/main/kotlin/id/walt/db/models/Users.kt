package id.walt.db.models

import org.jetbrains.exposed.dao.id.UUIDTable

object Users : UUIDTable() {
    val email = varchar("email", 128).uniqueIndex()
    val password = varchar("password", 200)
}

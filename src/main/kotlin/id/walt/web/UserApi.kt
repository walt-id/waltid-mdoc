package id.walt.web

import id.walt.db.models.Users
import id.walt.db.models.Users.email
import id.walt.db.models.Users.password
import io.github.smiley4.ktorswaggerui.dsl.delete
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.put
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@Serializable
data class UserData(val email: String, val password: String, val id:String? = null)

object UserApi {

    private fun Application.walletRoute(build: Route.() -> Unit) {
        routing {
         //   authenticate("authenticated") {
                route("api/xyz", {
                    tags = listOf("user")
                }) {
                    build.invoke(this)
                }
            }
        //}
    }

    fun Application.helloApi() = walletRoute {
        route("user") {
            get({
                summary = "List users"
                response {
                    HttpStatusCode.OK to {
                        description = "Array of users"
                        body<List<JsonObject>>()
                    }
                }
            }) {
                val usersResp = transaction {
                    Users.selectAll().map { UserData(it[email], it[password],it[Users.id].toString()) }
                }
                context.respond(usersResp)
            }

            route("{id}") {
                get({
                    summary = "Load a user"
                    request {
                        pathParameter<String>("id") {
                            description = "The userId"
                        }
                    }
                }) {
                    val id = UUID.fromString(context.parameters.get("id"))
                    val user = transaction {
                        val row = Users.select { Users.id eq id }.single()
                        UserData(row[email], row[password], row[Users.id].toString())
                    }
                    context.respond(user)
                }
            }

            put({
                summary = "Store user"
                description = "Store a user by defining email + password"
                request {
                    body<UserData> {
                        example("example", UserData("user@example.com", "password"))
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "In case of success"
                    }
                }
            }) {
                val userRequest = call.receive<UserData>()
                transaction {
                    Users.insertAndGetId {
                        it[email] = userRequest.email
                        it[password] = userRequest.password
                    }
                }
                call.respond(HttpStatusCode.OK)
            }

            route("{id}") {
                delete({
                    summary = "Delete a user"
                    description = "Delete a user by ID"
                    request {
                        pathParameter<String>("id") {
                            description = "The userId"
                        }
                    }
                }) {
                    val id = UUID.fromString(context.parameters.get("id"))
                    transaction {
                        Users.deleteWhere { Users.id eq id }
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}

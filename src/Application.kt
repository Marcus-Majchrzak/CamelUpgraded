import classes.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.network.sockets.*
import java.time.*
import java.util.*
import kotlin.collections.LinkedHashSet
import com.beust.klaxon.Klaxon

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }

        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/ws/camelupgraded") {
            val Game = Game(5)
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                send("""
                     {
                        "action": "init",
                        "data": {
                            "id": "${thisConnection.name}"
                         }
                     } 
                     """.trimIndent()
                )
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()

                    //DEBUGGING
                    val textWithUsername = "[${thisConnection.name}]: $receivedText"
                    println(textWithUsername)

                    println(receivedText)
                    val response = Klaxon().parse<Action>(receivedText)
                    if (response != null) {
                        when(response.action) {
                            "move" -> Game.moveAction((response as ActionMove).id.toInt())
                            "leg-bet" -> {
                                val action = response as ActionLegBet
                                val camel = stringToCamel(action.camel) ?: throw IllegalArgumentException("Invalid Camel Type")
                                Game.legBetAction(action.id.toInt(), camel)
                            }
                            "race-bet" -> {
                                val action = response as ActionRaceBet
                                val camel = stringToCamel(action.camel) ?: throw IllegalArgumentException("Invalid Camel Type")
                                val betType = stringToRaceBetType(action.betType) ?: throw IllegalArgumentException("Invalid Race Bet Type")
                                Game.raceBetAction(action.id.toInt(), camel, betType)
                            }
                            "place-tile" -> {
                                val action = response as ActionPlaceTile
                                val tileType = stringToTileType(action.tileType) ?: throw IllegalArgumentException("Invalid Tile Type")
                                Game.tileAction(action.id.toInt(), action.space, tileType)
                            }
                        }
                    }
                    else {
                        //CHECK FOR ERRORS
                    }
                    val gameState = Game.getGameState()
                    val json = """
                        {
                            "players": ${gameState.Players},
                            "boardState" : ${gameState.boardState}
                        }
                    """.trimIndent()
                    println(json)
                    send(json)


                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }
        }
    }
}


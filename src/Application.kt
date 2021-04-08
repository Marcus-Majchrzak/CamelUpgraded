import classes.Action
import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.slf4j.event.Level
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

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

    install(WebSockets) {
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
        val gameAdapter = GameAdapter()
        webSocket("/ws/camelupgraded") {

            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                gameAdapter.addPlayer(thisConnection.getPlayerNo());
                val initialResponse =
                    gameAdapter.getInitiateMessage(thisConnection.getSecret(), thisConnection.getPlayerNo())
                println(initialResponse)
                send(initialResponse)
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()

                    //DEBUGGING
                    val textWithUsername = "[${thisConnection.getPlayerNo()}]: $receivedText"
                    println(textWithUsername)
                    println(receivedText)

                    val response = Klaxon().parse<Action>(receivedText)
                    val updateMessages: ArrayList<String> = arrayListOf()
                    synchronized(thisConnection) {
                        gameAdapter.parseResponse(response, thisConnection.getSecret(), thisConnection.getPlayerNo())
                        connections.forEach { c ->
                            updateMessages.add(gameAdapter.getUpdateMessage(c.getPlayerNo()))
                        }
                    }
                    connections.forEachIndexed { i, c ->
                        c.session.send(updateMessages[i])
                    }

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

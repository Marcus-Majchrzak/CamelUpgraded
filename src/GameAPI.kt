package classes

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.util.*


class GameAPI {
    private lateinit var _game: Game
//    private _playerWS: WebSocket = Websocket()
    fun setGame(game: Game) {
        _game = game
    }
    @Suppress("unused")
    fun Application.module() {
        install(WebSockets)
        routing {
            webSocket("/chat") {
                send("You are connected!")
                for(frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    send("You said: $receivedText")
                }
            }
        }
    }
}


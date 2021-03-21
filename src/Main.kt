package classes

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket

class Main {
    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            val game = Game(5);
            val api  = GameAPI()
            game.setAPI(api)
            api.setGame(game)
            io.ktor.server.netty.EngineMain.main(args)
        }
    }
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


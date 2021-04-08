import io.ktor.http.cio.websocket.*
import java.util.concurrent.atomic.*
import kotlin.random.Random

class Connection(val session: DefaultWebSocketSession) {
    companion object {
        var lastNum = AtomicInteger(0)

        //Maps secret -> playerNo
        var secrets: MutableSet<String> = mutableSetOf()
    }

    private val playerNo = lastNum.getAndIncrement()
    private val secret = newSecret()

    fun getPlayerNo(): Int {
        return playerNo
    }

    fun getSecret(): String {
        return secret
    }

    @Synchronized
    private fun newSecret(): String {
        var newSecret = generateSecret()
        var secretInUse = secrets.contains(newSecret)
        while (secretInUse) {
            newSecret = generateSecret()
            secretInUse = secrets.contains(newSecret)
        }
        secrets.add(newSecret)
        return newSecret
    }

    private fun generateSecret(): String {
        val charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val secretLength = 32
        return (1..secretLength).map { Random.nextInt(0, charSet.length) }.map(charSet::get).joinToString("")
    }
}
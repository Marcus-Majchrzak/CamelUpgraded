package classes

import com.beust.klaxon.JsonObject
import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import kotlin.reflect.KClass

enum class Camels {
    BLUE,
    RED,
    GREEN,
    ORANGE,
    WHITE
}
fun stringToCamel(camel: String): Camels? {
    return when(camel.toLowerCase()){
        "blue" -> Camels.BLUE
        "red" -> Camels.RED
        "green" -> Camels.GREEN
        "orange" -> Camels.ORANGE
        "white" -> Camels.WHITE
        else -> null
    }
}




enum class RaceBetTypes {
    WINNER,
    LOSER
}
fun stringToRaceBetType(type: String): RaceBetTypes? {
    return when(type.toLowerCase()){
        "winner" -> RaceBetTypes.WINNER
        "loser" -> RaceBetTypes.LOSER
        else -> null
    }
}
enum class TileTypes {
    OASIS,
    MIRAGE
}
fun stringToTileType(type: String): TileTypes? {
    return when(type.toLowerCase()){
        "oasis" -> TileTypes.OASIS
        "mirage" -> TileTypes.MIRAGE
        else -> null
    }
}
data class DesertTile(val type: TileTypes, val playerId: Int)
data class RaceBet(val camel: Camels, val playerId: Int)
data class LegBet(val camel: Camels, val value: Int)
data class DiceRoll(val camel: Camels, val move: Int)
data class WinningCamels(val winner: Camels, val runnerUp: Camels)

data class GameState(
    val Players: String,
    val boardState: String
)
data class BoardState(
    val CamelPositions: String,
    val LegBids: String
)

@TypeFor(field = "action", adapter = ActionTypeAdapter::class)
open class Action(val action: String)
data class ActionMove(val id: String): Action("move")
data class ActionLegBet(val id: String, val camel: String): Action("leg-bet")
data class ActionRaceBet(val id: String, val camel: String, val betType: String): Action("race-bet")
data class ActionPlaceTile(val id: String, val space: Int, val tileType: String): Action("place-tile")

class ActionTypeAdapter: TypeAdapter<Action> {
    override fun classFor(action: Any): KClass<out Action> = when(action as String) {
        "move" -> ActionMove::class
        "leg-bet" -> ActionLegBet::class
        "race-bet" -> ActionRaceBet::class
        "place-tile" -> ActionPlaceTile::class
        else -> throw IllegalArgumentException("Unknown type: $action")
    }
}
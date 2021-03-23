import classes.*

class GameAdapter {
    private val _game = Game(10)

    fun getInitiateMessage(id: String): String {
        return """
             {
                "action": "init",
                "id": "$id",
                "data": $_game
             } 
             """.trimIndent()
    }

    fun getUpdateMessage(id: String): String {
        return """
             {
                "action": "update",
                "data": $_game
             } 
             """.trimIndent()
    }

    fun parseResponse(response: Action?) {
        if (response != null) {
            when (response.action) {
                "move" -> _game.moveAction((response as ActionMove).id.toInt())
                "leg-bet" -> {
                    val action = response as ActionLegBet
                    val camel = stringToCamel(action.camel) ?: throw IllegalArgumentException("Invalid Camel Type")
                    _game.legBetAction(action.id.toInt(), camel)
                }
                "race-bet" -> {
                    val action = response as ActionRaceBet
                    val camel = stringToCamel(action.camel) ?: throw IllegalArgumentException("Invalid Camel Type")
                    val betType =
                        stringToRaceBetType(action.betType) ?: throw IllegalArgumentException("Invalid Race Bet Type")
                    _game.raceBetAction(action.id.toInt(), camel, betType)
                }
                "place-tile" -> {
                    val action = response as ActionPlaceTile
                    val tileType =
                        stringToTileType(action.tileType) ?: throw IllegalArgumentException("Invalid Tile Type")
                    _game.tileAction(action.id.toInt(), action.space, tileType)
                }
            }
        } else {
            //CHECK FOR ERRORS
        }
    }
}
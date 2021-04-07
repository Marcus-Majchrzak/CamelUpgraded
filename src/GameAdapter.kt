import classes.*

class GameAdapter {
    private val _game = Game(0)

    fun getInitiateMessage(secret: String, playerNo: Int): String {
        return """
             {
                "action": "init",
                "id": "$secret",
                "data": ${_game.getJson(playerNo)}
             } 
             """.trimIndent()
    }

    fun getUpdateMessage(playerNo: Int): String {
        return """
             {
                "action": "update",
                "data": ${_game.getJson(playerNo)}
             } 
             """.trimIndent()
    }

    fun parseResponse(response: Action?, serverSecret: String, playerNo: Int) {
        if (response != null ) {
            val userSecret = getUserSecret(response) ?: throw IllegalArgumentException("Invalid Action")
            if(userSecret == serverSecret){
                parseAction(response, playerNo)
            }
            else {
                throw IllegalArgumentException("Invalid Secret")
            }
        } else {
            throw IllegalArgumentException("Invalid Request")
        }
    }
    private fun parseAction(response: Action, id: Int){
        when (response.action) {
            "move" -> _game.moveAction(id)
            "leg-bet" -> {
                val action = response as ActionLegBet
                val camel = stringToCamel(action.camel) ?: throw IllegalArgumentException("Invalid Camel Type")
                _game.legBetAction(id, camel)
            }
            "race-bet" -> {
                val action = response as ActionRaceBet
                val camel = stringToCamel(action.camel) ?: throw IllegalArgumentException("Invalid Camel Type")
                val betType =
                    stringToRaceBetType(action.betType) ?: throw IllegalArgumentException("Invalid Race Bet Type")
                _game.raceBetAction(id, camel, betType)
            }
            "place-tile" -> {
                val action = response as ActionPlaceTile
                val tileType =
                    stringToTileType(action.tileType) ?: throw IllegalArgumentException("Invalid Tile Type")
                _game.tileAction(id, action.space, tileType)
            }
            else -> {
                throw IllegalArgumentException("Invalid Action")
            }
        }
    }
    private fun getUserSecret(response: Action): String? {
        return when (response.action) {
            "move" -> (response as ActionMove).id
            "leg-bet" -> (response as ActionLegBet).id
            "race-bet" -> (response as ActionRaceBet).id
            "place-tile" -> (response as ActionPlaceTile).id
            else -> null
        }
    }
    fun addPlayer(id: Int){
        _game.addPlayer(id)
    }
}
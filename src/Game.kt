package classes

class Game(maxPlayers: Int) {
    private val _maxPlayers = maxPlayers;
    private val _board = GameBoard()
    private var _players: ArrayList<Player> = arrayListOf()

    fun addPlayer(id: Int) {
        if (_players.size < this._maxPlayers) {
            _players.add(Player(id))
        } else {
            //TODO error for max players
        }
    }

    fun startGame() {
        //TODO
    }

    fun moveAction(id: Int) {
        _board.moveAction()
        _players[id].changeMoney(1)
        if (_board.raceOver()) {
            raceOver()
        } else if (_board.isLegOver()) {
            legOver()
        }
        _board.printBoard()
    }

    fun legBetAction(id: Int, camel: Camels) {
        val bet = _board.takeBet(camel)
        if (bet != null) {
            _players[id].addBet(bet)
        } else {
            //TODO add error for no leg bets
        }
    }

    fun raceBetAction(id: Int, camel: Camels, t: RaceBetTypes) {
        _board.addRaceBet(RaceBet(camel, id), t)
    }

    fun tileAction(id: Int, space: Int, type: TileTypes) {
        _board.tileAction(space, DesertTile(type, id))
    }

    private fun legOver() {
        val ranking = _board.getRankings()
        val winners = WinningCamels(ranking.last(), ranking[ranking.size - 2])
        _players.forEach { it.computeBids(winners) }
        _board.legReset()
    }

    private fun raceOver() {
        legOver()
        val ranking = _board.getRankings()
        calculateOverallBets(_board.getLoserBets(), ranking.first())
        calculateOverallBets(_board.getWinnerBets(), ranking.last())
    }

    private fun calculateOverallBets(bets: ArrayDeque<RaceBet>, camel: Camels) {
        var numCorrect = 0
        bets.forEach { bet ->
            if (bet.camel == camel) {
                if (numCorrect < raceBetValues.size) {
                    _players[bet.playerId].changeMoney(raceBetValues[numCorrect])
                    numCorrect++
                } else {
                    _players[bet.playerId].changeMoney(1)
                }
            } else {
                _players[bet.playerId].changeMoney(-1)
            }
        }
    }

    fun getJson(playerNo: Int): String {
        var othersStr = _players
            .filterIndexed { i, _ -> i != playerNo }
            .map { it.toString() }.reduce { a, b -> "$a,$b" }
        return """
            {
            "me": ${_players[playerNo]}
            "otherPlayers": [${othersStr}],
            "boardState" : $_board
            }
            """.trimIndent()
    }
}

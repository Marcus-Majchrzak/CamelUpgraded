package classes

class Game(maxPlayers: Int) {
    private val _maxPlayers = maxPlayers
    private val _board = GameBoard(this)
    private var _players: ArrayList<Player> = arrayListOf()
    private var _playerTurn: Int = 0

    fun addPlayer(id: Int) {
        if (_players.size < this._maxPlayers) {
            println("MEMES")
            _players.add(Player(id))
        } else {
            //TODO error for max players
        }
    }

    fun startGame() {
        //TODO
    }

    fun changePlayerMoney(id: Int, money: Int) {
        _players[id].changeMoney(money)
    }

    fun moveAction(id: Int) {
        if (_playerTurn != id) return
        _board.moveAction()
        changePlayerMoney(id, 1)
        if (_board.raceOver()) {
            raceOver()
        } else if (_board.isLegOver()) {
            legOver()
        }
        _board.printBoard()
        incrementTurn()
    }

    fun legBetAction(id: Int, camel: Camels) {
        if (_playerTurn != id) return
        val bet = _board.takeBet(camel)
        if (bet != null) {
            _players[id].addBet(bet)
        } else {
            //TODO add error for no leg bets
        }
        incrementTurn()
    }

    fun raceBetAction(id: Int, camel: Camels, t: RaceBetTypes) {
        if (_playerTurn != id) return
        _board.addRaceBet(RaceBet(camel, id), t)
        incrementTurn()
    }

    fun tileAction(id: Int, space: Int, type: TileEffectTypes) {
        if (_playerTurn != id) return
        _board.tileAction(space, DesertTile(type, id))
        incrementTurn()
    }

    private fun incrementTurn() {
        _playerTurn = (_playerTurn + 1) % _players.size
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
        println("This is the gameboard state: ${_board}")
        val playerStr =
            _players
                .map { it.toString() }
                .reduce { a, b -> "$a,$b" }

        return """
            {
            "me": ${playerNo},
            "players": [${playerStr}],
            "boardState" : $_board,
            "playerTurn" : $_playerTurn
            }
            """.trimIndent()
    }
}

package classes

class Game(numOfPlayers: Int) {
    private lateinit var _gameAPI:GameAPI;
    private val _board = GameBoard()
    private var _players : Array<Player> = Array(numOfPlayers) {Player(it.toString(), null)}

    fun setAPI(api: GameAPI){
        _gameAPI = api
    }

    fun playGame(){
        legBetAction(1, Camels.BLUE)
        moveAction(1)
        moveAction(1)
        moveAction(1)
        moveAction(1)
        moveAction(1)
        println(_players[1])

    }
    fun moveAction(id : Int) {
        println("DID MOVE ACTION")
        _board.moveAction()
        _players[id].changeMoney(1)
        if(_board.raceOver()){
            raceOver()
        }
        else if(_board.isLegOver()){
            legOver()
        }
        println("Current Winners:" + _board.getRankings())
        _board.printBoard()
    }
    fun legBetAction(id : Int, camel: Camels) {
        println("DID LEG BET")
        val bet = _board.takeBet(camel)
        if(bet != null){
            _players[id].addBet(bet)
        }
        else {
            //TODO add error reporting
            moveAction(id)
        }
    }
    fun raceBetAction(id : Int, camel: Camels, t: RaceBetTypes) {
        println("DID RACE BET")
        _board.addRaceBet(RaceBet(camel, id), t)
    }
    fun tileAction(id : Int, space: Int, type: TileTypes) {
        println("DID TILE ACTION")
        _board.tileAction(space, DesertTile(type, id))
    }
    private fun legOver(){
        val ranking = _board.getRankings()
        val winners = WinningCamels(ranking.last(), ranking[ranking.size - 2])
        _players.forEach { it.computeBids(winners) }
        _board.legReset()
    }

    private fun raceOver(){
        legOver()
        val ranking = _board.getRankings()
        calculateOverallBets( _board.getLoserBets(), ranking.first())
        calculateOverallBets( _board.getWinnerBets(), ranking.last())

    }
    private fun calculateOverallBets(bets: ArrayDeque<RaceBet>, camel: Camels){
        var numCorrect = 0
        bets.forEach { bet ->
            if (bet.camel == camel) {
                if(numCorrect < raceBetValues.size){
                    _players[bet.playerId].changeMoney(raceBetValues[numCorrect])
                    numCorrect++
                }
                else{
                    _players[bet.playerId].changeMoney(1)
                }
            }
            else {
                _players[bet.playerId].changeMoney(-1)
            }
        }
    }
    override fun toString(): String {
        var playersStr = _players.map { it.toString() }.reduce{a, b -> "$a,$b"}
        return """
            {
            "players": {${playersStr}},
            "boardState" : $_board
            }
            """.trimIndent()
    }
}

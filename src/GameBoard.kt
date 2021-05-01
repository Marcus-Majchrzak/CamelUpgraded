package classes

import com.beust.klaxon.Klaxon
import kotlin.collections.set


class GameBoard {
    private val _pyramid = DicePyramid()
    private var _board: MutableList<ArrayDeque<Camels>> = MutableList(numberOfSpaces) { ArrayDeque() }
    private var _placedTiles: MutableMap<Int, DesertTile> = mutableMapOf()
    private var _camelLocation: MutableMap<Camels, Int> = Camels.values().associateBy({ it }, { 0 }).toMutableMap()
    private var _camelRankings: ArrayDeque<Camels> = ArrayDeque(Camels.values().toList())
    private var _camelLegBets: Map<Camels, ArrayDeque<LegBet>> =
        Camels.values().associateBy({ it }, { ArrayDeque() })
    private var _loserBets: ArrayDeque<RaceBet> = ArrayDeque()
    private var _winnerBets: ArrayDeque<RaceBet> = ArrayDeque()

    init {
        Camels.values().forEach { camel ->
            _board[0].addFirst(camel)
            legBetValues.forEach { bid -> _camelLegBets[camel]?.addFirst(LegBet(camel, bid)) }
        }
    }

    //TODO: Rewrite to not be bad
    private fun moveCamel(dice: DiceRoll) {
        val moveQueue = ArrayDeque<Camels>()
        val square = _camelLocation[dice.camel] ?: error("Could not find Camel in Map")

        //Move Camels off
        var popCamel = _board[square].removeLast()
        while (popCamel != dice.camel) {
            moveQueue.addFirst(popCamel)
            popCamel = _board[square].removeLast()
        }
        moveQueue.addFirst(popCamel)

        //Put Camels back on
        val squareToMoveTo = minOf(numberOfSpaces - 1, withDesertTileMovement(square + dice.move))
        while (!moveQueue.isEmpty()) {
            popCamel = moveQueue.removeFirst()
            _board[squareToMoveTo].addLast(popCamel)
            _camelLocation[popCamel] = squareToMoveTo
        }
    }

    private fun withDesertTileMovement(space: Int): Int {
        return when (_placedTiles[space]?.effect) {
            TileEffectTypes.MIRAGE -> space - 1
            TileEffectTypes.OASIS -> space + 1
            else -> space
        }
    }

    fun moveAction() {
        if (!isLegOver()) {
            moveCamel(_pyramid.rollDice())
            //TODO: Add lazy evaluation
            updateRankings()
        } else {
            //TODO FLOW IF CAN'T ROLL - should never happen
        }
    }

    fun tileAction(space: Int, tile: DesertTile) {
        _placedTiles = _placedTiles.filter { it.value.playerId != tile.playerId } as MutableMap<Int, DesertTile>
        if (_placedTiles[space + 1] == null && _placedTiles[space - 1] == null && space >= 1 && space < numberOfSpaces - 2) {
            _placedTiles[space] = tile
        }
    }

    private fun updateRankings() {
        _camelRankings = ArrayDeque()
        _board.forEach {
            _camelRankings.addAll(it)
        }
    }

    fun getRankings(): ArrayDeque<Camels> {
        return _camelRankings
    }

    fun printBoard() {
        println(_board)
    }

    fun isLegOver(): Boolean {
        return _pyramid.diceRolled().size >= rollsInLeg
    }

    fun legReset() {
        _camelLegBets = Camels.values().associateBy({ it }, { ArrayDeque() })
        Camels.values().forEach { camel ->
            legBetValues.forEach { bid -> _camelLegBets[camel]?.addFirst(LegBet(camel, bid)) }
        }
        _pyramid.resetPyramid()
    }

    fun raceOver(): Boolean {
        //TODO replace how we find out who are winners
        return _camelLocation.maxByOrNull { it.value }?.value == numberOfSpaces - 1
    }

    fun takeBet(camel: Camels): LegBet? {
        return _camelLegBets[camel]?.removeLast()
    }

    fun addRaceBet(bet: RaceBet, t: RaceBetTypes) {
        if (!_winnerBets.contains(bet) && !_loserBets.contains(bet)) {
            when (t) {
                RaceBetTypes.WINNER -> _winnerBets.addLast(bet)
                RaceBetTypes.LOSER -> _loserBets.addLast(bet)
            }
        }
    }

    fun getWinnerBets(): ArrayDeque<RaceBet> {
        return _winnerBets
    }

    fun getLoserBets(): ArrayDeque<RaceBet> {
        return _loserBets
    }

    override fun toString(): String {
        //TODO MAKE NOT SPAGHETTI

        //Stringify CamelLocation
        val stringLocation = _board.map { space ->
            space.map {
                "\"${it.toString().toLowerCase()}\""
            }.toString()
        }.toString()

        //Stringify placedTiles

        val placedTilesStr = Klaxon().toJsonString(_placedTiles)

        //Stringify Leg Bet
        val stringLegBet = _camelLegBets.map { bets ->
            "\"${bets.key.toString().toLowerCase()}\":" +
                    bets.value.map { bet ->
                        bet.value
                    }.toString()
        }.reduce { a, b -> "$a,$b" }

        //Stringify diceRolled
        val diceRolled = if (_pyramid.diceRolled().isNotEmpty()) {
            _pyramid.diceRolled().map { roll ->
                """{
                "color": "${roll.camel.toString().toLowerCase()}",
                "value": ${roll.move}
                }
            """.trimMargin()
            }.reduce { a, b -> "$a,$b" }
        } else ""

        return """
                        {
                            "camelPositions": ${stringLocation},
                            "legBids" : {${stringLegBet}},
                            "diceRolled" : [$diceRolled],
                            "placedTiles" : $placedTilesStr
                        }
                    """.trimIndent()
    }
}
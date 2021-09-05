package classes

import com.beust.klaxon.Klaxon

class Player(id: Int) {
    private var _name: String = "Player $id"
    private var _money: Int = startingCash
    private var _legBets: MutableList<LegBet> = arrayListOf()

    fun changeMoney(income: Int) {
        _money += income
    }

    fun addBet(bet: LegBet) {
        _legBets.add(bet)
    }

    fun computeBids(winners: WinningCamels) {
        _legBets.forEach { bid ->
            when (bid.camel) {
                winners.winner -> changeMoney(bid.value)
                winners.runnerUp -> changeMoney(1)
                else -> changeMoney(-1)
            }
        }
        _legBets = arrayListOf()
    }

    override fun toString(): String {
        return """
            {
            "name": "$_name",
            "money": $_money,
            "legBets": ${Klaxon().toJsonString(_legBets)}
            }""".trimIndent()
    }
}
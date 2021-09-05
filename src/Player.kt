package classes

import com.beust.klaxon.Klaxon

class Player(id: Int) {
    private var _name: String = "Player $id"
    private var _money: Int = startingCash
    private var _legBets: MutableList<LegBet> = arrayListOf()
    private var _incomeTable: MutableMap<IncomeReason, Int> =
        IncomeReason.values().associateBy({ it }, { 0 }).toMutableMap()

    fun changeMoney(income: Int, reason: IncomeReason) {
        _money += income
        if (_incomeTable[reason] != null)
            _incomeTable[reason] = _incomeTable[reason]!! + (income)
        println(_incomeTable)
    }

    fun addBet(bet: LegBet) {
        _legBets.add(bet)
    }

    fun computeBids(winners: WinningCamels) {
        _legBets.forEach { bid ->
            when (bid.camel) {
                winners.winner -> changeMoney(bid.value, IncomeReason.LEGBET)
                winners.runnerUp -> changeMoney(1, IncomeReason.LEGBET)
                else -> changeMoney(-1, IncomeReason.LEGBET)
            }
        }
        _legBets = arrayListOf()
    }

    override fun toString(): String {
        return """
            {
            "name": "$_name",
            "money": $_money,
            "legBets": ${Klaxon().toJsonString(_legBets)},
            "incomeTable": ${Klaxon().toJsonString(_incomeTable)}
            }""".trimIndent()
    }
}
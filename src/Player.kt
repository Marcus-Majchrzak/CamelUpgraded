package classes

class Player(id: String, name: String?) {
    private val _id : String = id
    private var _name: String = name ?: "Player_$id"
    private var _cash: Int = startingCash
    private var _legBets: MutableList<LegBet> = arrayListOf()

    fun changeMoney(income: Int) {
        _cash += income
    }
    fun addBet(bet: LegBet) {
        _legBets.add(bet)
    }
    fun computeBids(winners: WinningCamels) {
        _legBets.forEach{ bid ->
            when(bid.camel){
                winners.winner   -> changeMoney(bid.value)
                winners.runnerUp -> changeMoney(1)
                else             -> changeMoney(-1)
            }
        }
        _legBets = arrayListOf()
    }
    override fun toString(): String {
        return  "\"$_name\": {\n" +
                "\"money\": $_cash\n" +
                //TODO ADD LEG BETSs
                "}\n"
    }
}
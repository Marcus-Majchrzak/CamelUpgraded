package classes

import kotlin.random.Random

class DicePyramid {
    private var diceIn: MutableList<Camels> = Camels.values().toMutableList()

    fun rollDice() : DiceRoll {
        val color = diceIn.random()
        diceIn.remove(color)

        val number = Random.nextInt(1,4)

        println("$color moved $number squares!")
        return DiceRoll(color, number)
    }
    fun resetPyramid() {
        diceIn = Camels.values().toMutableList()
    }
    fun diceLeft():List<Camels> {
        return diceIn.toList()
    }
    fun diceRolled():List<Camels> {
        return Camels.values().filterNot {it in diceIn}
    }
}
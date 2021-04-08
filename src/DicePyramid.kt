package classes

import kotlin.random.Random

class DicePyramid {
    private var diceIn: MutableList<Camels> = Camels.values().toMutableList()
    private var diceOut: MutableList<DiceRoll> = mutableListOf()

    fun rollDice(): DiceRoll {
        val color = diceIn.random()
        diceIn.remove(color)

        val number = Random.nextInt(1, 4)

        println("$color moved $number squares!")
        diceOut.add(DiceRoll(color, number))

        return DiceRoll(color, number)
    }

    fun resetPyramid() {
        diceIn = Camels.values().toMutableList()
        diceOut = mutableListOf()
    }

    fun diceRolled(): List<DiceRoll> {
        return diceOut.toList()
    }
}
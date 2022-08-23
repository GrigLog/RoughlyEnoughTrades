package griglog.relt.utils

import java.util.*

val map = TreeMap<Int, String>().apply {
    put(1000, "M")
    put(900, "CM")
    put(500, "D")
    put(400, "CD")
    put(100, "C")
    put(90, "XC")
    put(50, "L")
    put(40, "XL")
    put(10, "X")
    put(9, "IX")
    put(5, "V")
    put(4, "IV")
    put(1, "I")
}

fun toRoman(number: Int): String {
    val (mapNumber, str) = map.floorEntry(number) ?: return "?"
    return if (number == mapNumber) str
    else str + toRoman(number - mapNumber)
}
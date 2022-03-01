package converter
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

fun main() {
    do {
        print("Enter two numbers in format: {source base} {target base} (To quit type /exit) ")
        val input = readln()
        if (input == "/exit") break
        val (sourceBase, targetBase) = input.split(" ").map { it.toBigInteger() }
        do {
            print("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back) ")
            val inputNumber = readln()
            if (inputNumber == "/back") break
            val decimalNumber = convertToDecimal(inputNumber, sourceBase)
            val convertedNumber = convertFromDecimal(decimalNumber, targetBase)
            println("Conversion result: $convertedNumber")
            println()
        } while (true)
        println()
    } while (true)
}

private fun convertFromDecimal(inputNumber: BigDecimal, targetBase: BigInteger): String {
    var decimalNumber = inputNumber.toBigInteger()
    var numberConverted = ""
    var fractionConverted = ""

    if (inputNumber.toString().contains(".")) {
        val (integerPart, fractionalPart) = inputNumber.integerAndFractionalParts()
        decimalNumber = integerPart.toBigInteger()
        fractionConverted = getFractionConverted(fractionalPart, targetBase.toBigDecimal())
    }
    do {
        val remainder = decimalNumber % targetBase
        val result = decimalNumber / targetBase
        decimalNumber = result
        val remainderValue = getCharLetter(remainder.toInt())
        numberConverted += remainderValue
    } while (decimalNumber != BigInteger.ZERO)

    return if (fractionConverted.isEmpty()) {
        numberConverted.reversed()
    } else {
        "${numberConverted.reversed()}.$fractionConverted"
    }
}

fun getFractionConverted(fractionPiece: String, targetBase: BigDecimal): String {
    var fractionConverted = ""
    var remainingValue = "0.$fractionPiece".toBigDecimal()
    do {
        val result = (remainingValue * targetBase).setScale(5, RoundingMode.HALF_DOWN)
        val (integerPart, fractionalPart) = result.integerAndFractionalParts()
        remainingValue = "0.$fractionalPart".toBigDecimal()
        fractionConverted += getCharLetter(integerPart.toInt())
    } while (remainingValue != BigDecimal("0.0") && fractionConverted.length < 5)

    return fractionConverted
}

private fun convertToDecimal(inputNumber: String, sourceBase: BigInteger): BigDecimal {
    var integerPart = inputNumber
    var fractionalPart = ""

    if (inputNumber.contains(".")) {
        integerPart = inputNumber.substringBefore(".")
        fractionalPart = inputNumber.substringAfter(".")
    }

    var numberBase10 = BigInteger.ZERO
    for (i in integerPart.indices) {
        val digitValue = getCharNumber(integerPart[i]).toBigInteger()

        numberBase10 *= sourceBase
        numberBase10 += digitValue
    }

    if (fractionalPart.isEmpty()) return numberBase10.toBigDecimal()

    var result = BigDecimal.ZERO
    for (i in fractionalPart.lastIndex downTo 0) {
        val digitValue = getCharNumber(fractionalPart[i]).toBigDecimal()
        val sum = digitValue + result
        val fractionBase10 = sum.divide(sourceBase.toBigDecimal(), MathContext.DECIMAL128)
        result = fractionBase10
    }
    val fractionBase10AfterDot = result.toString().substringAfter(".")
    return "$numberBase10.$fractionBase10AfterDot".toBigDecimal()
}

private fun getCharLetter(remainder: Int): String =
    if (remainder in 10..35) (remainder - 10 + 'A'.code).toChar().toString() else remainder.toString()

private fun getCharNumber(charLetter: Char): Int =
    if (charLetter.isLetter()) (charLetter.lowercaseChar().code - 'a'.code) + 10 else charLetter.digitToInt()

private fun BigDecimal.integerAndFractionalParts() : Pair<String, String> {
    val parts = this.toString().split('.')
    return parts[0] to parts[1]
}
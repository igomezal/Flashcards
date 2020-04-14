package flashcards

import java.io.File
import java.util.*
import kotlin.random.Random

val scanner = Scanner(System.`in`)
val cards = mutableMapOf<String, String>()
val mistakesCounter = mutableMapOf<String, Int>()
val log = mutableListOf<String>()

fun printlnAndStoreLog(s: String) {
    log.add(s)
    println(s)
}

fun scanAndStoreLog(): String {
    val nextLine = scanner.nextLine()
    log.add(nextLine)
    return nextLine
}

fun main(args: Array<String>) {
    var importFileName = -1
    var exportFileName = -1
    if (args.isNotEmpty()) {
        importFileName = args.indexOf("-import")
        exportFileName = args.indexOf("-export")
        if (importFileName >= 0) {
            importFileName++
            importFile(args[importFileName])
        }

        if (exportFileName >= 0) {
            exportFileName++
        }
    }

    printlnAndStoreLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
    var action = scanAndStoreLog()

    while(action != "exit") {

        when(action) {
            "add" -> add()
            "remove" -> remove()
            "import" -> {
                val fileName = askForFile()
                importFile(fileName)
            }
            "export" -> {
                val fileName = askForFile()
                exportFile(fileName)
            }
            "ask" -> ask()
            "log" -> logToFile()
            "hardest card" -> hardestCard()
            "reset stats" -> resetStats()
        }

        printlnAndStoreLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        action = scanAndStoreLog()
    }

    printlnAndStoreLog("Bye bye!")

    if (exportFileName >= 0) exportFile(args[exportFileName])
}

fun add() {
    printlnAndStoreLog("The card:")
    val card = scanAndStoreLog()
    if (cards.containsKey(card)) {
        printlnAndStoreLog("The card \"$card\" already exists.")
    } else {
        printlnAndStoreLog("The definition of the card:")
        val definition = scanAndStoreLog()
        if (cards.containsValue(definition)) {
            printlnAndStoreLog("The definition \"$definition\" already exists.")
        } else {
            mistakesCounter[card] = 0
            cards[card] = definition
            printlnAndStoreLog("The pair (\"$card\":\"$definition\") has been added.")
        }
    }
}

fun remove() {
    printlnAndStoreLog("The card:")
    val card = scanAndStoreLog()
    if (cards.containsKey(card)) {
        cards.remove(card)
        mistakesCounter.remove(card)
        printlnAndStoreLog("The card has been removed.")
    } else {
        printlnAndStoreLog("Can't remove \"$card\": there is no such card.")
    }
}

fun askForFile(): String {
    printlnAndStoreLog("File name:")
    return scanAndStoreLog()
}

fun importFile(fileName: String) {
    val file = File(fileName)
    if (file.exists()) {
        val readFile = file.readLines()
        var index = 0
        while (index < readFile.size - 1) {
            val card = readFile[index].filter { it.isLetter() || it == ' ' }
            cards[card] = readFile[index + 1]
            mistakesCounter[card] = readFile[index].filter { it.isDigit() }.toInt()
            index += 2
        }
        printlnAndStoreLog("${readFile.size / 2} cards have been loaded.")
    } else {
        printlnAndStoreLog("File not found.")
    }
}

fun exportFile(fileName: String) {
    val file = File(fileName)

    val s = buildString {
        cards.keys.forEach {
            append("${mistakesCounter[it]}$it\n")
            append("${cards[it]}\n")
        }
    }

    file.writeText(s)

    printlnAndStoreLog("${cards.size} cards have been saved.")
}

fun ask() {
    printlnAndStoreLog("How many times to ask?")
    val numberToAsk = scanAndStoreLog().toInt()
    val keyCards = cards.keys
    val cardsList = keyCards.toList()

    repeat(numberToAsk) {
        val positionToAsk = if(keyCards.size > 1) Random.nextInt(keyCards.size - 1) else 0
        val cardToAsk = cardsList[positionToAsk]

        printlnAndStoreLog("Print the definition of \"$cardToAsk\"")
        val answer = scanAndStoreLog()
        if (cards[cardToAsk] == answer) {
            printlnAndStoreLog("Correct answer.")
        } else {
            if(mistakesCounter.containsKey(cardToAsk)) {
                mistakesCounter[cardToAsk] = mistakesCounter[cardToAsk]!! + 1
            }

            printlnAndStoreLog("Wrong answer. The correct one is \"${cards[cardToAsk]}\"${
            if (cards.containsValue(answer))
                ", you've just written the definition of \"${cards.filterValues { it == answer }.keys.first()}\""
            else
                ""}.")
        }
    }
}

fun logToFile() {
    val fileName = askForFile()
    val file = File(fileName)

    val s = buildString {
        log.forEach {
            append("$it\n")
        }
    }

    file.writeText(s)

    printlnAndStoreLog("The log has been saved.")
}

fun hardestCard() {
    val biggerEntry = mistakesCounter.maxBy { it.value }
    if (biggerEntry != null) {
        if (biggerEntry.value != 0) {
            val allItemsWithBiggerEntry = mistakesCounter.filter { it.value == biggerEntry.value }
            var failedCards = ""
            allItemsWithBiggerEntry.keys.forEachIndexed {
                    index, card ->
                failedCards += if(index == allItemsWithBiggerEntry.size - 1) {
                    "\"${card}\""
                } else {
                    "\"${card}\", "
                }
            }
            if (allItemsWithBiggerEntry.size > 1) {
                printlnAndStoreLog("The hardest cards are $failedCards. You have ${biggerEntry.value} errors answering them.")
            } else {
                printlnAndStoreLog("The hardest card is $failedCards. You have ${biggerEntry.value} errors answering it.")
            }
        } else {
            printlnAndStoreLog("There are no cards with errors.")
        }
    } else {
        printlnAndStoreLog("There are no cards with errors.")
    }
}

fun resetStats() {
    mistakesCounter.forEach {
        mistakesCounter[it.key] = 0
    }
    printlnAndStoreLog("Card statistics has been reset.")
}


fun buildString(build: StringBuilder.() -> Unit): String {
    val stringBuilder = StringBuilder()
    stringBuilder.build()
    return stringBuilder.toString()
}
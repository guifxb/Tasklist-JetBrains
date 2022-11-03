package tasklist
import kotlinx.datetime.*
import com.squareup.moshi.*
import java.io.File
import java.lang.reflect.*


data class Task(
    var num: Int,
    var priority: String,
    var date: String,
    var time: String,
    var task: MutableList<String>,
    var dueTag: String,
)
    var action: String = ""

fun main() {
    //menu
    println("Input an action (add, print, edit, delete, end):")
    action = readln()

    val taskList = mutableListOf<Task>()
    var isRunning = true
    var validDate: LocalDate
    val jsonFile = File("tasklist.json")
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val type: ParameterizedType = Types.newParameterizedType(List::class.java, Task::class.java)
    val taskListAdapter: JsonAdapter<List<Task?>> = moshi.adapter(type)

    if (jsonFile.exists()) {
        //taskList.add(taskListAdapter.fromJson(jsonFile.readText()))
        var tempTask = taskListAdapter.fromJson(jsonFile.readText())
        if (tempTask != null) {
            for (temp in tempTask) {
                temp?.let { taskList.add(it) }
            }
        }
    }



    fun renumberTasks() {
        for (task in taskList) {
            task.num = taskList.indexOf(task) + 1
        }
    }

    fun printTasks() {
        println("""
            +----+------------+-------+---+---+--------------------------------------------+
            | N  |    Date    | Time  | P | D |                   Task                     |
            +----+------------+-------+---+---+--------------------------------------------+
           """.trimIndent())
        for (task in taskList) {
            println("| ${task.num.toString().padEnd(3)}| ${task.date} | ${task.time} | ${when (task.priority.uppercase()) {
                "C" -> "\u001B[101m \u001B[0m"
                "H" -> "\u001B[103m \u001B[0m"
                "N" -> "\u001B[102m \u001B[0m"
                "L" -> "\u001B[104m \u001B[0m"
                else -> "cajamanga"
            }   } | ${when (task.dueTag.uppercase()) {
                "I" -> "\u001B[102m \u001B[0m"
                "T" -> "\u001B[103m \u001B[0m"
                "O" -> "\u001B[101m \u001B[0m"
                else -> "cajamanga"

            }            } |${if (task.task[0].length > 44) {
                "${task.task[0].substring(0, 44)}|\n|    |            |       |   |   |${task.task[0].substring(44).padEnd(44)}"

            } else {
                task.task[0].padEnd(44)
            }
            }|")
            for (index in 1..task.task.lastIndex) if (task.task[index].length > 44) {
                val chunks = task.task[index].chunked(44)
                for (chunk in chunks)
                    println("|    |            |       |   |   |${chunk.padEnd(44)}|")
            }
            else {
                println("|    |            |       |   |   |${task.task[index].padEnd(44)}|")
            }
            println("+----+------------+-------+---+---+--------------------------------------------+")
        }
    }

    fun isValidDate(): LocalDate {
        println("Input the date (yyyy-mm-dd):")
        var tryDate = LocalDate(2017, 4, 29)
        try {
            val (year, month, day) = readln().split("-")
            tryDate = LocalDate(year.toInt(), month.toInt(), day.toInt())
        } catch (e: Exception) {
            println("The input date is invalid")
            isValidDate()
        }
        return tryDate
    }

    fun isValidTime(): String {
        println("Input the time (hh:mm):")
        var tryTime = LocalDateTime(100, 1, 1, 1, 1)
        try {
            val (hour, minute) = readln().split(":")
            tryTime = LocalDateTime(1000, 1, 1, hour.toInt(), minute.toInt())
            return "${if (tryTime.hour < 10) "0${tryTime.hour}" else tryTime.hour}:${if (tryTime.minute < 10) "0${tryTime.minute}" else tryTime.minute}"
        } catch (e: Exception) {
            println("The input time is invalid")
            isValidTime()
        }
        return "${if (tryTime.hour < 10) "0${tryTime.hour}" else tryTime.hour}:${if (tryTime.minute < 10) "0${tryTime.minute}" else tryTime.minute}"
    }

    while (isRunning) {
        when (action.lowercase()) {
            "add" -> {
                val CurrentTask = Task(0, "", "", "", mutableListOf(""), "")
                //priority
                println("Input the task priority (C, H, N, L):")
                var priority = readln()
                val validPriority = listOf("C", "H", "N", "L")
                while (priority.uppercase() !in validPriority) {
                    println("Input the task priority (C, H, N, L):")
                    priority = readln()
                }
                CurrentTask.priority = priority.uppercase()

                //date
                validDate = isValidDate()
                CurrentTask.date = validDate.toString()

                //time
                CurrentTask.time = isValidTime()

                //task, dueTag and renumber
                println("Input a new task (enter a blank line to end):")
                var currentAsk = readln()
                val currentTask = mutableListOf<String>()
                if (currentAsk.isBlank()) {
                    println("The task is blank")
                    println("Input an action (add, print, edit, delete, end):")
                    action = readln()

                } else {
                    while (currentAsk.trim().length != 0) {
                        currentTask.add(currentAsk.trim())
                        currentAsk = readln()
                    }
                    CurrentTask.task = currentTask
                    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
                    val numberOfDays = currentDate.daysUntil(validDate)
                    when {
                        numberOfDays == 0 -> CurrentTask.dueTag = "T"
                        numberOfDays > 0 -> CurrentTask.dueTag = "I"
                        numberOfDays < 0 -> CurrentTask.dueTag = "O"
                    }
                    taskList.add(CurrentTask)
                    renumberTasks()
                    println("Input an action (add, print, edit, delete, end):")
                    action = readln()
                }

            }

            "print" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                    println("Input an action (add, print, edit, delete, end):")
                    action = readln()
                } else {
                    printTasks()
                    println("Input an action (add, print, edit, delete, end):")
                    action = readln()
                }
            }

            "edit" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                    println("Input an action (add, print, edit, delete, end):")
                    action = readln()
                } else {
                    printTasks()
                    println("Input the task number (1-${taskList.size}):")
                    var taskToEdit = readln()
                    while (taskToEdit.toIntOrNull() == null || taskToEdit.toIntOrNull() !in 1..taskList.size) {
                        println("Invalid task number")
                        println("Input the task number (1-${taskList.size}):")
                        taskToEdit = readln()
                    }
                    println("Input a field to edit (priority, date, time, task):")
                    var field = readln()
                    val options = listOf("priority", "date", "time", "task")
                    while (field !in options) {
                        println("Invalid field")
                        println("Input a field to edit (priority, date, time, task):")
                        field = readln()
                    }
                    when (field) {
                        "priority" -> {
                            println("Input the task priority (C, H, N, L):")
                            var priority = readln()
                            val validPriority = listOf("C", "H", "N", "L")
                            while (priority.uppercase() !in validPriority) {
                                println("Input the task priority (C, H, N, L):")
                                priority = readln()
                            }
                            println("The task is changed")
                            taskList[taskToEdit.toInt() - 1].priority = priority.uppercase()
                            println("Input an action (add, print, edit, delete, end):")
                            action = readln()
                        }
                        "date" -> {
                            validDate = isValidDate()
                            taskList[taskToEdit.toInt() - 1].date = validDate.toString()
                            println("The task is changed")
                            println("Input an action (add, print, edit, delete, end):")
                            action = readln()
                        }
                        "time" -> {
                            taskList[taskToEdit.toInt() - 1].time = isValidTime()
                            println("The task is changed")
                            println("Input an action (add, print, edit, delete, end):")
                            action = readln()
                        }
                        "task" -> {
                            println("Input a new task (enter a blank line to end):")
                            var currentAsk = readln()
                            val currentTask = mutableListOf<String>()
                            if (currentAsk.isBlank()) {
                                println("The task is blank")
                                println("Input an action (add, print, edit, delete, end):")
                                action = readln()
                            } else {
                                while (currentAsk.trim().length != 0) {
                                    currentTask.add(currentAsk.trim())
                                    currentAsk = readln()
                                }
                                taskList[taskToEdit.toInt() - 1].task = currentTask
                                println("The task is changed")
                                println("Input an action (add, print, edit, delete, end):")
                                action = readln()
                            }
                        }
                    }
                    renumberTasks()
                }
            }
            "delete" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                    println("Input an action (add, print, edit, delete, end):")
                    action = readln()
                } else {
                    printTasks()
                    println("Input the task number (1-${taskList.size}):")
                    var taskToDel = readln()
                    while (taskToDel.toIntOrNull() == null || taskToDel.toIntOrNull() !in 1..taskList.size) {
                        println("Invalid task number")
                        println("Input the task number (1-${taskList.size}):")
                        taskToDel = readln()
                    }
                    taskList.removeAt(taskToDel.toInt() - 1)
                    println("The task is deleted")
                    renumberTasks()
                    println("Input an action (add, print, edit, delete, end):")
                    action = readln()
                }
            }
            "end" -> {
                println("Tasklist exiting!")
                jsonFile.writeText(taskListAdapter.toJson(taskList))
                isRunning = false
            }

            else -> {
                println("The input action is invalid")
                println("Input an action (add, print, edit, delete, end):")
                action = readln()
            }
        }
    }
}

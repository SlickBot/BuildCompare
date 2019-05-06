package com.slicky.buildcompare.core

import javafx.scene.chart.XYChart
import tornadofx.*
import java.io.File

class Gradle(
        var onStart: () -> Unit = {},
        var onFinish: () -> Unit = {}
) {

    private val lock = Any()
    private var process: Process? = null

    fun startProcess(
            projects: List<Project>,
            repeatCount: Int
    ) = runAsync {
        onStart()
        repeat(repeatCount) { i ->
            println("\ni = ${i + 1}")
            for (project in projects) {
                runAdbBuild(project)
            }
        }
        onFinish()
    }

    private fun runAdbBuild(project: Project) {
        print("${project.name}: stopDaemons")
        val stopDuration = stopDaemonsTask(project)
        println("  ${stopDuration}ms")

        print("${project.name}: clean")
        val cleanDuration = cleanTask(project)
        println("  ${cleanDuration}ms")

        print("${project.name}: build")
        val buildDuration = buildTask(project)
        println("  ${buildDuration}ms")
        
        project.addValue(buildDuration)
    }

    private fun cleanTask(project: Project): Long {
        return execute("${project.gradlewPath()} --b ${project.buildPath()} -q clean")
    }

    private fun buildTask(project: Project): Long {
        return execute("${project.gradlewPath()} --b ${project.buildPath()} -q assembleRelease")
    }

    private fun stopDaemonsTask(project: Project): Long {
        return execute("${project.gradlewPath()} --q --stop")
    }

    private fun execute(command: String): Long {
        synchronized(lock) {
            if (process != null) return -1

            val timeStart = System.currentTimeMillis()
            val p = Runtime.getRuntime().exec(command)
            process = p
            val result = p.waitFor()
            val timeEnd = System.currentTimeMillis()
            process = null

            if (result != 0) {
                println("output: ${p.inputStream.reader().readText()}")
                println("error: ${p.errorStream.reader().readText()}")
            }

            return timeEnd - timeStart
        }
    }

    class Project(
            private val rootDir: File,
            val name: String
    ) {

        private val projectName = "${name}FakeSocial"
        private val file =  File(rootDir, projectName)

        val dataList = mutableListOf<Long>()
        val series = createNewSeries(name)

        init {
            require(file.exists())
        }
        
        fun addValue(value: Long) {
            dataList += value
            series.data.add(createNewData(dataList.size.toString(), value / 1000.0))
        }

        fun gradlewPath(): String {
            return File(rootDir, "$projectName/gradlew").path
        }

        fun buildPath(): String {
            return File(rootDir, "$projectName/build.gradle").path
        }

        private fun createNewSeries(title: String) =
                XYChart.Series(title, mutableListOf<XYChart.Data<String, Number>>().observable())

        private fun createNewData(label: String, value: Number) =
                XYChart.Data<String, Number>(label, value)

    }

}

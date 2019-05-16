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
            repeatCount: Int,
            incremental: Boolean
    ) = runAsync {
        onStart()
        repeat(repeatCount) { i ->
            println("\ni = ${i + 1}")
            for (project in projects) {
                val duration = if (incremental) {
                    incrementalBuild(project)
                } else {
                    cleanBuild(project)
                }
                project.addValue(duration)
            }
        }
        onFinish()
    }

    private fun incrementalBuild(project: Project): Long {
        print("${project.name}: stopDaemons")
        val stopDuration = stopDaemonsTask(project)
        println("  ${stopDuration}ms")

        print("${project.name}: clean")
        val cleanDuration = cleanTask(project)
        println("  ${cleanDuration}ms")

        print("${project.name}: build")
        val buildDuration = buildTask(project)
        println("  ${buildDuration}ms")

        print("${project.name}: patch")
        val patchDuration = patchFileTask(project)
        println("  ${patchDuration}ms")

        print("${project.name}: build2")
        val build2Duration = buildTask(project)
        println("  ${build2Duration}ms")

        print("${project.name}: unpatch")
        val unpatchDuration = unpatchFileTask(project)
        println("  ${unpatchDuration}ms")

        return build2Duration
    }

    private fun cleanBuild(project: Project): Long {
        print("${project.name}: stopDaemons")
        val stopDuration = stopDaemonsTask(project)
        println("  ${stopDuration}ms")

        print("${project.name}: clean")
        val cleanDuration = cleanTask(project)
        println("  ${cleanDuration}ms")

        print("${project.name}: build")
        val buildDuration = buildTask(project)
        println("  ${buildDuration}ms")

        return buildDuration
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

    private fun patchFileTask(project: Project): Long {
//        val command = "patch -d ${project.file.path} -p0 -N < ${project.patchPath()}"
//        println("\nCOMMAND PATCH: $command\n")
//        return execute(command, project.file)

//        val p = ProcessBuilder()
////                .directory(project.file)
//                .redirectErrorStream(true)
//                .command(
//                        "patch",
////                        "-d", project.file.path,
////                        "-d", "/home/slicky/Diploma/BuildCompare",
////                        "-d/",
//                        "-p0",
////                        "-p4",
//                        "-N",
////                        "<", "incremental_change_${project.name.toLowerCase()}.patch"
//                        "<", "${project.file.path}/incremental_change.patch"
//                )
//                .start()

        val path = "${project.file.path}/app/src/main/${project.name.toLowerCase()}/com/ulj/slicky/${project.name.toLowerCase()}fakesocial/activity/content"
        val ext = if (project.name == "Java") "java" else "kt"

        val p1 = ProcessBuilder("mv", "$path/ContentAdapter.$ext", "$path/ContentAdapter.$ext.old").start()
        val r1 = p1.waitFor()

        if (r1 != 0) {
            println("output: ${p1.inputStream.reader().readText()}")
            println("error: ${p1.errorStream.reader().readText()}")
        }

        val p2 = ProcessBuilder("mv", "$path/ContentAdapter.$ext.new", "$path/ContentAdapter.$ext").start()
        val r2 = p2.waitFor()

        if (r2 != 0) {
            println("output: ${p2.inputStream.reader().readText()}")
            println("error: ${p2.errorStream.reader().readText()}")
        }

        return 0
    }

    private fun unpatchFileTask(project: Project): Long {
        val path = "${project.file.path}/app/src/main/${project.name.toLowerCase()}/com/ulj/slicky/${project.name.toLowerCase()}fakesocial/activity/content"
        val ext = if (project.name == "Java") "java" else "kt"

        val p1 = ProcessBuilder("mv", "$path/ContentAdapter.$ext", "$path/ContentAdapter.$ext.new").start()
        val r1 = p1.waitFor()

        if (r1 != 0) {
            println("output: ${p1.inputStream.reader().readText()}")
            println("error: ${p1.errorStream.reader().readText()}")
        }

        val p2 = ProcessBuilder("mv", "$path/ContentAdapter.$ext.old", "$path/ContentAdapter.$ext").start()
        val r2 = p2.waitFor()

        if (r2 != 0) {
            println("output: ${p2.inputStream.reader().readText()}")
            println("error: ${p2.errorStream.reader().readText()}")
        }

        return 0
    }

    private fun execute(command: String, directory: File? = null): Long {
        synchronized(lock) {
            if (process != null) return -1

            val timeStart = System.currentTimeMillis()
            val p = Runtime.getRuntime().exec(command, null, directory)
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
        val file =  File(rootDir, projectName)

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

//        fun patchPath(): String {
//            return File(rootDir, "$projectName/incremental_change.patch").path
//        }

        private fun createNewSeries(title: String) =
                XYChart.Series(title, mutableListOf<XYChart.Data<String, Number>>().observable())

        private fun createNewData(label: String, value: Number) =
                XYChart.Data<String, Number>(label, value)

    }

}

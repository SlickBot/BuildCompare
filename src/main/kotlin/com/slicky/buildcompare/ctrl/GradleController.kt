package com.slicky.buildcompare.ctrl

import com.slicky.buildcompare.core.Gradle
import com.slicky.buildcompare.model.*
import com.slicky.buildcompare.view.MainView
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.SnapshotParameters
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import javax.imageio.ImageIO

class GradleController : Controller() {

    private val view: MainView by inject()

    val model = MainViewModel(MainViewData())
//    val props = PropertyHandler("adb.properties")

    init {
        initRootPath()
        initRepeatCount()
        initCsvOutPath()
    }

    fun startBuildingProcess() {
        // Return if model could not commit.
        if (!model.commit())
            return

        // Run on async thread.
        runAsync {
            try {
                // Do work!
                doWork()
            } catch (e: Exception) {
                // Notify user.
                displayAlert("Error occurred while building!", e)
            }
        }
    }

    private fun initRootPath() {
        preferences { model.rootPath.value = get("root_path", "") }
        model.rootPath.onChange { preferences { put("root_path", it) } }
    }

    private fun initRepeatCount() {
        preferences { model.iterations.value = getInt("repeat_count", 1) }
        model.iterations.onChange { preferences { putInt("repeat_count", it?.toInt() ?: 1) } }
    }

    private fun initCsvOutPath() {
        preferences { model.outputPath.value = get("output_path", System.getProperty("user.dir")) }
        model.outputPath.onChange { preferences { put("output_path", it) } }
    }

    private fun doWork() {
        // If period amount is less than 1, its pointless to do anything.
        if (model.iterations.value.toInt() < 1)
            return

        val rootFile = File(model.rootPath.value)
        if (!rootFile.isDirectory) {
            displayAlert("Root path is not directory!")
            return
        }

        val projects = try {
            mutableListOf<Gradle.Project>().apply {
                if (model.javaActive.value)
                    this += Gradle.Project(rootFile, "Java")
                if (model.kotlinActive.value)
                    this += Gradle.Project(rootFile, "Kotlin")
                if (model.parcelizeActive.value)
                    this += Gradle.Project(rootFile, "Parcelize")
                if (model.ankoActive.value)
                    this += Gradle.Project(rootFile, "Anko")
            }
        } catch (e: Exception) {
            displayAlert("Could not find one of projects directory!")
            return
        }

        // Clear graph on UI thread and add Series.
        Platform.runLater {
            view.graph.data.clear()
            for (project in projects) {
                view.graph.data.add(project.series)
            }
        }

        val csvFile = File(model.outputPath.value)
        if (!csvFile.isDirectory) {
            displayAlert("CSV output path is not directory!")
            return
        }

        val gradle = Gradle(
                onStart = {
                    view.isLoading = true
                },
                onFinish = {
                    runAsync {
                        val file = File(model.outputPath.value, "durations-${System.currentTimeMillis()}.csv")
                        file.printWriter().use { w ->
                            w.println(projects.joinToString(",") { it.name })
                            for (i in 0 until (projects.firstOrNull()?.dataList?.size ?: 0)) {
                                w.println(projects.joinToString(",") { it.dataList[i].toString() })
                            }
                        }
                        saveFile(File(model.outputPath.value, "image-${System.currentTimeMillis()}.png"))
                        view.isLoading = false
                    }
                }
        )
        gradle.startProcess(projects, model.iterations.value.toInt(), model.incrementalBuild.value)
    }

    fun saveSnapshot() {
        chooseFile("Save Snapshot",
                arrayOf(FileChooser.ExtensionFilter("PNG Images", "*.png")),
                FileChooserMode.Save,
                primaryStage
        ).firstOrNull()?.let { saveFile(it) }
    }

    private fun saveFile(file: File) {
        Platform.runLater {
            val image = view.graph.snapshot(SnapshotParameters(), null)
            runAsync {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
            }
        }
    }

    private fun displayAlert(text: String, e: Exception? = null) {
        Platform.runLater {
            if (e != null) {
                e.printStackTrace()
                alert(Alert.AlertType.ERROR, text, e.localizedMessage)
            } else {
                alert(Alert.AlertType.ERROR, text)
            }
        }
    }

}
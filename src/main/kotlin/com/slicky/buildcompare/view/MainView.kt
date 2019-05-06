package com.slicky.buildcompare.view

import com.slicky.buildcompare.app.Styles
import com.slicky.buildcompare.ctrl.GradleController
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.chart.*
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.RadioButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.BorderPane
import tornadofx.*

class MainView : View("BuildCompare") {

    private val ctrl: GradleController by inject()

    lateinit var mainPane: BorderPane
    lateinit var graph: XYChart<String, Number>
    lateinit var indicator: ProgressIndicator

    var isLoading: Boolean = false
        set(value) {
            field = value
            Platform.runLater {
                mainPane.isDisable = value
                indicator.isVisible = value
            }
        }


    override val root = borderpane {
        mainPane = this

        center = stackpane {
            graph = areachart(
                    x = CategoryAxis().apply { label = "Iteration" },
                    y = NumberAxis(0.0, 40.0, .2).apply { label = "Build duration" }
            ) {
                addClass(Styles.graph)
                createSymbols = false
            }
            indicator = progressindicator {
                addClass(Styles.indicator)
                isVisible = false
            }
        }

        right = form {
            prefWidth = 325.0

            fieldset("Project properties") {
                field("Root path") { textfield(ctrl.model.rootPath).required()}
                field("Iterations") { integerfield(ctrl.model.iterations).required() }
                field("Projects") {
                    radiobutton("Java", ctrl.model.javaActive)
                    radiobutton("Kotlin", ctrl.model.kotlinActive)
                    radiobutton("Parcelize", ctrl.model.parcelizeActive)
//                    radiobutton("Anko", ctrl.model.ankoActive)
                }
                field {
                    hbox {
                        addClass(Styles.buttonField)
                        button("Start building") { setOnAction { ctrl.startBuildingProcess() } }
                    }
                }
            }

            fieldset("Output") {
                field("Output path") { textfield(ctrl.model.outputPath).required()}
            }

            fieldset {
                addClass(Styles.buttonField)
                button("Save graph") { setOnAction { ctrl.saveSnapshot() } }
            }

        }

    }

    private fun EventTarget.radiobutton(
            text: String? = null,
            property: Property<Boolean>? = null,
            group: ToggleGroup? = getToggleGroup(),
            op: RadioButton.() -> Unit = {}
    ): RadioButton = radiobutton(text, group) {
        if (property != null) selectedProperty().bindBidirectional(property)
        op()
    }

}

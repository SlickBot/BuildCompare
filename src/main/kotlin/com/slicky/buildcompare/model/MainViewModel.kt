package com.slicky.buildcompare.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class MainViewData {

    val rootPathProperty = SimpleStringProperty("")
    val iterationsProperty = SimpleIntegerProperty(30)
    val outputPathProperty = SimpleStringProperty("")

    val javaActiveProperty = SimpleBooleanProperty(true)
    val kotlinActiveProperty = SimpleBooleanProperty(true)
    val parcelizeActiveProperty = SimpleBooleanProperty(false)
    val ankoActiveProperty = SimpleBooleanProperty(false)

}

class MainViewModel(private val data: MainViewData) : ViewModel() {

    val rootPath = bind { data.rootPathProperty }
    val iterations = bind { data.iterationsProperty }
    val outputPath = bind { data.outputPathProperty }

    val javaActive = bind { data.javaActiveProperty }
    val kotlinActive = bind { data.kotlinActiveProperty }
    val parcelizeActive = bind { data.parcelizeActiveProperty }
    val ankoActive = bind { data.ankoActiveProperty }

}

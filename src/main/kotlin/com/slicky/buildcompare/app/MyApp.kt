package com.slicky.buildcompare.app

import com.slicky.buildcompare.view.MainView
import javafx.stage.Stage
import tornadofx.*
import kotlin.system.exitProcess

class MyApp: App(MainView::class, Styles::class) {

    override fun start(stage: Stage) {
        super.start(stage.apply {
            width = 1000.0
            height = 600.0
            setOnCloseRequest { exitProcess(0) }
        })
    }

}

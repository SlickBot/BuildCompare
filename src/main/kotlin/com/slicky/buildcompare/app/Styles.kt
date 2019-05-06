package com.slicky.buildcompare.app

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {

    companion object {
        val indicator by cssclass()
        val buttonField by cssclass()

        val graph by cssclass()

        val color0 = c(255, 85, 0)
        val color0a = c(255, 85, 0, 0.1)
        val color1 = c(0, 85, 255)
        val color1a = c(0, 85, 255, 0.1)
    }

    init {

        graph {
            chartSeriesLine {
                strokeWidth = 2.px
            }

            defaultColor0 and chartAreaSymbol { backgroundColor += color0 }
            defaultColor0 and chartSeriesAreaLine { stroke = color0 }
            defaultColor0 and chartSeriesAreaFill { fill = color0a }

            defaultColor1 and chartAreaSymbol { backgroundColor += color1 }
            defaultColor1 and chartSeriesAreaLine { stroke = color1 }
            defaultColor1 and chartSeriesAreaFill { fill = color1a }
        }


        buttonField {
            alignment = Pos.CENTER
            button {
                fontSize = 12.px
                fontWeight = FontWeight.BOLD
            }
        }

        progressIndicator and indicator {
            maxWidth = 100.px
            maxHeight = 100.px
        }

    }

}

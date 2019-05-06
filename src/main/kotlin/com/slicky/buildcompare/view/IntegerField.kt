package com.slicky.buildcompare.view

import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.control.TextField
import tornadofx.*

/**
 * Created by SlickyPC on 25.5.2017
 */
class IntegerField : TextField() {

    val integerProperty = SimpleObjectProperty<Number>()

    init {
        textProperty().addListener { _, _, new ->
            new?.let { text = new.replace("[^\\d]".toRegex(), "") }
        }
    }
}

fun EventTarget.integerfield(value: Number? = null, op: (IntegerField.() -> Unit) = {}): IntegerField {
    val field = IntegerField().apply {
        value?.let { integerProperty.value = value }
    }
    return opcr(this, field, op)
}

fun EventTarget.integerfield(property: Property<Number>, op: (IntegerField.() -> Unit) = {}): IntegerField {
    return integerfield(op = op).apply {
        bind(property)
    }
}

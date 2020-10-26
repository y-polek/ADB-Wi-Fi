package dev.polek.adbwifi.utils

fun Process.output(): String = this.inputStream?.bufferedReader()?.readText()?.trim('\n').orEmpty()

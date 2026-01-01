package org.vorpal.kosmos.laws.util

fun String.midfix(left: String = " ", right: String = " "): String =
    if (isBlank()) "" else "$left$this$right"
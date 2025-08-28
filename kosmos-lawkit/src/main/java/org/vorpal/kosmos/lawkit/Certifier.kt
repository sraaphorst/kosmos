package org.vorpal.kosmos.lawkit

fun interface Certifier<A> {
    /** Return an empty list on success; otherwise a list of Throwables explaining the failures. */
    suspend fun runOn(subject: A): List<Throwable>
}

inline fun <A> law(
    label: String,
    crossinline block: suspend () -> Unit
): Certifier<A> = Certifier {
    val err = runCatching { block() }.exceptionOrNull()
    if (err == null) emptyList()
    else listOf(IllegalStateException("[$label] ${err.message}", err))
}

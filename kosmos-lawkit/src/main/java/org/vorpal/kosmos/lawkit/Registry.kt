package org.vorpal.kosmos.lawkit

import org.vorpal.kosmos.core.Lawful
import kotlin.reflect.KClass

/**
 * Registry to certify objects and ensure that they pass the required laws.
 * If they pass, a Pass<A> will be returned with the subject registered for use.
 * If one or more laws fail, the exceptions that are generated will be collected and returned
 * in a Fail<A> instance along with the subject to allow further investigation.
 */
object Registry {
    /**
     * Run all certifiers on [subject]. If any emit errors, return Fail(subject, errors).
     * Otherwise, return Pass(subject).
     */
    suspend inline fun <reified A> register(
        subject: A,
        certifiers: List<Certifier<A>>
    ): Lawful<A> {
        val errors = buildList {
            certifiers.forEach { addAll(it.runOn(subject)) }
        }
        val c = A::class
        return if (errors.isEmpty()) Lawful.Pass(subject)
        else Lawful.Fail(subject, errors)
    }

    /** Convenience vararg function. */
    suspend inline fun <reified A> register(
        subject: A,
        vararg certifiers: Certifier<A>
    ): Lawful<A> = register(subject, certifiers.toList())

    private val cache: Map<Pair<Any, KClass<*>>, Lawful<*>> = mutableMapOf()
}

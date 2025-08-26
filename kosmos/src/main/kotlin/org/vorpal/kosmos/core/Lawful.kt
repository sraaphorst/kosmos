package org.vorpal.org.vorpal.kosmos.core

/** Evidence that a type passed is lawful, i.e. passed the lawful tests in its suite. */
class Lawful<S> internal constructor(val value: S)
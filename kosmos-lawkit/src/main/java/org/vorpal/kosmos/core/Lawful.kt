package org.vorpal.kosmos.core

/** A lawful instance of a type, i.e. one that has passed the requisite tests. */
class Lawful<S> internal constructor(val value: S)
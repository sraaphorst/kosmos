package org.vorpal.kosmos.core

/** Extend Boolean to support the infix implies operation. */
infix fun Boolean.implies(q: Boolean) = (!this) || q

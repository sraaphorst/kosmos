package org.vorpal.kosmos.core

infix fun Boolean.implies(q: Boolean) = (!this) || q
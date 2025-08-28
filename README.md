# Kosmos

*A universe of algebraic structures for code and crypto.*

This is a work in progress. Currently, the project is split into the following modules:

* **kosmos:** The core libraries that embody the mathematical objects that comprise kosmos.
* **kosmos-laws:** Reusable framework-agnostic law predicates that can be called from any runner.
* **kosmos-lawkit:** The "batteries included" layer which comprises the kotest adapters, registration DSL, and lazy
certified instances.

////kosmos-lawkit/src/main/kotlin/org/vorpal/kosmos/lawkit/Certify.kt
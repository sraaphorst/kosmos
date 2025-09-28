# Kosmos: Introduction

Kosmos is a library of mathematical structures and algorithms written for Kotlin. The fundamental idea behind Kosmos is:
1. Define a mathematical object, e.g. a `Monoid` over a type `A`.
2. Run the instance of the mathematical object against the Kosmos lawkit, which checks to ensure that the object meets
the definition of the type.
3. If the object passes, a `Lawful<Monoid<A>>` is returned, which is cached in the Kosmos registry. In the case of
failure, a `FailedCertifiation<Monoid<A>>` is returned, which contains a `List<Throwable>` that identifies the errors
that occurred during the certification process.
4. The `Lawful` object can then be used safely in the algorithms that Kosmos provides. Note that it is possible to use
an uncertified object in the algorithms as well, but since they have not met the contract that is guaranteed by the
certification process, the algorithms might fail with an exception or undefined behavior. 

* [`kosmos-core`](01-kosmos-core.md): This forms the core of Kosmos, i.e. the structures that Kosmos uses. Examples of structures
that Kosmos implements are:
  * `BinOp<A>`: An arbitrary binary operation over a type `A`.
  * `Monoid<A>`: A monoid over a type `A`.
  * `Group<A>`: A group over a type `A`.
  * `Ring<A>`: A ring over a type `A`, which consists of:
    * A `Monoid<A>` typically representing a concept analogous to multiplication.
    * An `AbelianGroup<A>` typically representing a concept analogous to addition.
    * The monoid acts distributively over the abelian group.

* [`kosmos-lawkit`](02-kosmos-lawkit.md): This subproject serves as the Registry and the Certification process that
Kosmos uses in order to determine if a type is `Lawful` and to cache it for future use, and indicate failures if a type
does not meet the requirements of a definition.

* [`kosmos-laws`](03-kosmos-laws.md): This subproject comprises:
  * The individual laws that Kosmos types can meet, e.g. a `BinOp<A>` (binary operation over a type `A`) can be
  associative, commutative, invertible, etc.
  * Collections of laws assembled to provide law suites, which form the contracts an object must meet to be declared as
  a `Lawful` instance of that object.
  * Note that `kosmos-laws` depends on the `kotest` library in its runtime, hence the isolation of this subproject.

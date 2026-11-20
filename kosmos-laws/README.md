# kosmos-laws

This module defines reusable algebraic laws as property tests, independent of any concrete instances.

Packages:
- `org.vorpal.kosmos.laws.property` — atomic laws such as:
    - `AssociativityLaw`
    - `CommutativityLaw`
    - `IdentityLaw`
    - `InvertibilityLaw`
    - `DistributivityLaw`
    - `IdempotencyLaw`
    - `CancellativityLaw`
    - `NoZeroDivisorsLaw`
    - `InvolutionLaw`
    - `AlternativityLaw`
    - `PowerAssociativityLaw`
    - `FlexibilityLaw`
    - `JordanIdentityLaw`
    - `MoufangLaw`
    - `BolIdentityLaw`
    - `MedialLaw`
    - `NilpotentLaw`

- `org.vorpal.kosmos.laws.algebra` — bundles of laws for specific algebraic structures, e.g.:
  - `SemigroupLaws`
  - `MonoidLaws`
  - `GroupLaws`
  - `RingLaws`
  - `FieldLaws`
  - `OrderedFieldLaws`, `AbsoluteValueLaws`, `NaturalPowerLaws` / `IntegerPowerLaws`

- `org.vorpal.kosmos.laws.relation` — laws of relations (reflexivity, transitivity, trichotomy, ...) and the suites
  `EquivalenceRelationLaws`, `PreorderLaws`, `PartialOrderLaws`, `TotalOrderLaws`, `StrictPartialOrderLaws`,
  `StrictTotalOrderLaws`.

- Each suite is the complete contract of its structure, composed from the suites of its parent structures; see
  [the law-testing guide](../docs/03-kosmos-laws.md).

- Each law is a small class implementing `TestingLaw`, parameterised by:
  - the operation(s) under test (`BinOp<A>`, etc.)
  - Kotest arbitraries (`Arb<A>`, `Arb<Pair<A,A>>`, `Arb<Triple<A,A,A>>`)
  - an equality (`Eq<A>`) and optional pretty printer (`Printable<A>`)

- This module does not contain any arbitraries or tests for particular instances (such as `RealField` or `ComplexField`).
   Those live in `kosmos-testkit` and in the test sources of the concrete modules.

- Dependencies:
  - `kosmos-core` (for `Eq`, `Printable`, `BinOp`, etc.)
  -	`io.kotest:kotest-property` for property-based testing
  •	No Arrow, no external algebra libraries.

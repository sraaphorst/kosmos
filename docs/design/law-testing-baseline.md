# Law testing restructure: stage 0 baseline

Recorded before any runner code was added, for the restructure in
[`law-testing-restructure.md`](law-testing-restructure.md) (issues #252, #131, #61).

## Starting point

- Commit: `4b15e6f` (`main`, "Merge pull request #277 from sraaphorst/K256-patch")
- JDK 21 (OpenJDK 21.0.12), Gradle 9.5.1, Kotlin 2.2.0, Kotest 6.0.5
- Command: `./gradlew :kosmos-laws:test :kosmos-testkit:test --stacktrace`

| Module           | Specs | Tests | Failed | Errored | Skipped | Test time |
|------------------|------:|------:|-------:|--------:|--------:|----------:|
| `kosmos-laws`    |     0 |     0 |      0 |       0 |       0 |         – |
| `kosmos-testkit` |   142 |  2240 |      0 |       0 |       0 |    ~154 s |

Wall time: 4 min 9 s from a cold Gradle daemon (including compilation).

CI (`.github/workflows/tests.yml`) runs only `:kosmos-testkit:test`, under the check name `testkit`.
`kosmos-laws` has no tests yet.

## API inventory

Uses of the legacy law-running API (by `grep`, so approximate):

| Call                         | `kosmos-laws` main | `kosmos-testkit` tests |
|------------------------------|-------------------:|-----------------------:|
| `.laws()`                    |                 41 |                      1 |
| `.fullLaws()`                |                 45 |                      1 |
| `.test()`                    |                  6 |                    144 |
| `.fullTest()`                |                  1 |                    283 |
| `testAll()` / `fullTestAll()`|              1 / 1 |                  0 / 0 |
| `List<LawSuite>.flatten(full)`|                 1 |                      0 |
| `runLaws(...)`               |                  2 |                      0 |
| `runLawSuite(...)`           |                  3 |                      0 |
| `TestingLaw.named`           |                 35 |                      0 |
| `object : TestingLaw`        |                  5 |                      0 |

- 50 `LawSuite` implementations, all in `kosmos-laws`. No anonymous suites.
- `TestingLaw.named` builders: `AlgebraLawBuilders`, `StarAlgebraLawBuilders`, `BooleanAlgebraLawBuilders`,
  `ModuleLawBuilders`, `InnerProductSpaceLawBuilders`, `BilinearFormBuilders`, `HomomorphismLawBuilders`,
  `RealNormedDivisionAlgebraLaws`, `MeadowLaws`. Their class name is `<anonymous>` in the inventory.
- `CarlstromWheelLaws` has four anonymous `object : TestingLaw`.
- `MoufangLaw` runs three child laws from inside its own `test()`.
- Laws signal falsity mostly with Kotlin `check(...)` (123 calls), which throws `IllegalStateException`,
  inside Kotest `checkAll`. Kotest wraps every exception from a property body in an `AssertionError`,
  so at the runner boundary a false law and an operation that throws look alike. Stage 3 must address this.

## Ordered law inventory

`kosmos-testkit/src/test/kotlin/org/vorpal/kosmos/laws/inventory/LawInventorySpec.kt` records, for 32
representative suites (50 since stage 2, see below) over ℤ and ℚ, the ordered list of laws in both the compact (`laws()`) and full
(`fullLaws()`) views. The snapshot is `kosmos-testkit/src/test/resources/law-inventory.txt` and is checked
on every test run, so a structural refactor cannot silently drop, add or reorder a law.

### Observations

1. **Compact views are not "the new axioms only".** Each suite defines its own compact view.
   The single-operation chain (Magma → … → AbelianGroup) includes everything in both views,
   while the two-operation chain (Hemiring → … → Field) leaves the constituent additive and
   multiplicative structures out of the compact view.
2. **The compact `FieldLaws` view does not check multiplicative associativity**, nor additive
   associativity or commutativity. Those appear only in the full view, via
   `Field > DivisionRing > Ring > Rng > Hemiring > Semigroup(*) > associativity (*)`.
   The #252 acceptance example ("root → multiplicative monoid → associativity") therefore holds
   only for the full view, and the multiplicative part of a field sits under `Semigroup(*)` (identity
   comes from `RingLaws`), not under a multiplicative monoid.
3. **Full views run the same law many times.** For example, the full `StarAlgebra(ℚ over ℚ)` view
   (52 laws) runs the additive abelian-group laws 4 times and `identity (*)`/`distributivity`/`totality (*)`
   3 times. `LeftRModule` and `RModule` full views run the additive group twice.
   These are the targets for stage 4 (run shared nodes once).

### Stage 2 additions

Before migrating the remaining families, 18 more entries (lattices and Boolean algebra on `Boolean`,
homomorphisms `ℤ → ℤ`, setoid / equivalence relation, `NonAssociativeDivisionAlgebra(ℚ)`) were generated
against the stage 1 commit (`5ba03fc`, before any suite was migrated) and appended to the snapshot.

# Kosmos: `kosmos-laws`

`kosmos-laws` holds the algebraic laws of Kosmos as executable property tests, and the machinery that runs them.
`kosmos-testkit` holds the generators (`Arb`s), the JSON export and the Kotest integration, and the specs that use them.

This page covers how to write a law spec, how law suites are structured, and what a run reports.

## The pieces

| Piece | What it is |
|---|---|
| `TestingLaw` | One atomic law, e.g. `AssociativityLaw(op, arb, eq, pr)`. `suspend fun test()` checks it. |
| `LawSuite` | The laws of a structure, e.g. `FieldLaws(field, arb, eq, pr)`, built from smaller suites. A compound law such as `IdentityLaw` (left and right identity) is a small suite too. |
| `LawComponent` | Either of the two: anything with a `node()`, which `verify()` and `lawTests(...)` accept. |
| `LawNode` | A law graph: `Leaf(law)` or `Branch(label, children)`. A node may appear under several parents. |
| `LawRunner` / `LawSession` | Run a graph (all at once / one leaf at a time) and produce a `LawReport`. |
| `LawReport` | The result tree: status, counts, failure details, seed. Renders as text or JSON. |
| `lawTests(...)` (testkit) | Registers a graph in a Kotest `FunSpec`: one container per branch, one test per law. |

## Writing a spec

The recommended form registers the law graph with Kotest, so each law is its own test in Gradle and the IDE:

```kotlin
class RationalFieldSpec : FunSpec({
    val field = RationalAlgebras.RationalField

    lawTests(FieldLaws(field, ArbRational.small, RationalAlgebras.eqRational, RationalAlgebras.printableRationalPretty))

    test("reciprocal throws on zero") {
        shouldThrow<ArithmeticException> { field.reciprocal(Rational.ZERO) }
    }
})
```

Suites and standalone laws can be mixed in one graph with `lawGraph`:

```kotlin
lawTests(lawGraph("IntegerCommutativeRing") {
    suite(CommutativeRingLaws(ring, arb, eq, pr))
    suite(InvolutiveRingLaws(ring, arb, eq, pr))   // its ring laws are shared with the commutative ring's: they run once
    branch("norm squared") {
        law(NormSqMultiplicativeLaw(...))
        law(NormSqNonnegativeLaw(...))
    }
    skipped(SomeLaw(...), "needs an ordered field")   // shown as disabled, with the reason
})
```

Everything a spec checks lives in its graph. A check that belongs to one type only (an example, an edge case, an
expected exception, a property of a concrete class) is a **spec-local law**, added with `local` from
`kosmos-testkit`:

```kotlin
lawTests(lawGraph("ℚ") {
    suite(OrderedFieldLaws(q, RationalRelations.RationalTotalOrder, arb, eq, pr))
    branch("rounding") {
        local("floor(r) ≤ r < floor(r) + 1") {
            lawCheckAll(arb) { r ->
                val f = r.floor().toRational()
                lawCheck(f <= r && r < f + Rational.ONE) { "floor($r) = $f" }
            }
        }
    }
    local("zero has no reciprocal") {
        lawCheck(throwsA<ArithmeticException> { Rational.ZERO.reciprocal() }) { "no exception" }
    }
})
```

A local law has no binding, so it is never shared. Kotest matchers (`shouldBe`, `shouldThrow`) also work inside
one; their failures read as `FAILED`, but only `lawCheck` gives the failure a message of the law's own. A check
that more than one structure needs belongs in a reusable law class instead.

The aggregated form still works when one test per structure reads better:

```kotlin
test("Q is a field") {
    FieldLaws(field, arb, eq, pr).verify().throwIfFailed()
}
```

`verify()` works on any law or suite. `runLaws(...)` (fail-fast, no report) remains for a quick list of laws.

## Writing a law

A law signals that it is **false** with `lawCheck` and draws its inputs with `lawCheckAll`, and declares what it is
bound to so that equal checks can be shared:

```kotlin
override val binding = LawBinding.of(this, op, arb, eq)

override suspend fun test() {
    lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (a, b, c) ->
        lawCheck(eq(op(op(a, b), c), op(a, op(b, c)))) { "($a $s $b) $s $c ≠ $a $s ($b $s $c)" }
    }
}
```

- `binding` lists every argument that changes the check (not the printable, which only affects messages). A law
  without a binding is never shared with another: correct, but it runs again on every path that reaches it.
- `lawCheck(holds) { message }` throws a `LawViolation` when `holds` is false. The message is built only on failure.
  (Kotest's `withClue` does not attach its clue to exceptions it did not create, so the message goes here.)
- `lawCheckAll(gen, render)` is Kotest's `checkAll` with the same budget and shrinking, plus: under the runner it uses
  the leaf's seed, and it records the final (shrunk) failing input, rendered with the law's `Printable`.
- Anything else that is thrown (the operation, the generator, the equality) means the law could not be evaluated.

## Suites are graphs

A suite declares its children. Each suite is the **complete** contract of its structure: it composes the suites of
its parent structures, as the mathematics does, without trying to avoid their overlap.

```kotlin
class RingLaws<A : Any>(
    private val ring: Ring<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = suiteName("Ring", ring.add.op.symbol, ring.mul.op.symbol)
    override val binding = LawBinding.of(this, ring, arb, eq, pr)

    override fun children(): List<LawNode> =
        listOf(
            RngLaws(ring, arb, eq, pr),
            SemiringLaws(ring, arb, eq, pr),
            NonAssociativeRingLaws(ring, arb, eq, pr)
        ).asNodes()
}
```

- **No compact view.** There is one graph per suite. For example, a field's multiplicative associativity is reached
  at `Field > DivisionRing > Ring > Rng > Hemiring > Semigroup (*) > associativity (*)` and at eleven other paths,
  and runs once.
- **Atomic leaves.** The convention is one check per leaf. The compound laws are suites of their halves: `IdentityLaw` has
  `left identity` and `right identity`, `DistributivityLaw` has left and right distributivity, `MoufangLaw` has
  left, right and middle Moufang. A failure names the half that failed. A few laws still check more than one
condition in a single leaf (for example `PositiveDefiniteLaw`: non-negativity and non-degeneracy).
- **Inverses are required.** `InvertibilityLaw` has three children: `inverse exists` fails when the inverse returns
  null or throws on an element of the domain (the generator minus `exclude`, e.g. zero for a field's
  multiplication); left and right invertibility check `a⁻¹a = e` and `aa⁻¹ = e` where the inverse exists.
- **Paths.** Labels carry the operation roles, so a failure path says which operation is at fault.
- **Binding.** `binding` lists everything the checks depend on: every constructor argument (a suite's binding may
  include its printable; an atomic law's leaves it out, since it only affects messages). Always pass all of them.

## Shared checks run once

Bigger structures repeat smaller ones: an algebra over ℚ reaches the additive group of ℚ through its ring, its
module and its algebra. Within one run, a node reached again is not executed again; its result is shown under every
path, marked `sharedFrom` (the path where it ran).

Every built-in atomic law supplies a binding, so this works at the level of single checks, across the whole graph: two leaves are the
same check when they are the same node object, or laws of the same class whose bindings match. Suites are matched
the same way, so a repeated suite is shared as a whole. Bindings match when:

- every argument is the **same object** (same operation, generator, equality);
- plain values (numbers, strings, booleans, chars, enums) are equal;
- callable references (`shift::apply`) refer to the same function on the **same receiver object** (two receivers
  that are merely `equals` stay separate).

Labels, names and symbols never count, so two different operations both called `+` are two checks. A law with no
class of its own (`TestingLaw.named`) shares only if it is given a binding (`LawBinding.named(kind, args...)`).
`Eq.default()` and `Printable.default()` return one shared object, so suites built with default arguments share
their checks. A law that restricts its generator (the inverse laws drop `exclude`) builds the restricted generator
inside the check, so its binding is the original generator plus `exclude`. Nothing is reused across runs.

`LawReport.counts` counts occurrences (one per path); `LawReport.executions` counts what ran. For example, the
`StarAlgebra(ℚ over ℚ)` graph has 394 law occurrences and 39 executions.

## Outcomes

| Status | Meaning |
|---|---|
| `PASSED` | The law held on every sample. |
| `FAILED` | The law is false: a `LawViolation` (from `lawCheck`) was the final failure. |
| `ERRORED` | The law could not be evaluated: something else was thrown (e.g. the operation threw). |
| `SKIPPED` | Deliberately not executed, with a reason. |

A branch takes its worst child (errored > failed > passed > skipped). A run in which no law passed is not evidence,
and `throwIfFailed()` rejects it by default.

Two laws are deliberate exceptions, because for them "the operation throws" *is* the law being false:

- `TotalityLaw`: a throwing operation gives `totality` **Failed** and the other laws on it **Errored**.
- `InverseExistenceLaw`: an inverse that throws (or returns null) on the domain gives `inverse exists` **Failed**,
  with the exception kept as the cause. Left and right invertibility skip such elements.

Older laws that use plain `check` inside `checkAll` cannot tell the two apart (Kotest wraps every exception in an
`AssertionError`), so for them an `AssertionError` reads as `FAILED` and they have no seed.

## Reading a report

`report.renderFailures()` (also the message of `throwIfFailed()`) shows the failing paths and each problem's details:

```
Ring (+, *): ERRORED (passed 2, failed 1, errored 1, skipped 1; 3 executions; seed 1)
Ring (+, *)  ERRORED [2/5 passed]
├─ Hemiring (+, *)  FAILED [1/2 passed]
│  └─ Semigroup (*)  FAILED [0/1 passed]
│     └─ associativity (*)  FAILED
└─ AbelianGroup (+)  ERRORED [1/2 passed]
   └─ commutativity (+)  ERRORED

#1 FAILED: Ring (+, *) > Hemiring (+, *) > Semigroup (*) > associativity (*)
  counterexample: a = 1, b = 2, c = 3
  seed: 812... (replay: LawRunner.replay(root, listOf(0, 1, 0), 812...L))
  cause: org.vorpal.kosmos.laws.LawViolation: (a*b)*c ≠ a*(b*c)
```

Here multiplication is not associative (**Failed**), and the addition threw on some pair of inputs, so commutativity
of `+` could not be evaluated (**Errored**).

`report.renderTree()` shows the whole tree; a shared repeat is one line, `(shared: same check as <path>)`.
Pass `timing = true` to either for elapsed times.

## Seeds and replay

Each run has a master seed (`LawRunConfig(seed = ...)`, or random; recorded as `report.seed`). A leaf's seed is
derived from the master seed and the leaf's position, so the same master seed reproduces the same run.

To rerun one failing law with its seed, use the call printed in the report:

```kotlin
LawRunner.replay(root, indices = listOf(0, 1, 0), seed = 812...)
```

With the same code, generator, settings and library versions, it reproduces the same counterexample. A
deterministic check, or a failure before any sample was drawn, has no counterexample; the report says which fields
are missing and why (`LeafDiagnostics.unavailable`).

## Kotest integration (`kosmos-testkit`)

`lawTests(root)` / `lawTests(suite)` registers the graph in a `FunSpec`, at the root or inside a `context`.

- One `LawSession` per execution of the root container: shared checks run once, and a new run starts afresh.
- Running a single law with a Kotest filter executes it even if the other paths to the same shared check are
  filtered out. (Selecting a single law from the IntelliJ gutter has not been verified yet.)
- A retried test executes the law again rather than reusing the previous attempt.
- Repeated sibling labels get ` [2]`, ` [3]`, ... so every Kotest name is unique.

Selecting one law from Gradle uses Kotest's test filter (a regex over the test path; containers on the way must
match too):

```
kotest_filter_tests='.*Graph( -- Right( -- shared)?)?' ./gradlew :kosmos-testkit:test --tests 'org.vorpal.kosmos.testing.laws.KotestLawsSpec'
```

Gradle's own `--tests` filter selects specs, not nested tests.

## JSON export

`report.toJson()` (`LawReportJson`, in `kosmos-testkit`) writes a versioned document:

```json
{
  "schema": "kosmos-law-report", "version": 1,
  "root": "n0", "status": "FAILED", "seed": 42, "failFast": false,
  "counts": {"passed": 12, "failed": 1, "errored": 0, "skipped": 0}, "executions": 9,
  "nodes": [
    {"id": "n0", "kind": "branch", "label": "Field (+, *)", "path": ["Field (+, *)"], "indices": [],
     "status": "FAILED", "counts": {"passed": 12, "failed": 1, "errored": 0, "skipped": 0},
     "elapsedMillis": 120, "children": ["n1", "n9"], "sharedFrom": null, "outcome": null},
    {"id": "n5", "kind": "leaf", "label": "associativity (*)", "path": ["Field (+, *)", "..."], "indices": [0, 0, 1, 0],
     "status": "FAILED", "counts": {"passed": 0, "failed": 1, "errored": 0, "skipped": 0},
     "elapsedMillis": 30, "children": [], "sharedFrom": null,
     "outcome": {"status": "FAILED",
                 "exception": {"type": "java.lang.AssertionError", "message": "..."},
                 "cause": {"type": "org.vorpal.kosmos.laws.LawViolation", "message": "..."},
                 "seed": -123, "counterexample": "a = 1, b = 2, c = 3", "unavailable": [], "skipReason": null}}
  ]
}
```

- `nodes` lists every occurrence depth-first; `root`, `children` and `sharedFrom` refer to `id`s.
- `indices` + `outcome.seed` are what `LawRunner.replay` needs.
- Only types and messages of exceptions are exported; live throwables stay in the in-memory report.
- A change to the shape bumps `version`.

## Where things live

- `kosmos-laws/.../laws/`: `LawNode`, `LawBinding`, `LawRunner`, `LawSession`, `LawReport`, `LawReportText`,
  `LawExecution` (`lawCheck`, `lawCheckAll`), and the suites and property laws.
- `kosmos-testkit/.../testing/laws/`: `KotestLaws` (`lawTests`) and `LawReportJson`.
- Design notes and the history of this restructure: `docs/design/law-testing-restructure.md`.

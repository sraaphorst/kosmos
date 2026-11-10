# Kosmos law testing restructure

[Testing restructuring (#252)](https://github.com/sraaphorst/kosmos/issues/252) is the implementation brief. Its [design follow-up](https://github.com/sraaphorst/kosmos/issues/252#issuecomment-6076577794) calls for a directed acyclic graph (DAG) from the outset, separate definition, execution, and reporting layers, and separate identities for laws and executions. [More unified law testing / Law runner / Improved reporting (#131)](https://github.com/sraaphorst/kosmos/issues/131) adds the requirement to compose standalone laws and existing suites in the same spec. [Major testing refactor (#61)](https://github.com/sraaphorst/kosmos/issues/61) covers the subsequent cleanup of concrete specs.

Progress notes and departures from this plan are recorded at the end of this file.
The stage 0 baseline is in [`law-testing-baseline.md`](law-testing-baseline.md).

## Assignment

Implement #252 in the stages below. Use an isolated branch or worktree and leave a green, reviewable checkpoint after each stage. The result should let a contributor compose an existing suite and a standalone law, run the composition with one entry point, see the full path to each failing leaf, and replay an individual property failure. Keep existing suite constructors and test calls working throughout the migration.

Keep the mathematical predicates, generators, iteration budgets, equality policies, and existing domain-specific regressions intact during the structural refactor. Record suspected errors or missing axioms separately, with the current behavior and a reproducer. Keep registry integration, a redesign of `Symbols` or `Printable`, and a wholesale rewrite of specs in separate work.

## 0. Capture the baseline

- Record the starting commit and run `./gradlew :kosmos-laws:test :kosmos-testkit:test --stacktrace` on JDK 21. Record test counts, failures, and wall time. The current workflow runs the testkit task alone. Add the laws task and its failure artifacts once runner tests live in that module, preserving the existing check name.
- Inventory suite implementations and callers of `laws()`, `fullLaws()`, `test()`, `fullTest()`, `testAll()`, `fullTestAll()`, `flatten(full)`, and `runLaws`. Include `TestingLaw.named`, anonymous suites, and laws constructed inside property callbacks.
- Capture ordered law occurrences for representative suites in both modes. Audit which mode each child currently uses. For example, `FieldLaws.laws()` already includes `DivisionRingLaws.laws()`, and its full view delegates to the child's full view. The current compact view is therefore defined by each suite's existing behavior.
- Use `RationalFieldSpec` as the standard-suite pilot and `IntegerCommutativeRingSpec` as the mixed suite/standalone-law pilot. The latter already combines several suites with individual norm laws.

## 1. Add composition and execution

- In `kosmos-laws`, add a small sealed node model with leaves wrapping `TestingLaw` and branches holding ordered child nodes. Accept both shapes at the runner boundary. Keep `TestingLaw` a functional interface. Provide a compatibility adapter that can wrap the selected flat view of an existing suite as a branch.
- Represent shared children as references from the beginning. Keep execution results separate from definitions. Give each run its own state and validate cycles before evaluating leaves. Preserve traversal order and keep legacy flat APIs and fail-fast versus collecting behavior covered by compatibility tests.
- Return a structured report with paths, elapsed time, and leaf outcomes. Preserve original throwables in memory and propagate coroutine cancellation. Branches aggregate child outcomes and counts. Proposed precedence is errored, failed, passed, skipped. An all-skipped branch is skipped, and an empty report must be visible as providing zero evidence.
- Test an ad hoc branch containing both adapted suites and standalone laws, a failure deep in a branch, successful sibling execution after a failure, a cycle, and cancellation. Run existing specs through their original entry points.

## 2. Migrate suites to declare their hierarchy

- Start with `MagmaLaws`, `SemigroupLaws`, and `MonoidLaws`, then cover the group, semiring, ring, division ring, and field families. Use the actual suite decomposition and operation roles in path labels. A field's multiplicative associativity may sit beneath several constituent suites.
- Make the compact and full flat views derive from the corresponding graph traversal for migrated suites. Preserve existing order and occurrence counts in both flat views. Explicitly represent child-mode selection wherever the compact and full views differ. Keep the legacy adapter for implementations outside the migrated hierarchy.
- Compare the before and after law inventory in each batch. Verify that the full `FieldLaws` graph contains multiplicative associativity under `Hemiring > Semigroup(*)` and that the compact view retains its current coverage without that leaf. Run the controlled failing fixture in full mode and check that exactly that leaf and its ancestors fail. A real structure that violates several axioms should report every resulting failure.
- Migrate the other `LawSuite` families in package-sized batches, with targeted tests after each batch. Leave ordinary examples, edge cases, expected exceptions, and domain-specific properties in their specs. Where the reusable suites already cover an assertion, remove a duplicate only after checking equivalent domains, equality, and test budgets.

## 3. Capture diagnostics and make a leaf replayable

- Spike against the pinned Kotest 6.0.5 version using `AssociativityLaw`. Demonstrate an injected seed, capture of the final failing or shrunk input, and replay of the same leaf. Use supported Kotest interfaces and record the verified integration before migrating the other property laws.
- Mark a false predicate as `Failed` at the assertion boundary. Preserve Kotest shrinking and distinguish unexpected exceptions from the operation, generator, equality function, or test harness as `Errored`. Use a typed or otherwise explicit signal through the property runner. Existing laws that use generic `check` require particular care because the same exception type can be thrown by the operation itself. For opaque legacy leaves, document the compatibility classification and the metadata they can supply.
- Capture the rendered failing input with the existing `Printable`, the original cause, effective seed, and settings required to replay. Use a stable leaf selector and rerun that leaf through the same generator and configuration. A deterministic check or a failure before sampling can legitimately have no seed or input. State which fields are unavailable and why.
- Reserve `Skipped` for explicit non-execution with a reason. Keep existing reciprocal and other generator preconditions. Treat unexpected generator exhaustion as a visible error. Keep iteration telemetry optional until the pinned API spike verifies that it can be collected consistently.
- Test deterministic failure, shrunk property failure and replay, an arithmetic error, an error with an assertion-like exception type, cancellation, and explicit skip. Verify replay with the same code, generator, settings, and library versions.

## 4. Execute shared nodes once

- Separate a logical law identifier, a binding to a concrete test context, and the result of one execution. Reuse the same node explicitly where the suite construction knows the binding is identical. Display names, operation symbols, and law class names are labels and cannot establish that equivalence. Different subjects, operation roles, equality, arbitraries, preconditions, or run settings can require distinct checks.
- Cache by bound node identity within a run. Both parent paths should display the shared result. Report unique executions in the run total and label occurrence counts if exposed per path. Keep flat legacy traversal occurrence-based for compatibility.
- Test a diamond with a counted check, identical labels on separately bound checks, a shared failing leaf, independent subsequent runs, and different compact/full selections.

## 5. Render and integrate

- Add full and failing-path text renderers. Include operation roles, failure details, and original cause information in the structured report's `throwIfFailed()`. Test stable output with timing controlled or omitted from assertions.
- Add a versioned JSON export using report DTOs with node IDs and child references. Export exception type, message, and available replay metadata. Keep runtime lambdas and live throwables in the runner model. The JSON dependency is currently test-scoped in `kosmos-testkit`. Make any dependency change explicit and keep serialization at the export boundary.
- Add the nested Kotest adapter on the testkit side. Verify that Gradle and IDE views can run a whole root and select one leaf. Preserve the existing aggregated spec style. A full nested run should share executions within its root, and selecting a leaf should execute it even when another displayed path to the same node is filtered out. Test duplicate sibling display names and verify that cancellation or retries cannot leak a prior result into a new run.
- Migrate the two pilot specs. Preserve their non-law examples and assertions. Document the authoring pattern, compact/full behavior, sharing, replay, and JSON schema in `docs/03-kosmos-laws.md`. Keep a passing report usable as future `Lawful<T>` evidence with its scope and run settings. Registry wiring belongs to separate work.

## Delivery checks

- Both Gradle test tasks pass from a clean checkout, and CI runs both. The first infrastructure checkpoint passes existing specs without editing them.
- Migrated suites retain their ordered compact and full coverage. The runner tests verify failed paths, distinct error states, cancellation, seed replay, and one execution of a shared leaf.
- Text and JSON renderings and nested Kotest discovery have regression coverage. Record how single-leaf selection was verified and any IDE-specific limitations.
- Summarize the checkpoints, tests run, baseline differences, suites migrated, and any remaining #61 spec-cleanup batches. Flag departures from #252 explicitly, especially any replay or nested-adapter requirement the pinned Kotest version leaves unresolved.

## Progress and departures

### Stage 0 (done)

See [`law-testing-baseline.md`](law-testing-baseline.md). Departure from #252: its acceptance example
("a field whose multiplication is not associative fails only at root → multiplicative monoid → associativity")
holds only for the **full** view, and the path runs through `Semigroup(*)`, because the compact `FieldLaws`
view does not include multiplicative associativity at all. Whether the compact view *should* include it is a
coverage question for Sebastian, not something the refactor changes on its own.

### Stage 1 (done)

New in `kosmos-laws` (package `org.vorpal.kosmos.laws`):

- `LawNode.kt`: `LawMode { COMPACT, FULL }`; sealed `LawNode` with `Leaf(law)` and `Branch(label, children)`;
  `LawNode.flat(...)` compatibility adapter; occurrence-based `flatten()`; `LawGraphs.requireAcyclic` (reference identity,
  so a shared node is fine and a node that is its own descendant is rejected with `LawGraphCycleException`).
- `LawReport.kt`: `LawStatus` (ERRORED > FAILED > PASSED > SKIPPED), `LeafOutcome` (keeps the original throwable),
  `LawCounts`, `LawPath` (labels for display, child indices as the stable selector), `NodeReport`, and `LawReport`
  with `problems`, `hasEvidence` and a path-based `throwIfFailed(requireEvidence = true)`.
- `LawRunner.kt`: `LawRunner.run(node, config)` with fresh state per run, cycle check before any leaf runs,
  collecting by default and `failFast` as an option (later leaves become `Skipped` with a reason), cancellation
  rethrown. Entry points: `node.verify()`, `suite.verify(mode)`, and the `lawGraph("...") { suite(...); law(...) }` builder.
- `LawSuite.node(mode)`: defaults to the flat adapter, so every existing suite already works as a node.

Interim classification: `AssertionError` is `Failed`, anything else is `Errored`. Kotest's `checkAll` turns every
exception in a property body into an `AssertionError`, so a throwing operation inside a property still reads as
`Failed` until stage 3.

Tests: `kosmos-laws/src/test/.../LawRunnerSpec.kt` (16 tests). CI now runs `:kosmos-laws:test` too, under the same
`testkit` check name. Existing specs are untouched: 143 specs / 2241 tests pass (baseline + the inventory spec).

### Stage 2 (done)

`StructuredLawSuite` (in `kosmos-laws`): a suite implements `children(mode): List<LawNode>`, and `laws()` /
`fullLaws()` are the flattenings of `node(COMPACT)` / `node(FULL)`, so the graph and the flat views cannot drift.
Child-mode selection is explicit in each `children`: most suites pass `mode` through; where the views differ
(e.g. `HemiringLaws`, `NonAssociativeRingLaws`, modules, vector spaces, `NonAssociativeAlgebraLaws`,
`MeadowLaws`, `InnerProductSpaceLaws`) the code uses `when (mode)` and names the child's mode.

All 53 suite classes are migrated, so the flat adapter in `LawSuite.node` is now only used by suites written
outside `kosmos-laws` (none today) and by ad hoc test fixtures. No law, predicate, generator, equality or
budget changed.

Coverage checks:

- Golden snapshot (`LawInventorySpec`): 50 suites x 2 views, identical before and after.
- Whole-suite differential: `test()` / `fullTest()` were temporarily instrumented to dump every suite's ordered law
  list while the full testkit ran, once on the stage 1 commit and once after migration. All 378 suite invocations
  in the existing specs (26 suite types, real instances) produced identical lists. The instrumentation was not
  committed.
- `FieldLawGraphSpec`: a controlled "field" whose multiplication is non-associative (only when the left factor is
  itself a product, so no other field law can see it) fails exactly
  `Field > DivisionRing > Ring > Rng > Hemiring > Semigroup(*) > associativity (*)` in full mode, every ancestor
  fails and every other branch passes; the compact view passes, as recorded in the baseline. `(ℚ, −, *)` reports all
  four additive failures (associativity, identity, commutativity, invertibility of `+`).

Suites with no spec that exercises them (checked only by the snapshot): the lattice family and `BooleanAlgebraLaws`,
`NonAssociativeDivisionAlgebraLaws`, `RngHomomorphismLaws`, `NonAssociativeRngHomomorphismLaws`,
`EquivalenceRelationLaws`, `SetoidLaws`. `InnerProductSpaceLaws` has no instance at all, so it is checked only by
compiling. These are candidates for #61.

### Stage 3 spike (Kotest 6.0.5, `AssociativityLaw`)

Verified with `kosmos-laws/src/test/.../PropertyDiagnosticsSpec.kt`, using only public Kotest API
(`PropTestConfig(seed = ...)` and the property body):

| Question | Result |
|---|---|
| Seed in | Works. The runner draws one seed per leaf from a master seed (`LawRunConfig.seed`) and installs it in the coroutine context; `lawCheckAll` passes it as `PropTestConfig(seed = ...)`. Same master seed, same leaf seeds. |
| Seed out | We never need to parse it: the runner chose it. Kotest's message ("Repeat this test by using seed N") agrees. |
| Counterexample out | Works without parsing. `lawCheckAll` records the input of every failing evaluation, rendered with the law's `Printable`. Kotest's shrinker adopts each failing candidate in turn, so the last failing evaluation is the reported shrunk value. The test checks it equals Kotest's "Arg 0" after shrinking. |
| Replay | Works. `LawRunner.replay(root, path.indices, seed)` reruns one leaf and yields the same counterexample. |
| False vs threw | Works for migrated laws. A false predicate throws `LawViolation` (via `lawCheck`); Kotest wraps it, and the runner looks at the final failure's own throwable, which `lawCheckAll` records before Kotest wraps it. `LawViolation` is `Failed`; anything else (including an `AssertionError` thrown by the operation) is `Errored`. |

Findings that shape the design:

- **Kotest catches `CancellationException` inside `checkAll`** and wraps it as an `AssertionError`. The runner checks
  `ensureActive()` and the cause chain after every leaf so cancellation is never recorded as a result.
- **kotlinx.coroutines may copy an exception that crosses `withContext`** (stack-trace recovery). The runner catches
  inside the block so the report keeps the original throwable instance.
- **`Arb.filter { false }` kept retrying for over 10 minutes** in Kotest 6.0.5 (stack in `filter.kt`, no give-up), and the
  loop does not suspend, so a timeout cannot stop it. Unexpected generator exhaustion is therefore *not* detectable as an
  error yet; a generator that throws is reported as `Errored` with a seed and no counterexample.
- **Kotest writes failing seeds to `~/.kotest/seeds`** (`PropertyTesting.writeFailedSeed`, default on) and reuses them
  when no seed is given. `lawCheckAll` always passes an explicit seed under the runner, so a previous run's seed cannot
  leak into a new run. Legacy entry points keep Kotest's behaviour.
- **Iteration telemetry**: `PropertyContext` does not expose attempts publicly in 6.0.5, so it stays out for now.
- **Legacy laws** (`check` inside plain `checkAll`) keep the compatibility classification: `AssertionError` is `Failed`,
  so a throwing operation inside a legacy property still reads as `Failed`, and they have no seed. Migrating a law means:
  `checkAll(arb)` → `lawCheckAll(arb, render)`, and `withClue(msg) { check(p) }` → `lawCheck(p, msg)`. The predicate,
  generator, equality and default budget (1000 iterations, bounded shrinking) are unchanged.
- **Explicit skip**: `LawNode.Leaf(law, skip = "reason")` / `lawGraph { skipped(law, reason) }`.

### Stage 3 (done)

Every property law in `kosmos-laws` now uses the explicit signal (60 files):

- `checkAll(...)` → `lawCheckAll(...)` (one-, two- and three-generator forms). Same generators, same default budget.
- `withClue(message) { check(p) }` → `lawCheck(p) { message }`. Kotest's `withClue` does not attach its clue to an
  exception it did not create, so the message moved into `lawCheck`; most messages are now built lazily (only on failure).
- `TotalityLaw` now throws `LawViolation(message, cause)`: for totality, the operation throwing *is* the law being false.
  In a suite run, an operation that throws therefore shows up as `totality` **Failed** and the other laws on that
  operation as **Errored** (see `SuiteDiagnosticsSpec`).
- Counterexamples are printed with the law's `Printable` where the sample type matches one in scope
  (`render = renderWith(pr)`, 59 call sites); elsewhere (mixed-type tuples such as `(scalar, vector)`) they fall back to
  `toString`, element by element.
- Message format change for legacy specs: a failure message is now the law's own message, without Kotest's
  trailing "Check failed.".

Unchanged: all predicates, generators, equalities, budgets; the law inventory snapshot; all 144 specs / 2246 tests.
Not done: iteration telemetry (no public API in Kotest 6.0.5); detecting generator exhaustion (Kotest's filter hangs).

### Stage 4 (done)

Within one run, a node that is reached again executes once and its result is shown under every parent.

- **What "the same" means.** Each suite declares a `LawBinding`: its class plus every constructor argument
  (`override val binding = LawBinding.of(this, ring, arb, eq, pr)`, all 53 suites). Arguments compare by
  **reference identity** (same operation object, generator, equality, printable); plain values (numbers, strings,
  booleans, enums such as `tolerance` or `includeNormLaw`) compare by value. Names, symbols and labels never take
  part, so two different operations both called `+` are two checks. `node(mode)` carries the key `(binding, mode)`,
  so compact and full views are different nodes. A node without a key is shared only with itself (same instance).
- **Runner.** A per-run cache from key (or node instance) to its first report. A repeat is not executed: the report is
  copied to the new path, recursively, with `sharedFrom` pointing at the original path and zero elapsed time. Nothing
  is cached across runs.
- **Report.** `counts` still counts occurrences (one per path, like `laws()` / `fullLaws()`); the new
  `LawReport.executions` counts leaves that actually ran. A shared failing leaf appears in `problems` under every path;
  `throwIfFailed` prints the repeat as "same check as <original path>".
- **Effect.** `StarAlgebra(Q over Q)` full: 52 occurrences, 32 executions (shared: `Monoid(+)` reached through
  `AbelianGroup` and `CommutativeMonoid`, `Magma(*)` through `NonAssociativeMonoid` and `Semigroup`, the scalar
  `CommutativeRing` inside `RModule`, the module's `AbelianGroup(+)`). Sharing happens only when the arguments really
  are the same objects; for example `RModule(Q over Z)` keeps both additive groups (ℤ and ℚ), even though both are
  labelled `(+)`.
- **Coverage.** Unchanged: the law inventory snapshot and `laws()` / `fullLaws()` are occurrence-based and untouched.
  Sharing can also join sub-suites that ignore an argument: `MagmaLaws` (totality) takes no equality, so two `Monoid`
  suites differing only in equality share their totality check, which is the same check.
- Tests: `SharedNodeSpec` (diamond with a counting law, failing shared leaf, re-pathed shared branch, identical labels
  bound separately, same/different suite arguments, compact vs full, binding equality, separate runs, failFast) and
  `SharedSuiteSpec` (StarAlgebra over ℚ). 4 + 145 specs / 44 + 2249 tests green.

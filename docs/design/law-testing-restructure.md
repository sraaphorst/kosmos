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

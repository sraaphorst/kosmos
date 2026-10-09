# Hypercomplex Review and Completion Report

**Date:** 2026-10-08
**Branch:** `K249`
**Scope:** the `algebra` package of `kosmos-core`, the `hypercomplex` package of `kosmos-core`, and
their tests in `kosmos-testkit`.

## Summary

| | |
|---|---|
| New algebras | split-quaternions, split-octonions, biquaternions, bioctonions |
| Bugs fixed | 5 (4 reciprocal-contract violations, 1 wrong signature) |
| New tests | 178, in 13 new spec files and 2 extended ones |
| Final result | `./gradlew clean build`: **1771 tests, 0 failures** |
| Tessarines | Already present: they are `Bicomplex` (same algebra, different basis) |

The existing hypercomplex code was in good shape. Everything is carried by the Cayley–Dickson pair
and nothing re-implements a product, so the four new algebras could be added without writing any
new multiplication: each is the existing kernel with a different sign or a different base.

---

## 1. Review of the `algebra` package

### What was read

- `algebra/structures`: every file, line by line.
- `algebra/quadratic`: read in full.
- `algebra/morphisms`, `algebra/products`, `algebra/poly`, `algebra/free`: **skimmed only**. These
  are largely uniform boilerplate (one `of` factory and `andThen`/`compose` per morphism kind); I
  checked the ones the hypercomplex code uses and did not audit the rest line by line.

### Bugs found and fixed

**1–4. Four base fields violated the `HasReciprocal` contract.** The contract (documented on
`HasReciprocal`) says `hasReciprocal` is the single source of truth and `reciprocal` throws
`ArithmeticException` exactly where it returns `false`. The hypercomplex algebras all complied, but
the fields underneath them did not:

| Field | Problem | Fix |
|-------|---------|-----|
| `RealField` | `reciprocal` threw `IllegalArgumentException`; `hasReciprocal(NaN)`, `hasReciprocal(∞)` and `hasReciprocal(1e-300)` returned `true` although `reciprocal` rejected them | overrode `hasReciprocal` with the floating-point predicate; throws `ArithmeticException` |
| `RationalField` | `reciprocal(0)` threw `IllegalArgumentException` | guards with `hasReciprocal`; throws `ArithmeticException` |
| `GaussianRatField` | same | same |
| `PrimeField` | `reciprocal` threw `IllegalArgumentException`; `hasReciprocal(p)` returned `true` although `p ≡ 0`; the operator symbol was `/` rather than `⁻¹` | `hasReciprocal` now tests the canonical representative; throws `ArithmeticException` |

This mattered beyond tidiness: `splitQuaternionMatrixIsomorphism` asks a field whether 2 is
invertible, and in 𝔽₂ the old `PrimeField` would have answered "yes" for `1 + 1`.

**5. `InnerProductSpace.sharp` had the wrong parameter type.** It took `(F) -> V`; a covector is
`(V) -> F`. Nothing overrides it, so the fix is safe.

### Behaviour changes you should know about

- Code that caught `IllegalArgumentException` from `RealField`, `RationalField`, `GaussianRatField`
  or `PrimeField` reciprocals must now catch `ArithmeticException`. One existing test
  (`RationalFieldSpec`) asserted the old exception and was updated.
- `Rational.reciprocal()` itself (the value-level method, not the field) is unchanged and still
  throws `IllegalArgumentException`.

### Additions

- `CayleyDickson.associativeSplit(base)`: the split (`σ = −1`) doubling of a commutative base, typed
  as an associative `InvolutiveRing`. It is to `split` what `associative` is to `usual`. Both now
  share one private implementation.
- `CarrierTransport.nonAssociativeInvolutiveRing(ring)`: needed to transport the octonion-level
  rings onto value classes.
- Documentation: the norm formula `N(a, b) = N(a) + σ·N(b)` on `CayleyDickson`; KDoc for `usual`
  and `split`; a dangling sentence in `Meadow`; `FiniteVectorSpace` (said "non-zero" dimension but
  allows 0).
- `algebra/structures/README.md` was stale: it listed `InvolutiveAlgebra` and
  `NonAssociativeSemigroup`, which no longer exist, and the old `CayleyDickson` signature. It is now
  regenerated from the actual declarations. The PNG diagram it links to was **not** regenerated.

### Noticed but left alone

- `Monoid.pow` performs one squaring more than necessary on the last step. Harmless.
- `IntegralDomain` is a placeholder; `Semimodule` and `Semialgebra` have `TODO: write laws`;
  `InnerProductSpace` has a `TODO` about the sesquilinear (complex) case.

---

## 2. Review of the existing `hypercomplex` code

**Verdict: sound and consistent.** I found no mathematical errors in ℂ, the split-complex, dual and
bicomplex numbers, or the quaternion and octonion tiers. The round-2 design (one `QuaternionTier`
and one `OctonionTier` shared by three coefficient rings; value classes where carriers collide;
`…Approx` conversions typed as plain `UnaryOp`s) holds up, and I built on it rather than around it.

The only changes to existing hypercomplex files are to the two tiers, which gained a `split` flag
and a factory:

- `QuaternionTier(…, split = false)` and `QuaternionTier.over(base, split)`: the quaternion tier
  over an arbitrary commutative ring.
- `OctonionTier(quaternions, split = false)`, with `normSq = N(a) + σ·N(b)`.

Default arguments keep every existing call site unchanged.

### Observations, not changed

- `Symbols.HYPERBOLIC_J` and `Symbols.IMAGINARY_J` are both `"J"`, so a split-complex `j` and a
  quaternion `j` print identically. The split-quaternions reuse the `I, J, K` labels too.
- `OctonionEmbeddingKit` returns `NonAssociativeRingHomomorphism`s although the embeddings are
  injective; the type could be tightened to `…Monomorphism`.

---

## 3. The four new algebras

| Algebra | Type | Construction | Package |
|---------|------|--------------|---------|
| Split-quaternions | `SplitQuaternion<R>` | `CD_split⟨R[i]⟩` | `hypercomplex.splitquaternion` |
| Split-octonions | `SplitOctonion<R>` | `CD_split⟨ℍ_R⟩` | `hypercomplex.splitoctonion` |
| Biquaternions | `Biquaternion` | `CD⟨ℂ[i]⟩ = CD⟨𝔹⟩` | `hypercomplex.biquaternion` |
| Bioctonions | `Bioctonion` | `CD⟨ℍ ⊗ ℂ⟩` | `hypercomplex.bioctonion` |

### Design decisions (defaults I picked; easy to change)

1. **The split forms are generic over a commutative ring `R`**, mirroring `SplitComplex<R>`. One
   implementation gives the algebras over ℝ, ℚ, ℤ and finite fields, and lets the tests check every
   law exactly over ℚ. The alternative was three named tiers per algebra, as ℍ has.
2. **All four are value classes**, because their carriers collide with existing ones
   (`CD<Complex>` is shared by `Quaternion`, `Bicomplex` and `SplitQuaternion<Real>`).
3. **Split-quaternion convention:** `i² = −1`, `j² = k² = +1`, `ij = k`. This is what the
   Cayley–Dickson split doubling of ℂ produces, and it makes `ℂ ↪ ℍ′` the canonical embedding.
4. **Split-octonions are `CD_split(ℍ)`**, not `CD_usual(ℍ′)`. The two are isomorphic; this one makes
   `ℍ ↪ 𝕆′` canonical.
5. **Biquaternions and bioctonions have floating-point complex coefficients** (`Complex`), since
   that is the only complex field with a public carrier convenient for it. The construction is
   checked exactly anyway; see section 4.
6. **Three conjugations** are exposed on the complexified algebras (`conj`, `complexConj`,
   `hermitianConj`), with the coefficient field's imaginary unit named `h` in the documentation to
   keep it apart from `i, j, k`.

### What each provides

- **Split-quaternions:** ring; with-reciprocal ring over a field; basis; norm; scalar action;
  module, algebra and star algebra; real ring, star algebra and vector space; embeddings of `R`, ℂ
  and the split-complex numbers (along `j` and along `k`); `↪ M₂(R)` and the isomorphism
  `≅ M₂(F)` with its inverse.
- **Split-octonions:** nonassociative ring; with-reciprocal ring over a field; basis; norm; scalar
  action; module, nonassociative algebra and star algebra; real algebra, star algebra and vector
  space; embeddings of `R`, ℍ and ℍ′.
- **Biquaternions:** ring with norm and reciprocal; units `i, j, k, h`; three conjugations; vector
  spaces over ℂ (dim 4) and ℝ (dim 8); star algebras over ℂ and ℝ; embeddings of ℂ, ℍ, ℍ′ and 𝔹;
  the isomorphism `≅ M₂(ℂ)` with its inverse.
- **Bioctonions:** nonassociative ring with norm and reciprocal; basis and `h`; three conjugations;
  vector spaces over ℂ (dim 8) and ℝ (dim 16); star algebras over ℂ and ℝ; embeddings of ℂ, 𝕆, 𝕆′
  and ℍ ⊗ ℂ; the 168 (84 normalized) embeddings `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`.

Every reciprocal follows the `HasReciprocal` contract, including the floating-point policy.

---

## 4. Tests

**Before:** 1593 tests. **After:** 1771 tests, all passing on a clean build.

### New spec files

| Spec | Tests | What it establishes |
|------|------:|---------------------|
| `SplitQuaternionRingSpec` | 19 | Exact over ℚ and ℤ: involutive-ring laws, unit relations, norm formula and multiplicativity, zero divisors, nilpotents, idempotents, reciprocal, module and star-algebra laws |
| `SplitQuaternionMatrixSpec` | 9 | `≅ M₂(ℚ)` both ways, norm = determinant, not surjective over ℤ, refused over 𝔽₂ |
| `RealSplitQuaternionSpec` | 7 | Real star algebra and vector space; embeddings of ℂ, split-complex, scalars |
| `SplitOctonionSpec` | 20 | Exact over ℚ: alternativity, flexibility, Moufang, nonassociativity, signature (4, 4), composition, zero divisors, inverse property, embeddings |
| `RealSplitOctonionSpec` | 8 | Real star algebra and vector space; `ℍ ↪ 𝕆′` |
| `BiquaternionSpec` | 16 | Ring and star-algebra laws, `h` central, three conjugations, norm, zero divisors, vector spaces |
| `BiquaternionMorphismSpec` | 10 | Embeddings of ℂ, ℍ, ℍ′, 𝔹; `≅ M₂(ℂ)` both ways; Hermitian conjugate = conjugate transpose |
| `BioctonionSpec` | 19 | Alternativity, flexibility, Moufang, nonassociativity, `h` central and nuclear, composition, zero divisors |
| `BioctonionMorphismSpec` | 7 | Embeddings of ℂ, 𝕆, 𝕆′, ℍ ⊗ ℂ; all 168 and 84 embeddings, exhaustively on basis units |
| `ComplexifiedTowerSpec` | 8 | The biquaternion and bioctonion construction over ℚ(i), **exactly** |
| `ComplexLikeEmbeddingSpec` | 9 | All six members of every ×6 family (ℤ[i]→Lip, ℤ[i]→𝓗, ℤ[i]→ℍ_ℚ, ℚ(i)→ℍ_ℚ, ℂ→ℍ); the six left ℂ-vector-space structures on ℍ |
| `TowerConveniencesSpec` | 7 | `Complex.powInt`, `asQuaternion`, `asOctonion`, `hyperbolicUnit` |
| `CarrierTransportSpec` | 6 | Direct tests of `CarrierTransport` |
| `PrimeFieldSpec` | 22 | Field laws for 𝔽₂, 𝔽₃, 𝔽₇, 𝔽₁₀₁ and the reciprocal contract |

### Extended spec files

- `CayleyDicksonSpec` (+5): `split`, `associativeSplit`, the split octonion-like level, and the norm
  formula for four values of `σ`.
- `ReciprocalPolicySpec` (+6): ℝ; ℚ and ℚ(i); and the four new algebras at the floating-point
  boundary and on the null cone.

### Coverage gaps closed in the existing code

Several public functions had no test referencing them at all before this work: the ×6 embedding
families for ℤ[i] and ℚ(i), `QuaternionLeftComplexVectorSpacesAll`, `Complex.powInt`,
`asQuaternion`, `asOctonion`, `hyperbolicUnit`, `CayleyDickson.split`, `CarrierTransport` (tested
only indirectly) and `PrimeField`. All are now covered.

### A note on exactness

Floating-point specs use approximate equality, which can hide a wrong formula whose error is small
on small inputs. Wherever possible the new tests therefore run over ℚ or ℚ(i) with strict equality.
For the biquaternions and bioctonions, whose public carriers are floating-point, the public objects
are tested approximately and the *construction* is tested exactly in `ComplexifiedTowerSpec`.

---

## 5. Documentation

`hypercomplex/README.md` was reorganized and extended (567 → about 910 lines):

- **New: "Map of the Number Systems"**: a family tree showing which doubling produces which
  algebra; a properties table (dimension, commutative / associative / alternative / division, norm,
  isomorphism type); the real forms of the complexifications; sub-algebra chains; matrix
  isomorphisms; and a glossary of alternative names (tessarines, coquaternions, and so on).
- **New: "Split Forms" and "Complexifications"** sections with usage examples, including the three
  conjugations.
- **New: "What Remains to Be Done"**, in three groups: more algebras from the same kernel
  (sedenions, generalized quaternion algebras, dual quaternions, split-biquaternions, multicomplex
  numbers, Clifford algebras with the identifications they would give); gaps in the existing
  algebras; and structure and laws (no `AlternativeAlgebra` or `CompositionAlgebra` type yet).
- Updated: the construction tables, carrier-collision table, package layout, canonical-embedding
  table, scalar-action table and quick reference.

Every new public declaration has KDoc.

---

## 6. Things to be aware of

1. **Your two uncommitted edits are in my commit.** `K249` had uncommitted changes in
   `CarrierTransport.kt` (KDoc notes) and `CayleyDicksonSpec.kt` (a reformat). I needed to edit both
   files, and could not separate the hunks, so they are included unchanged.
2. **Incremental compilation went stale once.** After adding files to `kosmos-testkit`, Gradle
   reported thousands of "unresolved reference" errors for top-level functions that had not
   changed; the generated `kosmos-testkit.kotlin_module` had been truncated. `./gradlew
   :kosmos-testkit:clean` fixed it. This is a Kotlin incremental-compilation problem, not a problem
   in the code, and the final verification was a full `clean build`.
3. **Nothing is pushed.** All work is in local commits on `K249`.

## 7. Open questions

1. Are you happy with the split forms being generic (`SplitQuaternion<R>`), or would you prefer
   named tiers (`RationalSplitQuaternion`, …) like the quaternions have?
2. Is `i² = −1, j² = k² = +1` the split-quaternion convention you want?
3. Should the biquaternions and bioctonions get public exact tiers over ℚ(i)?
4. Should I push the branch and open a pull request against `K249` or `main`?

---

## Addendum: second pass (2026-10-08, later)

Sections 1 to 7 describe the first pass. This addendum covers the follow-up work, and supersedes
them where they differ: in particular sections 2 and 3 describe typealias carriers and raw tiers
that no longer exist.

**Result:** `./gradlew clean build`: **1817 tests, 0 failures.**

### A. Octonion combinatorics (new `OctonionCombinatoricsSpec`, 17 tests)

All checked exhaustively and exactly, from the products of the basis units of `𝕆_ℚ`:

| Claim | Verified |
|-------|----------|
| 35 unordered triples of distinct units = 7 Fano lines + 28 non-lines | ✓ |
| An ordered triple associates iff it is collinear: 42 do, 168 do not | ✓ |
| The 168 anti-associate, and triples with a repeated unit all associate | ✓ |
| Each Fano line spans a quaternion subalgebra (a copy of Q₈) | ✓ |
| The associator is alternating (all three transpositions, random rationals) | ✓ |
| Reversal pairs the 168 into 84 pairs with opposite associators | ✓ |
| Exactly 21 permutations preserve the table, forming C₇ ⋊ C₃ | ✓ (element orders 1, 3, 7 with counts 1, 14, 6; normal C₇; nonabelian) |
| 168 collineations of the unoriented plane; 1344 signed automorphisms | ✓ |
| 2⁷ · 7! = 645,120 signed permutations give exactly 480 tables | ✓ |
| 480 = 30 Fano planes × 16 orientations | ✓ |

One refinement to the statement you quoted: on a nonassociative basis triple the associator is
exactly `±2·e_m`, where `m` is the single point of the Fano plane not on a line through two of the
three indices. Zorn vector-matrices were **not** implemented.

### B. Generic `Quaternion<R>` and `Octonion<R>`

- `Quaternion<R>` and `Octonion<R>` are value classes over the Cayley-Dickson pair, mirroring
  `SplitQuaternion<R>`. `RealQuaternion`, `RationalQuaternion`, `LipschitzQuaternion`,
  `RealOctonion`, `RationalOctonion` and `GravesianOctonion` are typealiases of them.
- **Source-incompatible:** the bare names `Quaternion` and `Octonion` now need a type argument.
  Use `RealQuaternion` / `RealOctonion`. The helper constructors `rationalQuaternion`,
  `lipschitzQuaternion`, `rationalOctonion` and `gravesianOctonion` are gone; `quaternion(…)` and
  `octonion(…)` are generic. Coordinates are members (`q.w`), and the raw pair is `q.cd`.
- `QuaternionAlgebras` and `OctonionAlgebras` gained generic constructions over any commutative
  ring: ring, ring-with-reciprocal over a field, basis, norm, scalar action, module, algebra, star
  algebra, scalar embedding, the canonical `Quaternion<R> ↪ Octonion<R>`, `Eq` and printable.
- `GaussianInt` and `GaussianRat` are value classes over `CD<BigInteger>` and `CD<Rational>`. Their
  `add`, `mul` and `conj` now come from `CayleyDickson.commutative` instead of hand-written
  formulas. `GaussianRat(re, im)`, `.re`, `.im` and destructuring still work. `GaussianInt.I` is new.
- `QuaternionTier` and `OctonionTier` were rewritten to deliver everything on the public carrier.
  This removed the per-algebra transport code from all six `*Algebras` families, including the four
  written in the first pass.

**My view on the design question.** Value classes are the right call, for the reason you gave and
one more: with typealiases, `Quaternion<Real>`, `SplitQuaternion<Real>` and `Bicomplex` would be the
same type, and the compiler would accept a split-quaternion where a quaternion is expected. The
cost is that the canonical embedding is no longer the bare `CayleyDickson.canonicalEmbedding`; the
tiers now supply it. Wrapping the Gaussian types is consistent with that and let their arithmetic
come from the kernel too.

**The inconsistency that remains:** `Complex` is still `typealias Complex = CD<Real>`. The
consistent end state is a generic `Complex<R>` with `GaussianInt = Complex<BigInteger>` and
`GaussianRat = Complex<Rational>`. I did not do it because `Complex` is used well beyond this
package (linear algebra, analysis), so it deserves its own decision.

### C. Exceptions

Rule now applied throughout: **dividing by zero or inverting a non-unit throws
`ArithmeticException`** (as the JVM's own `BigInteger` does); a malformed argument throws
`IllegalArgumentException`. Changed to match:

| Site | Was |
|------|-----|
| `Rational.reciprocal()`, `r / 0`, `Rational.of(n, 0)` | `IllegalArgumentException` |
| `DualRationalFns.inv`, `DualRationalFns.mobius`, `DualRealFns.inv` | `IllegalArgumentException` |
| `ExtendedGcd.modInverse()` (and `Int`/`Long.modInverse`) | `IllegalArgumentException` |
| `ExactDivOps` (division by zero, and inexact division) | `IllegalArgumentException` |
| `GaussianInt` and `EisensteinInt` `divRem` by zero | `IllegalArgumentException` |
| `DenseMatAlgebras.hadamardReciprocal` on a zero entry | `IllegalArgumentException` |

This supersedes the note in section 1 that `Rational.reciprocal()` was left unchanged. Left as
`IllegalArgumentException` on purpose: `Rational % m` for a non-positive modulus (an argument
precondition), and membership checks of the Hadamard unit groups.

### D. Other fixes

- `Monoid.pow` no longer performs a wasted final squaring. The same flaw was in `Group.zTimes` and
  `Complex.powInt`; both are fixed, and `powInt` now delegates to `Monoid.pow`.
  `ExponentiationSpec` counts the operations: exactly `⌊log₂ n⌋ + popcount(n)` for `pow`.

### E. Files removed

- `RationalQuaternion.kt`, `LipschitzQuaternion.kt`, `RationalOctonion.kt`, `GravesianOctonion.kt`
  (folded into `Quaternion.kt` and `Octonion.kt`).
- The `LipschitzQuaternions` constants object (unused).
- `RationalAlgebras.QToGaussianRatMonomorphism` (an exact duplicate of
  `GaussianRatAlgebras.QToGaussianRatMonomorphism`, and untested).

**Not deleted:** the untracked `_to_delete/` directory at the repository root. It is not under
version control, so removing it cannot be undone; say the word and I will remove it.

### F. New and changed tests in this pass

| Spec | Tests | |
|------|------:|---|
| `OctonionCombinatoricsSpec` | 17 | section A |
| `GenericQuaternionOctonionSpec` | 17 | `Quaternion<𝔽₇>` and `Octonion<𝔽₇>` laws; zero divisors over 𝔽₇; agreement with the named ℚ structures; carriers that share a pair are distinct types |
| `GaussianCarrierSpec` | 6 | the wrappers, and the transported arithmetic against the textbook formulas |
| `ExponentiationSpec` | 6 | section D |

Several existing specs had exception types or carrier construction updated; no assertion was weakened.

### G. Open questions

1. Should `Complex` become a generic `Complex<R>` value class (see B)?
2. Should `_to_delete/` be removed?
3. The first-pass questions about the split-quaternion convention and exact biquaternion tiers
   still stand.

---

## Addendum 2: generic `Complex<R>` (2026-10-08, later still)

**Result:** `./gradlew clean build`: **1829 tests, 0 failures.**

- `Complex<R>` is now a value class over `CD<R>`, with `RealComplex = Complex<Real>` as the name
  for ℂ. This closes open question 1 of the first addendum: no carrier in the hypercomplex package
  is a typealias of `CD` any more.
- `GaussianInt` and `GaussianRat` are `Complex<BigInteger>` and `Complex<Rational>`. They were
  briefly separate value classes; a typealias matches how `LipschitzQuaternion` relates to
  `Quaternion<R>`.
- `ComplexAlgebras` gained generic constructions over any commutative ring: `complexRing`,
  `imaginaryUnit`, `normSq`, `complexLeftAction`, `complexModule`, `complexStarAlgebra`,
  `scalarEmbedding`, and `eqComplex` / `printableComplex` overloads. The Gaussian rings are built
  from the same internal `complexRingOver`.
- New `GenericComplexSpec` (12 tests): `Complex<𝔽₇>` is the field with 49 elements; `Complex<𝔽₅>`
  has zero divisors because `2² = −1`; agreement with `GaussianRatField` and `ComplexField`; and
  `Complex<Real>`, `SplitComplex<Real>` and `Dual<Real>` are distinct types on one raw carrier.

**Source-incompatible changes**

| Before | After |
|--------|-------|
| `Complex` (the type) | `RealComplex`, or `Complex<Real>` |
| `c.a`, `c.b`, `CD(re, im)` as a complex number | `c.re`, `c.im`, `complex(re, im)`; the raw pair is `c.cd` |
| `GaussianInt.ZERO`, `.ONE`, `.I` (and `GaussianRat.…`) | `GaussianInts.ZERO`, … (`GaussianRats.…`): a typealias cannot carry its own companion |
| `GaussianInt(…).toString()` | prints as `Complex(re=…, im=…)` |
| `Quaternion<Real>.cd.a` was a `Complex` | it is a raw `CD<Real>`; wrap it with `Complex(q.cd.a)` |

`_to_delete/` has been removed, as you asked.

**One consequence worth knowing.** Before this change the biquaternions shared their raw carrier
with the real octonions (`CD<CD<CD<Real>>>`). They no longer do, because a biquaternion's
coefficients are now `RealComplex` values rather than raw pairs. Nothing depended on the
coincidence.

---

## Addendum 3: generic `Bicomplex<R>`, `Biquaternion<R>`, `Bioctonion<R>` (2026-10-09)

**Result:** `./gradlew clean build`: **1851 tests, 0 failures.**

The three complexified algebras are now generic over a commutative base ring, matching every other
carrier in the package:

```text
Bicomplex(R)    = Complex(R)    ⊗_R Complex(R) ≅ Complex(Complex(R))        value class over CD<Complex<R>>
Biquaternion(R) = Quaternion(R) ⊗_R Complex(R) ≅ Quaternion(Complex(R))     value class over CD<CD<Complex<R>>>
Bioctonion(R)   = Octonion(R)   ⊗_R Complex(R) ≅ Octonion(Complex(R))       value class over CD<CD<CD<Complex<R>>>>
```

- `RealBicomplex`, `RealBiquaternion` and `RealBioctonion` are the typealiases for the classical
  algebras. All existing structures (`BicomplexCommutativeRing`, `BiquaternionRing`,
  `BioctonionAlgebra`, the star algebras, vector spaces and morphisms) are unchanged apart from
  their type names.
- Each `*Algebras` object gained a generic section: ring, basis, the central unit `h`, the
  `Complex(R)`-valued norm, the three conjugations, both scalar actions, ring-with-reciprocal over a
  field, module, star algebras, and the embeddings (from `Complex<R>`, `Quaternion<R>` or
  `Octonion<R>`, the split form, and the level below).
- `ComplexAlgebras.complexRingWithReciprocal(field)` is new: reciprocals in `Complex<F>` over any
  field. The biquaternion and bioctonion reciprocals are built on it.
- The tiers are now `biquaternionTier(base)` and `bioctonionTier(base)`: the quaternion and octonion
  tiers with scalars `Complex(R)`.

**New `GenericComplexifiedSpec` (22 tests).** Everything exactly over ℚ: ring laws, unit relations,
the conjugations, multiplicativity of the norms, zero divisors, reciprocals, modules, star algebras
and all nine embeddings; ring laws and the reciprocal contract over 𝔽₃; and agreement of the generic
constructions over ℝ with the named real structures. The biquaternions and bioctonions are therefore
no longer checked only approximately, which closes first-pass open question 3.

**Source-incompatible changes**

| Before | After |
|--------|-------|
| `Bicomplex`, `Biquaternion`, `Bioctonion` as bare types | `RealBicomplex`, `RealBiquaternion`, `RealBioctonion` (or `Bicomplex<Real>`, …) |
| `z.alpha`, `z.beta`, `z.idempotent()`, `z.isUnit()`, `z.isInNullCone()`, `z.isZeroDivisor()` as members of `Bicomplex` | extension members on `RealBicomplex` (same call syntax; needs an import outside the package) |

`Bicomplex.ofStandard`, `Bicomplex.ofIdempotent`, `Bicomplex.ZERO` and `Bicomplex.ONE` are unchanged.

**Left real-only**, and listed in the README under what remains: the idempotent decomposition and
null-cone predicates of the bicomplex numbers, the printable for complex coefficients, the matrix
isomorphism `ℍ ⊗ ℂ ≅ M₂(ℂ)`, and the 168 embeddings into the bioctonions.


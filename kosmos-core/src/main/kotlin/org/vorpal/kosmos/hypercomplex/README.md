# Hypercomplex Number Systems

This package implements the hypercomplex number systems of Kosmos: the classical tower of normed
division algebras, their split forms, their complexifications, the commutative quadratic extensions
of a ring, and the integer- and rational-coefficient subrings of the tower.

Every one of them is an instance of a single construction, Cayley–Dickson doubling.

**Contents**

1. [Map of the number systems](#map-of-the-number-systems)
2. [One construction, many algebras](#one-construction-many-algebras)
3. [Package layout](#package-layout)
4. [The division tower and its coefficient rings](#the-tower-diagram)
5. [Split forms](#split-forms)
6. [Complexifications](#complexifications)
7. Embedding families: [complex → quaternion (×6)](#embeddings-complex--quaternion-6),
   [quaternion → octonion (×168)](#embeddings-quaternion--octonion-168-or-84-normalized)
8. [Combinatorics of the octonion multiplication table](#combinatorics-of-the-octonion-multiplication-table)
9. [Quick reference](#quick-reference-monomorphisms-and-conversions)
10. [What remains to be done](#what-remains-to-be-done)

---

## Map of the Number Systems

### The family tree

Each arrow is one Cayley–Dickson doubling of the algebra above it. The label says which doubling:
`usual` (`σ = 1`, new unit squares to `−1`), `split` (`σ = −1`, new unit squares to `+1`), or
`σ = 0` (new unit squares to `0`); `triv` means the base is given the *trivial* involution, which
keeps the result commutative.

```
                                        ℝ
              ┌─────────────────────────┼──────────────────────────┐
         usual│                    split│                       σ=0│
              ▼                         ▼                          ▼
              ℂ                   split-complex                  dual
                                  R[j]/(j² − 1)               F[ε]/(ε²)
      ┌───────┼────────────────┐
 usual│  split│      usual,triv│
      ▼       ▼                ▼
      ℍ       ℍ′               𝔹  (bicomplex = tessarines)
      │   split-quaternions    │
 ┌────┴────┐                   │usual
 │usual    │split              ▼
 ▼         ▼                 ℍ ⊗ ℂ  (biquaternions)
 𝕆         𝕆′                  │
       split-octonions         │usual
                               ▼
                             𝕆 ⊗ ℂ  (bioctonions)
```

The left branch is the classical tower $\mathbb{R} \subset \mathbb{C} \subset \mathbb{H} \subset \mathbb{O}$,
where each level loses a property ($\mathbb{C}$ loses ordering, $\mathbb{H}$ commutativity,
$\mathbb{O}$ associativity) but keeps a multiplicative norm and so remains a division algebra.
The other branches trade the division property for zero divisors.

### Properties at a glance

| Algebra | dim / ℝ | Built as | Commutative | Associative | Alternative | Division | Norm | Isomorphic to |
|---------|:-------:|----------|:-----------:|:-----------:|:-----------:|:--------:|------|---------------|
| ℂ | 2 | `CD(ℝ)`, σ = 1 | ✓ | ✓ | ✓ | ✓ | $a^2 + b^2$ | |
| split-complex | 2 | `CD(ℝ)`, σ = −1 | ✓ | ✓ | ✓ | ✗ | $a^2 - b^2$ | ℝ ⊕ ℝ |
| dual | 2 | `CD(ℝ)`, σ = 0 | ✓ | ✓ | ✓ | ✗ | $a^2$ | |
| ℍ | 4 | `CD(ℂ)`, σ = 1 | ✗ | ✓ | ✓ | ✓ | $w^2 + x^2 + y^2 + z^2$ | |
| ℍ′ split-quaternions | 4 | `CD(ℂ)`, σ = −1 | ✗ | ✓ | ✓ | ✗ | $w^2 + x^2 - y^2 - z^2$ | M₂(ℝ) |
| 𝔹 bicomplex | 4 | `CD(ℂ)`, σ = 1, trivial involution | ✓ | ✓ | ✓ | ✗ | $z_1^2 + z_2^2 \in \mathbb{C}$ | ℂ ⊕ ℂ |
| 𝕆 | 8 | `CD(ℍ)`, σ = 1 | ✗ | ✗ | ✓ | ✓ | sum of 8 squares | |
| 𝕆′ split-octonions | 8 | `CD(ℍ)`, σ = −1 | ✗ | ✗ | ✓ | ✗ | signature (4, 4) | Zorn vector-matrices |
| ℍ ⊗ ℂ biquaternions | 8 | `CD(𝔹)`, σ = 1 | ✗ | ✓ | ✓ | ✗ | $w^2 + x^2 + y^2 + z^2 \in \mathbb{C}$ | M₂(ℂ) |
| 𝕆 ⊗ ℂ bioctonions | 16 | `CD(ℍ ⊗ ℂ)`, σ = 1 | ✗ | ✗ | ✓ | ✗ | sum of 8 squares in ℂ | |

Every norm in the table is multiplicative, $N(xy) = N(x)N(y)$, and all but the dual numbers (whose
norm is degenerate) are **composition algebras** over their scalars. Those marked "Division ✗" have
an isotropic norm, and the elements of norm zero are exactly the zero divisors (together with 0).

### How they relate

**Real forms.** An algebra over ℂ can contain several real subalgebras that each span it. The
quaternion and octonion algebras over ℂ have exactly two:

```
        ℍ ───────────┐                    𝕆 ───────────┐
   (real coefficients)▼               (real coefficients)▼
                    ℍ ⊗ ℂ ≅ M₂(ℂ)                       𝕆 ⊗ ℂ
   (j ↦ h·j, k ↦ h·k)▲              (e₄…e₇ ↦ h·e₄…h·e₇)▲
        ℍ′ ──────────┘                    𝕆′ ──────────┘
```

Here `h` is the imaginary unit of the coefficient field ℂ. Multiplying a unit by `h` flips the sign
of its square, which is how a split form sits inside the complexification next to the compact one.
All four arrows are implemented as monomorphisms and tested.

**Sub-algebras along each tower.**

```
ℝ ↪ ℂ ↪ ℍ ↪ 𝕆                      the division tower (canonical CD embeddings)
ℝ ↪ ℂ ↪ ℍ′ ↪ 𝕆′                    the split tower: ℂ is the plane {w + xi}
ℝ ↪ split-complex ↪ ℍ′             along j, or along k
ℍ ↪ 𝕆′                             the positive-definite half {1, e₁, e₂, e₃}
ℂ ↪ 𝔹 ↪ ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ              the complexified tower (canonical CD embeddings)
```

**Matrix algebras.** Three of the algebras *are* matrix algebras, and Kosmos provides the
isomorphisms with their inverses:

| Isomorphism | Location | Norm becomes | Conjugation becomes |
|-------------|----------|--------------|---------------------|
| ℍ′ ≅ M₂(ℝ) | `SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism(field)` | determinant | adjugate |
| ℍ ⊗ ℂ ≅ M₂(ℂ) | `BiquaternionAlgebras.BiquaternionToComplexMatrixIsomorphism` | determinant | adjugate (Hermitian conjugate ↦ conjugate transpose) |

The division algebras ℂ and ℍ, and the commutative algebras, embed into matrix rings without filling
them; see [Matrix representations](#matrix-representations).

**Names.** The *tessarines* are the bicomplex numbers `𝔹`, in a different basis; see `Bicomplex`.
*Coquaternions* are the split-quaternions. *Hyperbolic numbers*, *perplex numbers* and *double
numbers* are the split-complex numbers. *Complex quaternions* and *complex octonions* are the
biquaternions and bioctonions.

---

## One Construction, Many Algebras

Every algebra in this package is carried by the Cayley–Dickson pair `CD<A>` and gets its
arithmetic from `CayleyDickson` (in `algebra.structures`). Nothing re-implements a doubling
product by hand. The variations come from two choices: the involution on the base, and `σ`.

**Base with a nontrivial involution** (`CayleyDickson.usual` / `split`, and their associative
counterparts `associative` / `associativeSplit`) gives the noncommutative algebras:

| Algebra | Carrier | Construction |
|---------|---------|--------------|
| ℍ over `R` | `Quaternion<R>` (value class over `CD<CD<R>>`) | `CD⟨R[i]⟩` |
| 𝕆 over `R` | `Octonion<R>` (value class over `CD<CD<CD<R>>>`) | `CD⟨ℍ_R⟩` |
| ℍ′ over `R` | `SplitQuaternion<R>` (value class over `CD<CD<R>>`) | `CD_split⟨R[i]⟩` |
| 𝕆′ over `R` | `SplitOctonion<R>` (value class over `CD<CD<CD<R>>>`) | `CD_split⟨ℍ_R⟩` |
| biquaternions over `R` | `Biquaternion<R>` (value class over `CD<CD<Complex<R>>>`) | `CD⟨Bicomplex(R)⟩` = quaternions over `Complex(R)` |
| bioctonions over `R` | `Bioctonion<R>` (value class over `CD<CD<CD<Complex<R>>>>`) | `CD⟨Biquaternion(R)⟩` = octonions over `Complex(R)` |

**Commutative base with the *trivial* involution** (`CayleyDickson.commutative(base, σ)`)
gives the commutative quadratic extension `R[x]/(x² + σ)`:

| σ | x² | Algebra | Carrier |
|---|----|---------|---------|
| 1  | −1 | complex-like `R[i]/(i² + 1)`: ℂ, ℚ(i), ℤ[i] | `Complex<R>` (value class over `CD<R>`) |
| 1  | −1 | bicomplex `Complex(R)[j]/(j² + 1)` | `Bicomplex<R>` (value class over `CD<Complex<R>>`) |
| −1 | +1 | split-complex `R[j]/(j² − 1)` | `SplitComplex<R>` (value class over `CD<R>`) |
| 0  | 0  | dual numbers `F[ε]/(ε²)` | `Dual<F>` (value class over `CD<F>`) |

**Generic carriers and their named instances.** Every carrier is generic over a commutative ring:
`Complex<R>`, `Quaternion<R>`, `Octonion<R>`, the split forms, and the complexified forms
`Bicomplex<R>`, `Biquaternion<R>` and `Bioctonion<R>`. The classical coefficient rings are
typealiases:

| Typealias | Is | Algebra |
|-----------|----|---------|
| `RealComplex` | `Complex<Real>` | ℂ |
| `GaussianRat` | `Complex<Rational>` | ℚ(i) |
| `GaussianInt` | `Complex<BigInteger>` | ℤ[i] |
| `RealBicomplex` | `Bicomplex<Real>` | 𝔹 = ℂ ⊗ ℂ |
| `RealBiquaternion` | `Biquaternion<Real>` | ℍ ⊗ ℂ |
| `RealBioctonion` | `Bioctonion<Real>` | 𝕆 ⊗ ℂ |
| `RealQuaternion` | `Quaternion<Real>` | ℍ |
| `RationalQuaternion` | `Quaternion<Rational>` | ℍ_ℚ |
| `LipschitzQuaternion` | `Quaternion<BigInteger>` | the Lipschitz order `ℤ⟨i, j, k⟩` |
| `RealOctonion` | `Octonion<Real>` | 𝕆 |
| `RationalOctonion` | `Octonion<Rational>` | 𝕆_ℚ |
| `GravesianOctonion` | `Octonion<BigInteger>` | the Gravesian integers |

`ComplexAlgebras`, `QuaternionAlgebras` and `OctonionAlgebras` hold the generic constructions
(`complexRing(base)`, `quaternionRing(base)`, `octonionRing(base)`, `…RingWithReciprocal(field)`,
modules, star algebras, the canonical embeddings) alongside the structures of ℂ, ℍ and 𝕆. They work
over any commutative ring. Over finite fields, for example:
- `Complex<𝔽₇>` is the field with 49 elements, but `Complex<𝔽₅>` has zero divisors, since
  `2² = −1` there (`GenericComplexSpec`);
- `Quaternion<𝔽₇>` is a ring with zero divisors: `i + 2j + 3k` has norm 0
  (`GenericQuaternionOctonionSpec`).

**Why value classes.** Every carrier above is a value class over a `CD` pair, not a typealias,
because the raw carriers collide. The algebras differ, so the Kotlin types must too:

| Raw carrier | Shared by |
|-------------|-----------|
| `CD<R>` | `Complex<R>`, `SplitComplex<R>`, `Dual<R>` |
| `CD<CD<R>>` | `Quaternion<R>`, `SplitQuaternion<R>` |
| `CD<CD<CD<R>>>` | `Octonion<R>`, `SplitOctonion<R>` |

The complexified algebras nest the complex value class instead: `Bicomplex<R>` is over
`CD<Complex<R>>`, `Biquaternion<R>` over `CD<CD<Complex<R>>>` and `Bioctonion<R>` over
`CD<CD<CD<Complex<R>>>>`. They are the complex-like, quaternion and octonion constructions with
scalars `Complex(R)`:
```text
Bicomplex(R)    = Complex(R)    ⊗_R Complex(R) ≅ Complex(Complex(R))
Biquaternion(R) = Quaternion(R) ⊗_R Complex(R) ≅ Quaternion(Complex(R))
Bioctonion(R)   = Octonion(R)   ⊗_R Complex(R) ≅ Octonion(Complex(R))
```
No carrier in the package is a typealias of `CD` any more.

**One construction per tier.** Every quaternion-level algebra is built by the single internal
`QuaternionTier`, and every octonion-level algebra by the single `OctonionTier`. A tier is given the
scalars, whether the doubling is split, and how to name the raw pair as the public type:

| Tier | Result |
|------|--------|
| `quaternionTier(R)` | `Quaternion<R>` |
| `splitQuaternionTier(R)` | `SplitQuaternion<R>` (`split = true`) |
| `biquaternionTier(R)` | `Biquaternion<R>` (the quaternion tier with scalars `Complex(R)`) |
| `octonionTier(R)` | `Octonion<R>` |
| `splitOctonionTier(R)` | `SplitOctonion<R>` (`split = true`, over `quaternionTier(R)`) |
| `bioctonionTier(R)` | `Bioctonion<R>` (over `biquaternionTier(R)`) |

A tier supplies the ring, basis, coordinates, scalar action, norm, embeddings, `Eq` and printable,
all on the public carrier, so no `*Algebras` object handles a raw `CD` pair or does its own
transport. The public objects add only what really differs (reciprocals, vector spaces or
ℤ-modules, extra conjugations, conversions).

**What `CayleyDickson` promises.** `usual`, `split` and `commutative` are the safe factories.
`withSigma` accepts any `σ`, but the conjugation is an anti-automorphism only when `σ` is central
and fixed by the base involution (always true for the generalized quaternion algebras `(a, b)_F`,
where `σ` is a scalar). `associative` and `associativeSplit` type the double of a *commutative*
base as an associative `InvolutiveRing`; these are the quaternion and split-quaternion steps. The
norm of any double is `N(a, b) = N(a) + σ·N(b)`. The kernel is tested directly, level by level, in
`CayleyDicksonSpec`, and the transport in `CarrierTransportSpec`.

**Orders are substructures, not new implementations.** A `HurwitzQuaternion` is an element of
the Hurwitz order `𝓗 ⊂ ℍ_ℚ`, represented by its underlying `RationalQuaternion`. Its
constructor is private: checked construction goes through `HurwitzQuaternion(q)`, and the ring
operations, which provably stay in `𝓗`, use a trusted internal `wrap`. The ring structure of `𝓗`
is the restriction of `ℍ_ℚ`, and its norm lands in ℤ. Only the 24 units are invertible *in* `𝓗`;
every other nonzero element is inverted in `ℍ_ℚ` after the inclusion
`HurwitzToRationalQuaternionMonomorphism`. The same holds for the Lipschitz quaternions (8 units)
and the Gravesian octonions (16 units): their `reciprocal` is defined on the units only.

**Reciprocals follow one contract** (documented on `HasReciprocal`): `hasReciprocal` is the single
source of truth, `reciprocal` throws `ArithmeticException` exactly where it returns `false`, and
`reciprocalOption` agrees with both. Exact algebras use exact nonzero-ness. Floating-point
algebras use *numerical* invertibility: the norm (or, for `Dual<Real>`, the computed inverse) is
finite and not approximately zero, so `NaN`, `±∞`, subnormal-scale values and the null cone are
rejected rather than inverted to garbage. `Bicomplex.isUnit()` stays the exact algebraic predicate.
The base fields ℝ, ℚ, ℚ(i) and 𝔽ₚ follow the same contract, since every hypercomplex reciprocal
ends in a division in one of them. This is tested uniformly in `ReciprocalPolicySpec` (and
`PrimeFieldSpec`).

---

## Package Layout

```
hypercomplex/
├── complex/                  # Complex<R> = CD⟨R⟩ (commutative doubling, σ = 1); ℂ = RealComplex
│   ├── Complex.kt
│   ├── ComplexAlgebras.kt
│   └── ComplexPrintable.kt
│
├── splitcomplex/             # SplitComplex<R> = CD⟨R⟩ (commutative doubling, σ = -1)
│   ├── SplitComplex.kt
│   └── SplitComplexAlgebras.kt
│
├── dual/                     # Dual<F>: value class over CD⟨F⟩ (commutative doubling, σ = 0)
│   ├── Dual.kt
│   ├── DualAlgebras.kt             ring, reciprocal, autodiff, matrix embedding
│   ├── DualModules.kt              scalar extension V ⊗ F[ε]
│   ├── DualRealFns.kt              elementary functions on Dual<Real>
│   └── DualRationalFns.kt          rational functions on Dual<Rational>
│
├── multi/                    # Bicomplex: value class over CD⟨ℂ⟩ (commutative doubling, σ = 1)
│   ├── Bicomplex.kt                standard basis z1 + z2·j; idempotent (𝛂, 𝜷) views
│   ├── BicomplexAlgebras.kt
│   (design notes for the general multicomplex case: docs/design/Multicomplex.md)
│
├── splitquaternion/          # SplitQuaternion<R> = CD_split⟨R[i]⟩ ≅ M₂(R)
│   ├── SplitQuaternion.kt
│   └── SplitQuaternionAlgebras.kt
│
├── splitoctonion/            # SplitOctonion<R> = CD_split⟨ℍ_R⟩
│   ├── SplitOctonion.kt
│   └── SplitOctonionAlgebras.kt
│
├── biquaternion/             # ℍ ⊗ ℂ = CD⟨𝔹⟩ ≅ M₂(ℂ)
│   ├── Biquaternion.kt
│   └── BiquaternionAlgebras.kt     three conjugations, real forms, matrix isomorphism
│
├── bioctonion/               # 𝕆 ⊗ ℂ = CD⟨ℍ ⊗ ℂ⟩
│   ├── Bioctonion.kt
│   └── BioctonionAlgebras.kt       three conjugations, real forms, 168 embeddings
│
├── quaternion/               # ℍ-like rings
│   ├── Quaternion.kt               Quaternion<R>; RealQuaternion, RationalQuaternion, LipschitzQuaternion
│   ├── QuaternionAlgebras.kt       generic constructions, and ℍ
│   ├── RationalQuaternionAlgebras.kt   ℍ_ℚ
│   ├── LipschitzQuaternionAlgebras.kt  the Lipschitz order
│   ├── HurwitzQuaternion.kt        𝓗 ⊂ ℍ_ℚ, the Hurwitz order (value class over RationalQuaternion)
│   ├── HurwitzQuaternionAlgebras.kt
│   ├── QuaternionTier.kt           the one quaternion-level construction, on any public carrier
│   ├── AxisSignEmbeddings.kt       the 6 specs for complex-like ↪ quaternion-like
│   ├── QuaternionEmbeddingKit.kt   generic complex-like → quaternion builder
│   └── QuaternionPrintable.kt
│
└── octonion/                 # 𝕆-like rings
    ├── Octonion.kt                 Octonion<R>; RealOctonion, RationalOctonion, GravesianOctonion
    ├── OctonionAlgebras.kt         generic constructions, and 𝕆
    ├── RationalOctonionAlgebras.kt     𝕆_ℚ
    ├── GravesianOctonionAlgebras.kt    the Gravesian integers ≅ ℤ⁸
    ├── OctonionTier.kt             the one octonion-level construction, on any public carrier
    ├── OctonionEmbeddingKit.kt     the 168 signed-basis-unit quaternion → octonion embeddings
    ├── OctonionFanoOrientation(s).kt, OrientedFanoLine.kt
    └── OctonionPrintable.kt
```

The Gaussian integers `ℤ[i]`, Gaussian rationals `ℚ(i)` and Eisenstein integers `ℤ[ω]` live in
`numberfields.quadratic`. `GaussianInt` and `GaussianRat` are `Complex<BigInteger>` and
`Complex<Rational>`, with their
ring arithmetic taken from `CayleyDickson.commutative`.

Bases are derived rather than written out: `CayleyDickson.doubledBasis(zero, baseBasis)` doubles
a basis `e₀, …, e_{n-1}` of `A` to `(e₀, 0), …, (0, e_{n-1})` in `CD<A>`, which reproduces
`1, i, j, k` for ℍ and `1, e₁, …, e₇` for 𝕆. Each quaternion and octonion ring exposes it as
`basis`.

---

## The Tower Diagram

This section is about the division tower and its three coefficient rings.

Solid arrows are ring monomorphisms (structure-preserving injections).
Horizontal arrows stay within one "level" of the tower;
vertical arrows widen coefficients.

```
                        ℤ-coefficients          ℚ-coefficients          ℝ-coefficients
                        ──────────────          ──────────────          ──────────────

Level 0 (scalars)       ℤ ─────────────────────→ ℚ ─ ─ ─ ─ ─ ─ ─ ─ ─ → ℝ
                        │                         │           (floating pt)
                        ↓                         ↓
Level 1 (complex)       ℤ[i] ─────────────────→ ℚ(i) ─ ─ ─ ─ ─ ─ ─ ─→ ℂ
                        │  ╲  (×6)                │  ╲  (×6)              │  ╲  (×6)
                        │   ╲                     │   ╲                   │   ╲
                        ↓    ↘                    ↓    ↘                  ↓    ↘
Level 2 (quaternion)    Lip ──→ Hur ───────────→ ℍ_ℚ ─ ─ ─ ─ ─ ─ ─ ─ → ℍ
                        │  ╲  (×168)              │  ╲  (×168)            │  ╲  (×168)
                        │   ╲                     │   ╲                   │   ╲
                        ↓    ↘                    ↓    ↘                  ↓    ↘
Level 3 (octonion)      Grav ─────────────────→ 𝕆_ℚ ─ ─ ─ ─ ─ ─ ─ ─ → 𝕆
```

Dashed arrows (─ ─ →) are floating-point conversions (`toReal()`). They are **not**
monomorphisms, or even homomorphisms: distinct large integers and rationals collide in
`Double`, and products are preserved only up to rounding. They are therefore typed as plain
conversions, `UnaryOp`s named `…Approx` (e.g. `rationalQuaternionToQuaternionApprox`), and are
tested as homomorphisms *up to approximate equality* only.

The Eisenstein integers $\mathbb{Z}[\omega]$ sit beside $\mathbb{Z}[i]$ at Level 1
with a homomorphism $\mathbb{Z}[\omega] \to \mathbb{C}$. They embed into the Hurwitz order,
but not into the Lipschitz quaternions (see [below](#eisenstein-integers-and-the-quaternion-orders)).

---

## Split Forms

The split forms use the doubling sign `σ = −1`, so the new unit squares to `+1`. They are generic
over a commutative base ring `R`, like `SplitComplex<R>`: the same code gives the algebras over ℝ,
ℚ, ℤ and finite fields. The exact versions (over ℚ) are what the tests check with strict equality.

### Split-quaternions

```text
i² = −1,   j² = +1,   k² = +1,   ij = k,   jk = −i,   ki = j,   ijk = +1
```

```kotlin
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitquaternion.splitQuaternion

val ring = SplitQuaternionAlgebras.RealSplitQuaternionRing
val q = splitQuaternion(2.0, 0.0, 1.0, 0.0)          // 2 + j
ring.normSq(q)                                        // 3.0 = w² + x² − y² − z²
ring.reciprocal(q)                                    // (2 − j) / 3

// Zero divisors: (1 + j)(1 − j) = 0, and neither factor is invertible.
ring.hasReciprocal(splitQuaternion(1.0, 0.0, 1.0, 0.0))   // false

// Exact, over any commutative ring:
val exact = SplitQuaternionAlgebras.splitQuaternionRing(RationalAlgebras.RationalField)

// ℍ′ ≅ M₂(F), with inverse; the norm is the determinant.
val iso = SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism(RationalAlgebras.RationalField)
```

The matrix representation is `w + xi + yj + zk ↦ [[w − z, y − x], [x + y, w + z]]`. It is an
isomorphism when 2 is invertible. Over ℤ it is injective but not surjective (entries on each
diagonal share a parity), and in characteristic 2 the isomorphism is refused.

### Split-octonions

`SplitOctonion<R>` is the split doubling of the Hamilton quaternions over `R`: `e₁, e₂, e₃` square
to `−1` and `e₄, …, e₇` square to `+1`. It is alternative, flexible and Moufang but not
associative, and its norm has signature `(4, 4)`.

```kotlin
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras

val ring = SplitOctonionAlgebras.RealSplitOctonionAlgebra
val (one, e1, e2, e3, e4) = ring.basis
ring.mul(e4, e4)                                      // +1
ring.mul(ring.add(one, e4), ring.add(one, ring.add.inverse(e4)))   // 0
```

The inverse `o⁻¹ = conj(o)/N(o)` exists off the null cone and is two-sided despite
nonassociativity, because the algebra is alternative.

---

## Complexifications

The biquaternions and bioctonions are the quaternion and octonion algebras *over ℂ*. Their
coefficients are complex numbers, and the imaginary unit of that coefficient field is written `h`
to keep it apart from the units `i, j, k` and `e₁, …, e₇`. `h` is central.

### Three conjugations

Each complexified algebra has three involutions, and it matters which one is meant:

| Conjugation | On `ℍ ⊗ ℂ` | Linear over | Reverses products | API |
|-------------|------------|-------------|:-----------------:|-----|
| quaternion / octonion | `w − xi − yj − zk` | ℂ | ✓ | `conj` |
| complex | conjugate each coefficient (`h ↦ −h`) | ℝ only | ✗ (an automorphism) | `complexConj` |
| Hermitian | both | ℝ only | ✓ | `hermitianConj` |

`conj` is the Cayley–Dickson conjugation, and the norm is `N(q) = q·conj(q)`, which is
complex-valued. The fixed ring of `complexConj` is the real form ℍ (or 𝕆). Under
`ℍ ⊗ ℂ ≅ M₂(ℂ)` the Hermitian conjugate is the conjugate transpose.

Correspondingly there are two star-algebra structures: `BiquaternionStarAlgebra` over ℂ (with
`conj`) and `BiquaternionHermitianStarAlgebra` over ℝ (with `hermitianConj`), and likewise for the
bioctonions.

```kotlin
import org.vorpal.kosmos.hypercomplex.biquaternion.BiquaternionAlgebras

val ring = BiquaternionAlgebras.BiquaternionRing
val (i, j, k, h) = listOf(ring.i, ring.j, ring.k, ring.h)

ring.mul(h, h)                                        // −1, and h commutes with everything
val nullElement = ring.add(ring.one, ring.mul(h, i))  // 1 + h·i
ring.normSq(nullElement)                              // 0 + 0i: a zero divisor
ring.hasReciprocal(nullElement)                       // false

val iso = BiquaternionAlgebras.BiquaternionToComplexMatrixIsomorphism   // ℍ ⊗ ℂ ≅ M₂(ℂ)
```

The bioctonions additionally carry the 168 (84 normalized) signed-basis-unit embeddings
`ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ`, the ℂ-linear extensions of the embeddings `ℍ ↪ 𝕆` described below.

**Generic over the base ring.** `Bicomplex<R>`, `Biquaternion<R>` and `Bioctonion<R>` take any
commutative ring, and `BicomplexAlgebras`, `BiquaternionAlgebras` and `BioctonionAlgebras` provide
the generic rings, norms, conjugations, modules, star algebras and embeddings alongside the real
structures. The usage examples in this section are over ℝ.

**Exactness.** The real versions have floating-point coefficients, so their specs use approximate
equality. `GenericComplexifiedSpec` checks the same laws *exactly* over ℚ (associativity,
alternativity, the Moufang identities, multiplicativity of the norm, reciprocals, every embedding),
and over the finite field 𝔽₃. `ComplexifiedTowerSpec` checks the raw construction over ℚ(i).

---

## Embeddings: Complex → Quaternion (×6)

### The Idea

A complex number $a + bi$ lives in a 2D subalgebra of the quaternions.
The real part always maps to the scalar component, but the imaginary
unit $i_{\mathbb{C}}$ can map to any of $\pm i$, $\pm j$, $\pm k$ in $\mathbb{H}$:

$$
\varphi(a + bi_{\mathbb{C}}) \;=\; a \cdot 1 \;+\; b \cdot u, \qquad u \in \{+i,\, -i,\, +j,\, -j,\, +k,\, -k\}
$$

Each choice gives a unital ring monomorphism.
There are exactly **6 such embeddings**, parameterised by an
`AxisSignEmbedding(axis, sign)` where `axis ∈ {I, J, K}` and `sign ∈ {PLUS, MINUS}`.

### How to Create Them

Each complex-like Algebras object exposes a factory function that
takes an `AxisSignEmbedding` parameter (defaulting to canonical: $i_{\mathbb{C}} \mapsto +i$).

#### ℂ ↪ ℍ  (Real coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.*
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras

// Canonical: i_C ↦ +i
val canonical = ComplexAlgebras.complexToQuaternionEmbedding()

// Pick a specific one: i_C ↦ -k
val emb = AxisSignEmbedding(ImagAxis.K, Sign.MINUS)
val phiMinusK = ComplexAlgebras.complexToQuaternionEmbedding(emb)

// Use it:
val z = complex(3.0, 4.0)
val q = phiMinusK(z)   // quaternion(3.0, 0.0, 0.0, -4.0)
```

#### ℤ[i] ↪ Lip  (BigInteger coefficients)

```kotlin
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras

// Canonical: i ↦ +i
val phi = GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding()

// Along j-axis with positive sign: i ↦ +j
val phiJ = GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding(
    AxisSignEmbedding(ImagAxis.J, Sign.PLUS)
)
```

#### ℚ(i) ↪ ℍ_ℚ  (Rational coefficients)

```kotlin
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras

val phi = GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding()
```

#### ℤ[i] ↪ Hur  (via Lipschitz, then inclusion)

```kotlin
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras

// Composes: ℤ[i] ↪ Lip ↪ Hur
val phi = GaussianIntAlgebras.gaussianIntToHurwitzQuaternionEmbedding(
    AxisSignEmbedding(ImagAxis.I, Sign.PLUS)
)
```

#### Enumerating All 6

```kotlin
AxisSignEmbedding.all.forEach { emb ->
    val phi = ComplexAlgebras.complexToQuaternionEmbedding(emb)
    println("${emb.axis}${emb.sign}: phi(1+i) = ${phi(complex(1.0, 1.0))}")
}
```

The `AxisSignEmbedding.all` list contains all 6 combinations in a
deterministic order.

---

## Embeddings: Quaternion → Octonion (×168, or ×84 normalized)

### The Idea

A quaternion $w + xi + yj + zk$ can be embedded into the octonions by sending $i$ and $j$ to
*signed* imaginary basis units:

$$
\varphi(i) = \sigma_i\, e_p, \qquad \varphi(j) = \sigma_j\, e_q, \qquad
\varphi(k) = \varphi(i)\,\varphi(j) = \pm e_r, \qquad p \ne q,\ \sigma_i, \sigma_j \in \{\pm 1\}.
$$

Every pair of distinct points $p, q$ lies on exactly one **line of the Fano plane**
$\text{PG}(2,2)$ (the unique $(7,3,1)$-design), whose third point $r$ is forced. Two distinct
imaginary units anticommute and square to $-1$, so $(\varphi(i), \varphi(j))$ always generates a
copy of ℍ, and the quaternion multiplication table is respected.

What gets counted depends on what is allowed to vary:

| Count | What it counts | API |
|------:|----------------|-----|
| 1 | the Cayley–Dickson octonions themselves | `OctonionDivisionAlgebra` |
| 7 | coordinate quaternion subalgebras, one per Fano line | `FanoPlane.lines` |
| 84 | embeddings normalized so that $j \mapsto +e_q$ | `normalizedQuaternionEmbeddings()` |
| 168 | all signed-basis-unit embeddings ($\sigma_i$, $\sigma_j$ independent) | `allQuaternionEmbeddings()` |

$$
168 = \underbrace{7 \times 6}_{\text{ordered pairs } (p, q)} \times \underbrace{2 \times 2}_{(\sigma_i,\, \sigma_j)},
\qquad 84 = 7 \times 6 \times 2.
$$

The two families differ by an automorphism of ℍ: every embedding with $\sigma_j = -1$ is a
normalized one precomposed with $i \mapsto i,\ j \mapsto -j,\ k \mapsto -k$. So the 84 are a
set of representatives of the 168 modulo that automorphism (verified in `OctonionEmbeddingSpec`).

### How to Create Them

Each octonion-level Algebras object exposes an `embeddingKit` and both
`allQuaternionEmbeddings()` and `normalizedQuaternionEmbeddings()`.

#### ℍ ↪ 𝕆  (Real coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.Sign

// === Method 1: Create one embedding by spec ===

val (spec, phi) = OctonionAlgebras.embeddingKit.createEmbedding(
    iIndex = 1,            // i ↦ ±e₁
    jIndex = 2,            // j ↦ ±e₂
    iSign = Sign.PLUS,
    jSign = Sign.PLUS
)
// spec.k == 3, spec.kSign == +1   (forced: k ↦ +e₃)

val q = quaternion(1.0, 2.0, 3.0, 4.0)
val o = phi(q)  // 1 + 2e₁ + 3e₂ + 4e₃

// === Method 2: Enumerate ===

val all = OctonionAlgebras.allQuaternionEmbeddings()               // 168
val normalized = OctonionAlgebras.normalizedQuaternionEmbeddings() // 84

// Find a specific one by filtering:
val (_, phiCustom) = all.entries.first { (s, _) ->
    s.i == 3 && s.j == 5 && s.iSign == Sign.MINUS && s.jSign == Sign.PLUS
}
```

#### Lip ↪ Grav  (BigInteger coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.GravesianOctonionAlgebras

val all = GravesianOctonionAlgebras.allQuaternionEmbeddings()

// Or one at a time:
val (spec, phi) = GravesianOctonionAlgebras.embeddingKit.createEmbedding(
    iIndex = 4, jIndex = 6, iSign = Sign.PLUS, jSign = Sign.MINUS
)
```

#### ℍ_ℚ ↪ 𝕆_ℚ  (Rational coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.RationalOctonionAlgebras

val all = RationalOctonionAlgebras.allQuaternionEmbeddings()
```

### The `OctonionEmbeddingSpec`

Each embedding is identified by an `OctonionEmbeddingSpec`:

```kotlin
data class OctonionEmbeddingSpec(
    val i: Int,          // which e_p quaternion i maps to (up to sign)
    val j: Int,          // which e_q quaternion j maps to (up to sign)
    val k: Int,          // forced: third point on the Fano line through p and q
    val iSign: Sign,     // φ(i) = iSign · e_p
    val jSign: Sign,     // φ(j) = jSign · e_q
    val kSign: Int       // +1 or -1: φ(k) = kSign · e_r
)
```

The `kSign` is not a free parameter: it is *computed* from the octonion multiplication table,
then recorded in the spec for inspection.

### The Canonical Embedding

The canonical embedding has `iIndex = 1, jIndex = 2` and both signs `PLUS`, giving
$i \mapsto e_1,\; j \mapsto e_2,\; k \mapsto e_3$. This is `RealQuaternion.asOctonion()`, i.e. the
Cayley–Dickson embedding $q \mapsto (q, 0)$.

---

## Eisenstein Integers and the Quaternion Orders

The Eisenstein integers $\mathbb{Z}[\omega]$, $\omega = e^{2\pi i/3}$, satisfy
$\omega^2 + \omega + 1 = 0$. A unital ring monomorphism
$\varphi : \mathbb{Z}[\omega] \hookrightarrow \mathcal{O}$ into a quaternion order therefore needs
an element $u = \varphi(\omega)$ with $u^2 + u + 1 = 0$, i.e. a unit of order 3.

- In the **Lipschitz** quaternions there is none: the only units are $\pm 1, \pm i, \pm j, \pm k$,
  which have orders 1, 2 and 4.
- In the **Hurwitz** order there are eight:
  $$
  u = \tfrac{1}{2}(-1 \pm i \pm j \pm k)
  $$
  all have real part $-\tfrac{1}{2}$ and norm 1, hence $u^2 + u + 1 = 0$. For example
  $\rho = \omega_{\mathcal{H}} - 1 = \tfrac{1}{2}(-1 + i + j + k)$, where
  $\omega_{\mathcal{H}} = \tfrac{1}{2}(1 + i + j + k)$ is `HurwitzQuaternion.OMEGA`.
  So $\mathbb{Z}[\omega] \cong \mathbb{Z}[\rho] \subset \mathcal{H}$ (verified in
  `HurwitzQuaternionRingSpec`).

Kosmos does not yet provide the $\mathbb{Z}[\omega] \hookrightarrow \mathcal{H}$ monomorphisms.
Today the only path from the Eisenstein integers upward is the floating-point homomorphism

```
ℤ[ω] → ℂ → ℍ → 𝕆
```

via `EisensteinIntAlgebras.EisensteinIntToCHomomorphism`, which
passes through `Real` and is therefore not exact.

---

## Canonical (CD) Embeddings

At every level of the Cayley–Dickson tower, there is a *structural*
embedding given by the first-slot injection $a \mapsto (a, 0)$.
This is a ring monomorphism by the universal property of the construction,
and it is provided by:

```kotlin
CayleyDickson.canonicalEmbedding(base, doubled)
```

These canonical embeddings are used throughout the tower:

| Arrow                | Embedding                                                    |
|----------------------|--------------------------------------------------------------|
| $\mathbb{Z}[i] \hookrightarrow \text{Lip}$ | `GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding()` (canonical default) |
| $\text{Lip} \hookrightarrow \text{Grav}$    | `LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism` |
| $\mathbb{Q}(i) \hookrightarrow \mathbb{H}_\mathbb{Q}$ | `GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding()` (canonical default) |
| $\mathbb{C} \hookrightarrow \mathbb{B}$     | `BicomplexAlgebras.complexToBicomplexMonomorphism`           |
| $\mathbb{H}_\mathbb{Q} \hookrightarrow \mathbb{O}_\mathbb{Q}$ | `RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism` |
| $\mathbb{H} \hookrightarrow \mathbb{O}$     | `OctonionAlgebras.QuaternionToOctonionMonomorphism`          |
| $\mathbb{C} \hookrightarrow \mathbb{H}'$    | `SplitQuaternionAlgebras.ComplexToSplitQuaternionMonomorphism` |
| $\mathbb{H} \hookrightarrow \mathbb{O}'$    | `SplitOctonionAlgebras.QuaternionToSplitOctonionMonomorphism` |
| $\mathbb{B} \hookrightarrow \mathbb{H} \otimes \mathbb{C}$ | `BiquaternionAlgebras.BicomplexToBiquaternionMonomorphism` |
| $\mathbb{H} \otimes \mathbb{C} \hookrightarrow \mathbb{O} \otimes \mathbb{C}$ | `BioctonionAlgebras.BiquaternionToBioctonionMonomorphism` |

The ×6 and ×168 embedding families *include* the canonical embedding
as a special case (the one with axis=I, sign=PLUS for ×6;
iIndex=1, jIndex=2, both signs PLUS for ×168).

---

## Scalar Actions and Modules

Each type has an appropriate scalar action:

| Type | Scalar | Structure |
|------|--------|-----------|
| $\mathbb{Z}[i]$ | $\mathbb{Z}$ | `ZModule` |
| $\mathbb{Q}(i)$ | $\mathbb{Q}$ | `FiniteVectorSpace` (dim 2) |
| $\mathbb{C}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 2) |
| Lip  | $\mathbb{Z}$ | `ZModule` |
| Hur  | $\mathbb{Z}$ | `ZModule` |
| $\mathbb{H}_\mathbb{Q}$ | $\mathbb{Q}$ | `FiniteVectorSpace` (dim 4) |
| $\mathbb{H}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 4) |
| $\mathbb{H}$     | $\mathbb{C}$ | `FiniteVectorSpace` (dim 2, per embedding) |
| Grav | $\mathbb{Z}$ | `ZModule` |
| $\mathbb{O}_\mathbb{Q}$ | $\mathbb{Q}$ | `FiniteVectorSpace` (dim 8) |
| $\mathbb{O}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 8) |
| $\mathbb{B}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 4) |
| $\mathbb{B}$     | $\mathbb{C}$ | `FiniteVectorSpace` (dim 2) |
| `SplitQuaternion<R>` | $R$ | `RModule` (rank 4); `FiniteVectorSpace` (dim 4) over ℝ |
| `SplitOctonion<R>` | $R$ | `RModule` (rank 8); `FiniteVectorSpace` (dim 8) over ℝ |
| $\mathbb{H} \otimes \mathbb{C}$ | $\mathbb{C}$ | `FiniteVectorSpace` (dim 4) |
| $\mathbb{H} \otimes \mathbb{C}$ | $\mathbb{R}$ | `FiniteVectorSpace` (dim 8) |
| $\mathbb{O} \otimes \mathbb{C}$ | $\mathbb{C}$ | `FiniteVectorSpace` (dim 8) |
| $\mathbb{O} \otimes \mathbb{C}$ | $\mathbb{R}$ | `FiniteVectorSpace` (dim 16) |

These scalar actions are required by the `OctonionEmbeddingKit`
to construct embeddings via linear combination of basis elements.

---

## Quick Reference: Monomorphisms and Conversions

Entries marked (≈) are **not** morphisms: they are `UnaryOp` conversions into floating point
(named `…Approx`), which round and so preserve the operations only approximately.

### Level 0 → Level 1

| Arrow | Location |
|-------|----------|
| `ℤ ↪ ℤ[i]` | `GaussianIntAlgebras.ZToGaussianIntMonomorphism` |
| `ℤ[i] ↪ ℂ` | `GaussianIntAlgebras.GaussianIntToComplexMonomorphism` |
| `ℚ ↪ ℚ(i)` | `GaussianRatAlgebras.QToGaussianRatMonomorphism` |
| `ℤ[i] ↪ ℚ(i)` | `GaussianIntAlgebras.GaussianIntToRatMonomorphism` |
| `ℤ ↪ ℚ(i)` | `GaussianRatAlgebras.ZToGaussianRatMonomorphism` |
| `ℚ(i) → ℂ` | `GaussianRatAlgebras.GaussianRatToComplexHomomorphism` |
| `ℝ ↪ ℂ` | `RealAlgebras.RealToComplexMonomorphism` |
| `ℤ[ω] → ℂ` | `EisensteinIntAlgebras.EisensteinIntToCHomomorphism` |

### Level 1 → Level 2 (×6 families)

| Arrow | Location |
|-------|----------|
| `ℂ ↪ ℍ` (×6) | `ComplexAlgebras.complexToQuaternionEmbedding(emb)` |
| `ℤ[i] ↪ Lip` (×6) | `GaussianIntAlgebras.gaussianIntToLipschitzQuaternionEmbedding(emb)` |
| `ℚ(i) ↪ ℍ_ℚ` (×6) | `GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding(emb)` |
| `ℤ[i] ↪ 𝓗` (×6) | `GaussianIntAlgebras.gaussianIntToHurwitzQuaternionEmbedding(emb)` |
| `ℂ ↪ 𝔹` | `BicomplexAlgebras.complexToBicomplexMonomorphism` (`z ↦ z + 0j`) |
| `ℂ ↪ ℂ(j) ⊂ 𝔹` | `BicomplexAlgebras.complexToBicomplexAlongJ` |

### Within Level 2

| Arrow | Location |
|-------|----------|
| `Lip ↪ 𝓗` | `LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism` |
| `𝓗 ↪ ℍ_ℚ` | `HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism` |
| `Lip ↪ ℍ_ℚ` | `LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism` |
| `Lip → ℍ` (≈) | `LipschitzQuaternionAlgebras.lipschitzToQuaternionApprox` |
| `𝓗 → ℍ` (≈) | `HurwitzQuaternionAlgebras.hurwitzToQuaternionApprox` |
| `ℍ_ℚ → ℍ` (≈) | `RationalQuaternionAlgebras.rationalQuaternionToQuaternionApprox` |
| `𝔹 → ℂ` (×2) | `BicomplexAlgebras.BicomplexFirstProjectionHomomorphism`, `…SecondProjectionHomomorphism` |

### Level 2 → Level 3 (×168 families + canonical)

| Arrow | Location |
|-------|----------|
| `ℍ ↪ 𝕆` (×168) | `OctonionAlgebras.allQuaternionEmbeddings()` |
| `Lip ↪ Grav` (×168) | `GravesianOctonionAlgebras.allQuaternionEmbeddings()` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (×168) | `RationalOctonionAlgebras.allQuaternionEmbeddings()` |
| `ℍ ↪ 𝕆` (canonical) | `OctonionAlgebras.QuaternionToOctonionMonomorphism` |
| `Lip ↪ Grav` (canonical) | `LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (canonical) | `RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism` |

### Within Level 3 and Cross-Level

| Arrow | Location |
|-------|----------|
| `Grav ↪ 𝕆_ℚ` | `GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism` |
| `Lip ↪ 𝕆_ℚ` | `LipschitzQuaternionAlgebras.LipschitzToRationalOctonionMonomorphism` |
| `𝓗 ↪ 𝕆_ℚ` | `RationalOctonionAlgebras.HurwitzToRationalOctonionMonomorphism` |
| `Grav → 𝕆` (≈) | `GravesianOctonionAlgebras.gravesianToOctonionApprox` |
| `𝕆_ℚ → 𝕆` (≈) | `RationalOctonionAlgebras.rationalOctonionToOctonionApprox` |

### Split forms

| Arrow | Location |
|-------|----------|
| `R ↪ ℍ′_R` | `SplitQuaternionAlgebras.scalarEmbedding(base)` |
| `ℂ ↪ ℍ′` | `SplitQuaternionAlgebras.ComplexToSplitQuaternionMonomorphism` |
| `SplitComplex<R> ↪ ℍ′_R` (along j) | `SplitQuaternionAlgebras.splitComplexToSplitQuaternionMonomorphism(base)` |
| `SplitComplex<R> ↪ ℍ′_R` (along k) | `SplitQuaternionAlgebras.splitComplexToSplitQuaternionAlongK(base)` |
| `R ↪ 𝕆′_R` | `SplitOctonionAlgebras.scalarEmbedding(base)` |
| `ℍ ↪ 𝕆′` | `SplitOctonionAlgebras.QuaternionToSplitOctonionMonomorphism` |
| `ℍ′_R ↪ 𝕆′_R` | `SplitOctonionAlgebras.splitQuaternionToSplitOctonionMonomorphism(base)` |

### Complexifications

| Arrow | Location |
|-------|----------|
| `ℂ ↪ ℍ ⊗ ℂ` (center) | `BiquaternionAlgebras.ComplexToBiquaternionMonomorphism` |
| `ℍ ↪ ℍ ⊗ ℂ` (real form) | `BiquaternionAlgebras.QuaternionToBiquaternionMonomorphism` |
| `ℍ′ ↪ ℍ ⊗ ℂ` (real form) | `BiquaternionAlgebras.SplitQuaternionToBiquaternionMonomorphism` |
| `𝔹 ↪ ℍ ⊗ ℂ` | `BiquaternionAlgebras.BicomplexToBiquaternionMonomorphism` |
| `ℂ ↪ 𝕆 ⊗ ℂ` (scalars) | `BioctonionAlgebras.ComplexToBioctonionMonomorphism` |
| `𝕆 ↪ 𝕆 ⊗ ℂ` (real form) | `BioctonionAlgebras.OctonionToBioctonionMonomorphism` |
| `𝕆′ ↪ 𝕆 ⊗ ℂ` (real form) | `BioctonionAlgebras.SplitOctonionToBioctonionMonomorphism` |
| `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ` (canonical) | `BioctonionAlgebras.BiquaternionToBioctonionMonomorphism` |
| `ℍ ⊗ ℂ ↪ 𝕆 ⊗ ℂ` (×168) | `BioctonionAlgebras.allBiquaternionEmbeddings()` |

### Matrix representations

| Arrow | Location |
|-------|----------|
| `ℂ ↪ M₂(ℝ)` | `ComplexAlgebras.ComplexToRank2RMatrixMonomorphism` |
| `SplitComplex<R> ↪ M₂(R)` | `SplitComplexAlgebras.splitComplexToRank2MatrixMonomorphism(base)` |
| `Dual<F> ↪ M₂(F)` | `DualAlgebras.dualToRank2MatrixMonomorphism(base)` |
| `𝔹 ↪ M₂(ℂ)` | `BicomplexAlgebras.BicomplexToComplexMatrixMonomorphism`, `…ToDiagonalMatrixMonomorphism` |
| `ℍ ↪ M₂(ℂ)` | `QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism` |
| `ℍ′_R ↪ M₂(R)` | `SplitQuaternionAlgebras.splitQuaternionToRank2MatrixMonomorphism(base)` |
| `ℍ′_F ≅ M₂(F)` | `SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism(field)` |
| `ℍ ⊗ ℂ ≅ M₂(ℂ)` | `BiquaternionAlgebras.BiquaternionToComplexMatrixIsomorphism` |

The first four are all `quadraticRank2MatrixEmbedding`: the regular representation of
`R[x]/(x² − s − tx)` on the basis `{1, x}`. Restricted to ℍ, the biquaternion isomorphism is the
classical `ℍ ↪ M₂(ℂ)`.

---

## Combinatorics of the Octonion Multiplication Table

The products of the seven imaginary units, `e_i · e_j = ± e_k`, form a finite object that can be
checked exhaustively. `OctonionCombinatoricsSpec` does so, exactly, over the rational octonions:

| Count | What it counts |
|------:|----------------|
| 35 = 7 + 28 | unordered triples of distinct units: the 7 Fano lines, and 28 non-lines |
| 42 | ordered triples of distinct units that associate (7 lines × 6 orders); each line spans a copy of the quaternion group Q₈ |
| 168 | ordered triples that do not associate (28 × 6); they *anti*-associate: `(e_i e_j) e_k = −e_i (e_j e_k)` |
| 84 | pairs `(i, j, k) ↔ (k, j, i)` of nonassociative triples, whose associators are negatives of each other |
| 21 | permutations of the units preserving the table: the Frobenius group C₇ ⋊ C₃ |
| 168 | collineations of the unoriented Fano plane, `PSL(2, 7)` |
| 1344 = 2³ · 168 | signed permutations preserving the table |
| 480 = 645120 / 1344 | distinct multiplication tables reachable by signed permutations: 30 Fano planes × 16 orientations |

Two facts behind the table:

- **The associator is alternating.** `[x, y, z] = (xy)z − x(yz)` changes sign under any
  transposition of its arguments, because the octonions are alternative. On a nonassociative basis
  triple it equals `±2·e_m`, where `m` is the one point of the Fano plane not on a line through two
  of `i, j, k`.
- **Why 16 orientations.** A Fano plane has 2⁷ = 128 ways to orient its 7 lines, but only 16 of them
  are octonion tables. Flipping the signs of the units (2⁷ choices) acts on the orientations with a
  kernel of order 8, giving 128 / 8 = 16.

---

## The Fano Plane

The 168 octonion embeddings are governed by the Fano plane, the unique
Steiner triple system $S(2,3,7)$ on points $\{1,\ldots,7\}$.
Its 7 lines (each a 3-element subset) are:

```
{1, 2, 3}    {1, 4, 5}    {1, 6, 7}
{2, 4, 6}    {2, 5, 7}    {3, 4, 7}    {3, 5, 6}
```

Every pair of distinct points lies on exactly one line.
Given a quaternion embedding choice $(i \mapsto e_p,\; j \mapsto e_q)$,
the third point $r$ on the unique line through $p$ and $q$
determines the image of $k$:

$$
\varphi(k) = \varphi(i) \cdot \varphi(j) = \pm\, e_r
$$

The sign depends on the orientation of the Fano line relative to
the octonion multiplication table and is computed (not chosen) by
the `OctonionEmbeddingKit`.

The Fano plane implementation lives in `org.vorpal.kosmos.combinatorics.FanoPlane`.

---

## What Remains to Be Done

Nothing below is implemented yet. Items are grouped by how they would build on what exists.

### Agreed roadmap

Dimension 16 is the stopping point for *named* hypercomplex types. In priority order:

1. **`Sedenion<R>`**: the fourth usual doubling, `CD⟨Octonion<R>⟩`, with `coefficients()` and
   `get(index)` rather than sixteen named coordinates. Flexible and power-associative, not
   alternative; zero divisors, yet over ℝ the norm is positive definite (signature `(16, 0)`), so
   there are no nonzero isotropic elements.
2. **`SplitSedenion<R>`**: the *split* doubling of the ordinary octonions, `N(a, b) = N(a) − N(b)`,
   signature `(8, 8)` over ℝ, so it has nonzero isotropic elements as well. "Split sedenion" is not
   a standardized name: several Cayley–Dickson sign sequences give an indefinite form at dimension
   16, so the documentation must state the sequence used.
3. **Arbitrary-depth Cayley–Dickson algebras** through a generic construction parameterized by the
   sequence of doubling signs and exposing a `dimension`, instead of a `Trigintaduonion<R>` type
   with five levels of nested generics.
4. **The 32-dimensional algebra, computationally**: its loop of 64 signed basis units and its
   subloop lattice (Cawagas et al. report 373 nontrivial subloops), and more generally how the
   subalgebra structure changes under successive doublings.

The geometry that goes with each level, to be recorded as tests when the level is built:

| Algebra | Imaginary units | Geometry of `e_i · e_j = ± e_k` | As a design | Collineations |
|---------|----------------:|----------------------------------|-------------|---------------|
| 𝕆 | 7 | PG(2,2), 7 lines | STS(7) | 168 (verified; 21 preserve orientation) |
| 𝕊 | 15 | PG(3,2), 35 lines | STS(15) | 20,160 = \|GL(4,2)\|, GL(4,2) ≅ A₈ |
| 32-dim | 31 | PG(4,2), 155 lines | STS(31) | \|GL(5,2)\| |

The collineation counts are for the unoriented geometry. The number preserving the signs is much
smaller, as it is for the octonions, and is to be computed.

**Subloops of the signed basis units.** For the `2ⁿ`-dimensional algebra the signed units
`{±e_0, …, ±e_(2ⁿ−1)}` form a loop of order `2ⁿ⁺¹`, and `±e_i ↦ i` maps it onto `𝔽₂ⁿ` with kernel
`{±1}`. Every nontrivial subloop contains `−1` (any imaginary unit squares to it), so nontrivial
subloops correspond exactly to subspaces of `𝔽₂ⁿ`. The number of proper nontrivial subloops is
therefore a sum of Gaussian binomial coefficients, `Σ_{k<n} [n choose k]₂`:

| Algebra | n | Proper nontrivial subloops |
|---------|:-:|----------------------------|
| 𝕆 | 3 | 1 + 7 + 7 = 15 |
| 𝕊 | 4 | 1 + 15 + 35 + 15 = 66 |
| 32-dim | 5 | 1 + 31 + 155 + 155 + 31 = 373 |

The count depends only on the XOR structure of the indices. The signs decide the finer question,
which is the interesting one computationally: which subloops are associative, alternative, or
isomorphic to one another.

**Ordinary and defective lines** (Saniga, Holweck and Pracna). With the units labelled
`1, …, 2ⁿ − 1`, a line `{a, b, c}` with `a < b < c` is *ordinary* if `a + b = c` as integers and
*defective* otherwise. For the sedenions, 25 of the 35 lines are ordinary and 10 are defective, and
the 10 defective lines, with the 10 points lying on them, form a Desargues configuration. This
depends on the binary labelling, and is directly testable.

References:
- M. Saniga, F. Holweck, P. Pracna, "From Cayley-Dickson Algebras to Combinatorial Grassmannians",
  *Mathematics* 3 (2015) 1192–1221, doi:10.3390/math3041192 (open access, CC BY 4.0); preprint
  "Cayley-Dickson Algebras and Finite Geometry", arXiv:1405.6888.
- D. C. Agourakis, M. Gerenutti, "A Dual Pathway to 168" (preprint, 2026),
  doi:10.22541/au.177507195.52530214/v1.

### More algebras from the same kernel

| Algebra | How it would be built | Notes |
|---------|-----------------------|-------|
| Generalized quaternion algebras `(a, b)_F` | `CayleyDickson.withSigma` twice | Available through the kernel and tested for `(2, 3)_ℚ`, but with no named type, norm, or splitting test (Hilbert symbol). |
| Dual quaternions `ℍ ⊗ 𝔻` | `Quaternion<Dual<Real>>` | Rigid motions of 3-space. `QuaternionAlgebras.quaternionRing(base)` already accepts any commutative ring, so this is naming, the dual conjugations and tests. |
| Split-biquaternions `ℍ ⊗ (ℝ ⊕ ℝ) ≅ ℍ ⊕ ℍ` | `Quaternion<SplitComplex<Real>>` | Same remark. |
| Dual-complex numbers, hyperbolic quaternions | | The hyperbolic quaternions (Macfarlane) are nonassociative and are *not* a Cayley–Dickson double. |
| Tricomplex and general multicomplex numbers | iterate `CayleyDickson.commutative` | Design notes in `docs/design/Multicomplex.md`. |
| Clifford algebras `Cl(p, q)` | new construction | Would unify several algebras here: `Cl(0,1) ≅ ℂ`, `Cl(1,0) ≅` split-complex, `Cl(0,2) ≅ ℍ`, `Cl(1,1) ≅ Cl(2,0) ≅ ℍ′`, `Cl(3,0) ≅ ℍ ⊗ ℂ`, `Cl(0,3) ≅ ℍ ⊕ ℍ`. |

### Gaps in the algebras that exist

- **Split-octonions:** the Zorn vector-matrix representation; the family of embeddings `ℍ ↪ 𝕆′` and
  `ℍ′ ↪ 𝕆′` beyond the canonical ones; the isomorphism `CD_usual(ℍ′) ≅ CD_split(ℍ)`.
- **Biquaternions and bioctonions:** the Lorentz-group action of the biquaternions; a generic
  printable for complex coefficients (only the real ones have one); the matrix isomorphism
  `Biquaternion(F) ≅ M₂(Complex(F))` over a general field (only the real one is provided).
- **Bicomplex numbers:** the idempotent decomposition and `isUnit` / null-cone predicates are
  real-only; over a general base only the two idempotent projections are provided.
- **Split forms over ℤ and ℚ:** these work today through the generic `SplitQuaternion<R>` and
  `SplitOctonion<R>`, but have no named `*Algebras` objects, orders, or unit groups.
- **Split-complex numbers:** the idempotent (diagonal) coordinates exist for ℝ only, not for a
  generic base.
- **Eisenstein integers:** the monomorphisms `ℤ[ω] ↪ 𝓗` (see
  [above](#eisenstein-integers-and-the-quaternion-orders)).
- **Integral octonions:** the Gravesian octonions are implemented, but not the maximal orders
  (the Cayley integers / octavians, whose lattice is E₈), nor the Kleinian or double-Hurwitzian
  orders.

### Structure and laws

- **Type-level structure.** "Alternative" and "composition algebra" are verified by tests but are
  not interfaces: there is no `AlternativeAlgebra` or `CompositionAlgebra` type, and no bundled
  `LawSuite` for them (the specs call `AlternativityLaw`, `MoufangLaw` and
  `NormSqMultiplicativeLaw` directly).
- **Isotropic norms.** `NormedDivisionAlgebra` covers the division tower only. The split and
  complexified algebras expose `HasNormSq` and `HasReciprocal` separately, with no shared interface
  for "composition algebra with a possibly isotropic norm".
- **Unit groups and automorphisms.** The 24 Hurwitz units, the unit quaternions as `SU(2)` and
  their action as rotations, `Aut(𝕆) = G₂`, and the Moufang loop of unit octonions.
- **Transcendental functions.** `exp`, `log`, powers and polar decomposition exist for dual numbers
  only.
- **Sedenion zero divisors and the number 168.** The preprint "A Dual Pathway to 168" relates the
  168 nonassociative octonion triples above to 168 pairs of primitive zero divisors in the
  sedenions, by a `GL(3, 2)`-equivariant bijection. The octonion side is now verified here; the
  sedenion side needs the named sedenions first.
- **Octonion matrices** (also listed in the top-level `TODO.md`), and with them the exceptional
  Jordan algebra.


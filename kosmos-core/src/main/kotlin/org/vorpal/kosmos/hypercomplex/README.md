# Hypercomplex Number Systems

This package implements the classical tower of normed division algebras
built by iterated Cayley–Dickson doubling, together with their
integer- and rational-coefficient subrings:

$$
\mathbb{R} \;\subset\; \mathbb{C} \;\subset\; \mathbb{H} \;\subset\; \mathbb{O}
$$

At each level, the algebraic structure weakens:
$\mathbb{C}$ loses ordering,
$\mathbb{H}$ loses commutativity,
$\mathbb{O}$ loses associativity.
Every level retains a multiplicative norm ($|ab| = |a||b|$) and
hence a division algebra structure.

---

## One Construction, Many Algebras

Every algebra in this package is carried by the Cayley–Dickson pair `CD<A>` and gets its
arithmetic from `CayleyDickson` (in `algebra.structures`). Nothing re-implements a doubling
product by hand. The variations come from two choices: the involution on the base, and `σ`.

**Base with a nontrivial involution** (`CayleyDickson.usual` / `split`) gives the
noncommutative tower:

| Algebra | Carrier | Construction |
|---------|---------|--------------|
| ℍ, ℍ_ℚ, Lip | `Quaternion`, `RationalQuaternion`, `LipschitzQuaternion` (typealiases of `CD<…>`) | `CD⟨ℂ⟩`, `CD⟨ℚ(i)⟩`, `CD⟨ℤ[i]⟩` |
| 𝕆, 𝕆_ℚ, Gravesian | `Octonion`, `RationalOctonion`, `GravesianOctonion` | `CD⟨ℍ⟩`, `CD⟨ℍ_ℚ⟩`, `CD⟨Lip⟩` |

**Commutative base with the *trivial* involution** (`CayleyDickson.commutative(base, σ)`)
gives the commutative quadratic extension `R[x]/(x² + σ)`:

| σ | x² | Algebra | Carrier |
|---|----|---------|---------|
| 1  | −1 | ℂ = ℝ[i]/(i² + 1) | `Complex` (typealias of `CD<Real>`) |
| 1  | −1 | 𝔹 = ℂ[j]/(j² + 1) (bicomplex) | `Bicomplex` (value class over `CD<Complex>`) |
| −1 | +1 | split-complex `R[j]/(j² − 1)` | `SplitComplex<R>` (typealias of `CD<R>`) |
| 0  | 0  | dual numbers `F[ε]/(ε²)` | `Dual<F>` (value class over `CD<F>`) |

`Bicomplex` and `Dual` are value classes, not typealiases. `Bicomplex` and `Quaternion` are
both carried by `CD<Complex>`, and `Dual<Real>` shares `CD<Real>` with `Complex`. The algebras
differ, so the Kotlin types must too. Their ring structures are transported from the
Cayley–Dickson ring along the wrapper with `CarrierTransport`.

**Orders are substructures, not new implementations.** A `HurwitzQuaternion` is an element of
the Hurwitz order `𝓗 ⊂ ℍ_ℚ`, represented by its underlying `RationalQuaternion`. Its
constructor is private: checked construction goes through `HurwitzQuaternion(q)`, and the ring
operations, which provably stay in `𝓗`, use a trusted internal `wrap`. The ring structure of `𝓗`
is the restriction of `ℍ_ℚ`. Only the 24 units are invertible *in* `𝓗`; every other nonzero
element is inverted in `ℍ_ℚ` after the inclusion `HurwitzToRationalQuaternionMonomorphism`.

---

## Package Layout

```
hypercomplex/
├── complex/                  # ℂ = CD⟨ℝ⟩ (commutative doubling, σ = 1)
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
│   └── Multicomplex.md             design notes for the general multicomplex case
│
├── quaternion/               # ℍ-like rings
│   ├── Quaternion.kt               ℍ   = CD⟨ℂ⟩             (Real coefficients)
│   ├── QuaternionAlgebras.kt
│   ├── RationalQuaternion.kt       ℍ_ℚ = CD⟨ℚ(i)⟩          (Rational coefficients)
│   ├── RationalQuaternionAlgebras.kt
│   ├── LipschitzQuaternion.kt      Lip = CD⟨ℤ[i]⟩          (BigInteger coefficients)
│   ├── LipschitzQuaternionAlgebras.kt
│   ├── HurwitzQuaternion.kt        𝓗 ⊂ ℍ_ℚ, the Hurwitz order (value class over RationalQuaternion)
│   ├── HurwitzQuaternionAlgebras.kt
│   ├── AxisSignEmbeddings.kt       the 6 specs for complex-like ↪ quaternion-like
│   ├── QuaternionEmbeddingKit.kt   generic complex-like → quaternion builder
│   └── QuaternionPrintable.kt
│
└── octonion/                 # 𝕆-like rings
    ├── Octonion.kt                 𝕆   = CD⟨ℍ⟩             (Real coefficients)
    ├── OctonionAlgebras.kt
    ├── RationalOctonion.kt         𝕆_ℚ = CD⟨ℍ_ℚ⟩           (Rational coefficients)
    ├── RationalOctonionAlgebras.kt
    ├── GravesianOctonion.kt        CD⟨Lip⟩ ≅ ℤ⁸            (BigInteger coefficients)
    ├── GravesianOctonionAlgebras.kt
    ├── OctonionEmbeddingKit.kt     generic quaternion → octonion builder (Fano plane)
    ├── OctonionFanoOrientation(s).kt, OrientedFanoLine.kt, Handedness.kt
    └── OctonionPrintable.kt
```

The Gaussian integers `ℤ[i]`, Gaussian rationals `ℚ(i)` and Eisenstein integers `ℤ[ω]` live in
`numberfields.quadratic`.

Bases are derived rather than written out: `CayleyDickson.doubledBasis(zero, baseBasis)` doubles
a basis `e₀, …, e_{n-1}` of `A` to `(e₀, 0), …, (0, e_{n-1})` in `CD<A>`, which reproduces
`1, i, j, k` for ℍ and `1, e₁, …, e₇` for 𝕆. Each quaternion and octonion ring exposes it as
`basis`.

---

## The Tower Diagram

All arrows are ring monomorphisms (structure-preserving injections).
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
                        │  ╲  (×84)               │  ╲  (×84)             │  ╲  (×84)
                        │   ╲                     │   ╲                   │   ╲
                        ↓    ↘                    ↓    ↘                  ↓    ↘
Level 3 (octonion)      Grav ─────────────────→ 𝕆_ℚ ─ ─ ─ ─ ─ ─ ─ ─ → 𝕆
```

Dashed arrows (─ ─ →) pass through floating-point conversion (`toReal()`)
and are therefore not exact monomorphisms in the strict algebraic sense.

The Eisenstein integers $\mathbb{Z}[\omega]$ sit beside $\mathbb{Z}[i]$ at Level 1
with a homomorphism $\mathbb{Z}[\omega] \to \mathbb{C}$. They embed into the Hurwitz order,
but not into the Lipschitz quaternions (see [below](#eisenstein-integers-and-the-quaternion-orders)).

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

## Embeddings: Quaternion → Octonion (×84)

### The Idea

A quaternion $w + xi + yj + zk$ can be embedded into the octonions
by choosing which three of the seven imaginary octonion units
$e_1, \ldots, e_7$ will serve as the images of $i$, $j$, $k$.

The constraint is that the chosen triple must form a **line in the
Fano plane** $\text{PG}(2,2)$ — the unique $(7,3,1)$-design on 7 points —
so that the quaternion multiplication table is respected:

$$
\varphi(i) \cdot \varphi(j) = \varphi(k)
$$

The Fano plane has **7 lines**.
For each line $\{p, q, r\}$, there are **6 ordered pairs** $(i \mapsto e_p,\; j \mapsto e_q)$
(the third index is forced).
For each ordered pair, there are **2 handedness choices**: whether $\varphi(i) = +e_p$ or $-e_p$.

$$
\text{Total:}\quad 7 \times 6 \times 2 = 84 \text{ embeddings}
$$

### How to Create Them

Each octonion-level Algebras object contains an `OctonionEmbeddingKit`
instance and an `allQuaternionEmbeddings()` / `allEmbeddings()` method.

#### ℍ ↪ 𝕆  (Real coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.Handedness

// === Method 1: Create one embedding by spec ===

val (spec, phi) = OctonionAlgebras.embeddingKit.createEmbedding(
    iIndex = 1,                    // i ↦ e₁
    jIndex = 2,                    // j ↦ e₂
    handedness = Handedness.RIGHT  // positive orientation
)
// spec.k == 3, spec.kSign == +1   (forced: k ↦ +e₃)

val q = quaternion(1.0, 2.0, 3.0, 4.0)
val o = phi(q)  // 1 + 2e₁ + 3e₂ + 4e₃

// === Method 2: Enumerate all 84 ===

val all: Map<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Quaternion, Octonion>>
    = OctonionAlgebras.allQuaternionEmbeddings()

assert(all.size == 84)

// Find a specific one by filtering:
val (_, phiCustom) = all.entries.first { (s, _) ->
    s.i == 3 && s.j == 5 && s.handedness == Handedness.LEFT
}
```

#### Lip ↪ Grav  (BigInteger coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.GravesianOctonionAlgebras

// All 84 Lipschitz quaternion → Gravesian octonion embeddings:
val all = GravesianOctonionAlgebras.allQuaternionEmbeddings()

// Or one at a time:
val (spec, phi) = GravesianOctonionAlgebras.embeddingKit.createEmbedding(
    iIndex = 4, jIndex = 6, handedness = Handedness.RIGHT
)
```

#### ℍ_ℚ ↪ 𝕆_ℚ  (Rational coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.RationalOctonionAlgebras

val all = RationalOctonionAlgebras.allEmbeddings()
```

### The `OctonionEmbeddingSpec`

Each embedding is identified by an `OctonionEmbeddingSpec`:

```kotlin
data class OctonionEmbeddingSpec(
    val i: Int,                        // which e_n does quaternion i map to
    val j: Int,                        // which e_n does quaternion j map to
    val k: Int,                        // forced: third point on the Fano line
    val handedness: Handedness,        // RIGHT: i ↦ +e_i, LEFT: i ↦ -e_i
    val kSign: Int                     // +1 or -1: φ(k) = kSign · e_k
)
```

The `kSign` is not a free parameter — it is *computed* from the
octonion multiplication table and the Fano plane structure, then
recorded in the spec for inspection.

### The Canonical Embedding

The canonical embedding has `iIndex=1, jIndex=2, handedness=RIGHT`,
giving $i \mapsto e_1,\; j \mapsto e_2,\; k \mapsto e_3$ with all
positive signs.  This matches `Quaternion.asOctonion()` and the
`CayleyDickson.canonicalEmbedding()` utility.

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

The ×6 and ×84 embedding families *include* the canonical embedding
as a special case (the one with axis=I, sign=PLUS for ×6;
iIndex=1, jIndex=2, handedness=RIGHT for ×84).

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

These scalar actions are required by the `OctonionEmbeddingKit`
to construct embeddings via linear combination of basis elements.

---

## Quick Reference: All Monomorphisms

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
| `Lip ↪ ℍ` | `LipschitzQuaternionAlgebras.LipschitzToQuaternionMonomorphism` |
| `𝓗 ↪ ℍ` | `HurwitzQuaternionAlgebras.HurwitzToQuaternionMonomorphism` |
| `ℍ_ℚ → ℍ` | `RationalQuaternionAlgebras.RationalQuaternionToQuaternionMonomorphism` |
| `𝔹 → ℂ` (×2) | `BicomplexAlgebras.BicomplexFirstProjectionHomomorphism`, `…SecondProjectionHomomorphism` |

### Level 2 → Level 3 (×84 families + canonical)

| Arrow | Location |
|-------|----------|
| `ℍ ↪ 𝕆` (×84) | `OctonionAlgebras.allQuaternionEmbeddings()` |
| `Lip ↪ Grav` (×84) | `GravesianOctonionAlgebras.allQuaternionEmbeddings()` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (×84) | `RationalOctonionAlgebras.allEmbeddings()` |
| `ℍ ↪ 𝕆` (canonical) | `OctonionAlgebras.QuaternionToOctonionMonomorphism` |
| `Lip ↪ Grav` (canonical) | `LipschitzQuaternionAlgebras.LipschitzToGravesianOctonionMonomorphism` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (canonical) | `RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism` |

### Within Level 3 and Cross-Level

| Arrow | Location |
|-------|----------|
| `Grav ↪ 𝕆_ℚ` | `GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism` |
| `Lip ↪ 𝕆_ℚ` | `LipschitzQuaternionAlgebras.LipschitzToRationalOctonionMonomorphism` |
| `𝓗 ↪ 𝕆_ℚ` | `RationalOctonionAlgebras.HurwitzToRationalOctonionMonomorphism` |
| `Grav ↪ 𝕆` | `GravesianOctonionAlgebras.GravesianToOctonionMonomorphism` |
| `𝕆_ℚ → 𝕆` | `RationalOctonionAlgebras.RationalToOctonionMonomorphism` |

### Matrix representations

| Arrow | Location |
|-------|----------|
| `ℂ ↪ M₂(ℝ)` | `ComplexAlgebras.ComplexToRank2RMatrixMonomorphism` |
| `SplitComplex<R> ↪ M₂(R)` | `SplitComplexAlgebras.splitComplexToRank2MatrixMonomorphism(base)` |
| `Dual<F> ↪ M₂(F)` | `DualAlgebras.dualToRank2MatrixMonomorphism(base)` |
| `𝔹 ↪ M₂(ℂ)` | `BicomplexAlgebras.BicomplexToComplexMatrixMonomorphism`, `…ToDiagonalMatrixMonomorphism` |
| `ℍ ↪ M₂(ℂ)` | `QuaternionAlgebras.QuaternionToComplexMatrixMonomorphism` |

The first four are all `quadraticRank2MatrixEmbedding`: the regular representation of
`R[x]/(x² − s − tx)` on the basis `{1, x}`.

---

## The Fano Plane

The 84 octonion embeddings are governed by the Fano plane, the unique
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
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

## Package Layout

```
hypercomplex/
├── complex/                  # Level 1: the complex-like rings
│   ├── Complex.kt                  ℂ  = CD⟨ℝ⟩           (Real coefficients)
│   ├── ComplexAlgebras.kt
│   ├── GaussianInt.kt              ℤ[i]                  (BigInteger coefficients)
│   ├── GaussianIntAlgebras.kt
│   ├── GaussianRat.kt              ℚ(i)                  (Rational coefficients)
│   ├── GaussianRatAlgebras.kt
│   ├── EisensteinInt.kt            ℤ[ω], ω = e^{2πi/3}  (BigInteger coefficients)
│   └── EisensteinIntAlgebras.kt
│
├── quaternion/               # Level 2: the quaternion-like rings
│   ├── Quaternion.kt               ℍ  = CD⟨ℂ⟩           (Real coefficients)
│   ├── QuaternionAlgebras.kt
│   ├── LipschitzQuaternion.kt      Lip = CD⟨ℤ[i]⟩       (BigInteger coefficients)
│   ├── LipschitzQuaternionAlgebras.kt
│   ├── HurwitzQuaternion.kt        Hur ⊃ Lip             (Rational half-integer coefficients)
│   ├── HurwitzQuaternionAlgebras.kt
│   ├── RationalQuaternion.kt       ℍ_ℚ = CD⟨ℚ(i)⟩       (Rational coefficients)
│   └── RationalQuaternionAlgebras.kt
│
├── octonion/                 # Level 3: the octonion-like rings
│   ├── Octonion.kt                 𝕆  = CD⟨ℍ⟩           (Real coefficients)
│   ├── OctonionAlgebras.kt
│   ├── CayleyOctonion.kt           Cay = CD⟨Lip⟩         (BigInteger coefficients)
│   ├── CayleyOctonionAlgebras.kt
│   ├── RationalOctonion.kt         𝕆_ℚ = CD⟨ℍ_ℚ⟩        (Rational coefficients)
│   └── RationalOctonionAlgebras.kt
│
├── embeddings/               # Embedding machinery
│   ├── AxisSignEmbeddings.kt       6-embedding specs for complex ↪ quaternion
│   ├── QuaternionEmbeddingKit.kt   Generic complex-like → quaternion builder
│   └── OctonionEmbeddingKit.kt     Generic quaternion → octonion builder (Fano plane)
│
├── CayleyDickson.kt          # The doubling construction itself
└── HyperComplex.kt           # Shared types (Handedness enum, etc.)
```

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
Level 3 (octonion)      Cay ──────────────────→ 𝕆_ℚ ─ ─ ─ ─ ─ ─ ─ ─ → 𝕆
```

Dashed arrows (─ ─ →) pass through floating-point conversion (`toReal()`)
and are therefore not exact monomorphisms in the strict algebraic sense.

The Eisenstein integers $\mathbb{Z}[\omega]$ sit beside $\mathbb{Z}[i]$ at Level 1
with a homomorphism $\mathbb{Z}[\omega] \to \mathbb{C}$, but they do **not** embed
into any quaternion subring (see [below](#why-eisenstein-integers-dont-embed-into-quaternions)).

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

Every quaternion-level Algebras object exposes a factory function that
takes an `AxisSignEmbedding` parameter (defaulting to canonical: $i_{\mathbb{C}} \mapsto +i$).

#### ℂ ↪ ℍ  (Real coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.*
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras

// Canonical: i_C ↦ +i
val canonical = QuaternionAlgebras.complexEmbeddingToQuaternion()

// Pick a specific one: i_C ↦ -k
val emb = AxisSignEmbedding(ImagAxis.K, Sign.MINUS)
val phiMinusK = QuaternionAlgebras.complexEmbeddingToQuaternion(emb)

// Use it:
val z = complex(3.0, 4.0)
val q = phiMinusK(z)   // quaternion(3.0, 0.0, 0.0, -4.0)
```

#### ℤ[i] ↪ Lip  (BigInteger coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternionAlgebras

// Canonical: i ↦ +i
val phi = LipschitzQuaternionAlgebras.gaussianIntEmbeddingToQuaternion()

// Along j-axis with positive sign: i ↦ +j
val phiJ = LipschitzQuaternionAlgebras.gaussianIntEmbeddingToQuaternion(
    AxisSignEmbedding(ImagAxis.J, Sign.PLUS)
)
```

#### ℚ(i) ↪ ℍ_ℚ  (Rational coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras

val phi = RationalQuaternionAlgebras.gaussianRatToQuaternionMonomorphism()
```

#### ℤ[i] ↪ Hur  (via Lipschitz, then inclusion)

```kotlin
import org.vorpal.kosmos.hypercomplex.quaternion.HurwitzQuaternionAlgebras

// Composes: ℤ[i] ↪ Lip ↪ Hur
val phi = HurwitzQuaternionAlgebras.gaussianIntEmbeddingToHurwitz(
    AxisSignEmbedding(ImagAxis.I, Sign.PLUS)
)
```

#### Enumerating All 6

```kotlin
AxisSignEmbedding.all.forEach { emb ->
    val phi = QuaternionAlgebras.complexEmbeddingToQuaternion(emb)
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
import org.vorpal.kosmos.hypercomplex.HyperComplex.Handedness

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

#### Lip ↪ Cay  (BigInteger coefficients)

```kotlin
import org.vorpal.kosmos.hypercomplex.octonion.CayleyOctonionAlgebras

// All 84 Lipschitz quaternion → Cayley octonion embeddings:
val all = CayleyOctonionAlgebras.allQuaternionEmbeddings()

// Or one at a time:
val (spec, phi) = CayleyOctonionAlgebras.embeddingKit.createEmbedding(
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

## Why Eisenstein Integers Don't Embed into Quaternions

The Eisenstein integers $\mathbb{Z}[\omega]$ where $\omega = e^{2\pi i/3}$
satisfy $\omega^2 + \omega + 1 = 0$.  For a unital ring monomorphism
$\varphi : \mathbb{Z}[\omega] \hookrightarrow \mathbb{H}_\mathbb{Z}$ (or any
quaternion ring) to exist, we would need a quaternion $u = \varphi(\omega)$
satisfying:

$$
u^2 + u + 1 = 0 \quad\Longleftrightarrow\quad u = \frac{-1 + \sqrt{-3}}{2}
$$

This requires half-integer coefficients *and* $\sqrt{-3}$ as a
quaternion — neither of which lives in the Lipschitz integers, and even
in the Hurwitz ring the equation $u^2 + u + 1 = 0$ has no solution.

The only available path from Eisenstein integers upward is the
floating-point homomorphism:

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
| $\mathbb{Z}[i] \hookrightarrow \text{Lip}$ | `LipschitzQuaternionAlgebras.gaussianIntEmbeddingToQuaternion()` (canonical default) |
| $\text{Lip} \hookrightarrow \text{Cay}$     | `CayleyOctonionAlgebras.LipschitzToCayleyMonomorphism`       |
| $\mathbb{Q}(i) \hookrightarrow \mathbb{H}_\mathbb{Q}$ | `RationalQuaternionAlgebras.gaussianRatToQuaternionMonomorphism()` (canonical default) |
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
| Cay  | $\mathbb{Z}$ | `ZModule` |
| $\mathbb{O}_\mathbb{Q}$ | $\mathbb{Q}$ | `FiniteVectorSpace` (dim 8) |
| $\mathbb{O}$     | $\mathbb{R}$ | `FiniteVectorSpace` (dim 8) |

These scalar actions are required by the `OctonionEmbeddingKit`
to construct embeddings via linear combination of basis elements.

---

## Quick Reference: All Monomorphisms

### Level 0 → Level 1

| Arrow | Location |
|-------|----------|
| `ℤ ↪ ℤ[i]` | `GaussianIntAlgebras.ZToGaussianIntMonomorphism` |
| `ℤ[i] ↪ ℂ` | `GaussianIntAlgebras.GaussianIntToComplexMonomorphism` |
| `ℚ ↪ ℚ(i)` | `GaussianRatAlgebras.QtoGaussianRatMonomorphism` |
| `ℤ[i] ↪ ℚ(i)` | `GaussianRatAlgebras.GaussianIntToRatMonomorphism` |
| `ℤ ↪ ℚ(i)` | `GaussianRatAlgebras.ZToGaussianRatMonomorphism` |
| `ℚ(i) ↪ ℂ` | `GaussianRatAlgebras.GaussianRatToComplexMonomorphism` |
| `ℝ ↪ ℂ` | `ComplexAlgebras.RealToComplexMonomorphism` |
| `ℤ[ω] → ℂ` | `EisensteinIntAlgebras.EisensteinIntToCHomomorphism` |

### Level 1 → Level 2 (×6 families)

| Arrow | Location |
|-------|----------|
| `ℂ ↪ ℍ` (×6) | `QuaternionAlgebras.complexEmbeddingToQuaternion(emb)` |
| `ℤ[i] ↪ Lip` (×6) | `LipschitzQuaternionAlgebras.gaussianIntEmbeddingToQuaternion(emb)` |
| `ℚ(i) ↪ ℍ_ℚ` (×6) | `RationalQuaternionAlgebras.gaussianRatToQuaternionMonomorphism(emb)` |
| `ℤ[i] ↪ Hur` (×6) | `HurwitzQuaternionAlgebras.gaussianIntEmbeddingToHurwitz(emb)` |

### Within Level 2

| Arrow | Location |
|-------|----------|
| `Lip ↪ Hur` | `HurwitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism` |
| `Hur ↪ ℍ_ℚ` | `RationalQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism` |
| `Lip ↪ ℍ_ℚ` | `RationalQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism` |
| `Lip ↪ ℍ` | `HurwitzQuaternionAlgebras.LipschitzToQuaternionMonomorphism` |
| `Hur ↪ ℍ` | `HurwitzQuaternionAlgebras.HurwitzToQuaternionMonomorphism` |
| `ℍ_ℚ ↪ ℍ` | `RationalQuaternionAlgebras.RationalQuaternionToQuaternionMonomorphism` |

### Level 2 → Level 3 (×84 families + canonical)

| Arrow | Location |
|-------|----------|
| `ℍ ↪ 𝕆` (×84) | `OctonionAlgebras.allQuaternionEmbeddings()` |
| `Lip ↪ Cay` (×84) | `CayleyOctonionAlgebras.allQuaternionEmbeddings()` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (×84) | `RationalOctonionAlgebras.allEmbeddings()` |
| `ℍ ↪ 𝕆` (canonical) | `OctonionAlgebras.QuaternionToOctonionMonomorphism` |
| `Lip ↪ Cay` (canonical) | `CayleyOctonionAlgebras.LipschitzToCayleyMonomorphism` |
| `ℍ_ℚ ↪ 𝕆_ℚ` (canonical) | `RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism` |

### Within Level 3 and Cross-Level

| Arrow | Location |
|-------|----------|
| `Cay ↪ 𝕆_ℚ` | `RationalOctonionAlgebras.CayleyToRationalOctonionMonomorphism` |
| `Lip ↪ 𝕆_ℚ` | `RationalOctonionAlgebras.LipschitzToRationalOctonionMonomorphism` |
| `Hur ↪ 𝕆_ℚ` | `RationalOctonionAlgebras.HurwitzToRationalOctonionMonomorphism` |
| `Cay ↪ 𝕆` | `CayleyOctonionAlgebras.CayleyToOctonionMonomorphism` |
| `𝕆_ℚ ↪ 𝕆` | `RationalOctonionAlgebras.RationalToOctonionMonomorphism` |

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
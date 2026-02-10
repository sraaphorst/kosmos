# org.vorpal.kosmos.analysis

The **analysis** package in **Kosmos** provides coordinate-free (but code-friendly) abstractions for

- **Scalar fields**: functions \(f: V \to F\)
- **Vector fields**: functions \(X: V \to V\)
- **Covectors** (intended linear maps): \(\omega: V \to F\)
- **Covector fields** (1-forms): \(\omega: V \to V^*\)

All concepts carry a `VectorSpace<F, V>` witness from `org.vorpal.kosmos.algebra.structures`. Carriers (vectors/matrices) are **dumb values**; algebra lives in the witnesses.

---

## Core concepts

| Concept | Math | Kotlin | Notes |
|---|---|---|---|
| Scalar field | \(f: V \to F\) | `ScalarField<F, V>` | Pointwise addition/multiplication form a commutative ring. |
| Vector field | \(X: V \to V\) | `VectorField<F, V>` | Pointwise addition makes an abelian group. Scalar fields act on vector fields pointwise. |
| Covector | \(\omega: V \to F\) | `Covector<F, V>` | Intended to be linear; linearity is not enforced by the type. |
| Covector field | \(\omega: V \to V^*\) | `CovectorField<F, V>` | A field of covectors (1-forms). |

---

## The key relationships

### Differential
Given a scalar field \(f\), its **differential** is a covector field \(df\) such that

\[
(df)_p(v) = D_v f(p)
\]

In Kosmos (currently), `Derivative` computes this via central differences.

### Pairing
A covector field \(\omega\) can be applied to a vector field \(X\) to produce a scalar field:

\[
\langle \omega, X \rangle(p) = \omega(p)(X(p))
\]

In code this is the operator: `omega(X) : ScalarField`.

---

## Relationships diagram

```
ScalarField f : V -> F
        |
        |  Differential (df)
        v
CovectorField df : V -> V*
        |
        |  Pairing with VectorField X
        v
ScalarField <df, X> : V -> F
```

---

## Code example (DenseVec + DenseVectorSpace)

This example uses the canonical "Rn" carrier in Kosmos: `DenseVec<Real>`.

```kotlin
import org.vorpal.kosmos.algebra.structures.instances.base.RealAlgebras
import org.vorpal.kosmos.analysis.*
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.linear.instances.DenseVectorSpace
import org.vorpal.kosmos.core.linear.values.DenseVec

val field = RealAlgebras.RealField
val space = DenseVectorSpace(field, 2)

// f(x, y) = x^2 + y^2
val f: ScalarField<Real, DenseVec<Real>> =
    ScalarField.of(space) { p ->
        val x = p[0]
        val y = p[1]
        x * x + y * y
    }

// Vector field X(x, y) = (-y, x)
val X: VectorField<Real, DenseVec<Real>> =
    VectorField.of(space) { p ->
        val x = p[0]
        val y = p[1]
        DenseVec.of(-y, x)
    }

// df via finite differences (central difference)
val df: CovectorField<Real, DenseVec<Real>> =
    with(Derivative) { f.dReal(1e-6) }

// Pairing <df, X> is a scalar field
val g: ScalarField<Real, DenseVec<Real>> = df(X)

val p = DenseVec.of(1.0, 2.0)
val value = g(p)
println(value)
```

---

## Notes on Covectors

- Mathematically, a covector is an element of the **linear dual** \(V^* = \mathrm{Hom}_F(V, F)\).
- In code, representing a covector as a function `V -> F` is the most faithful coordinate-free model.
- Representing \(V^*\) as coefficient vectors \(F^n\) requires extra structure (finite dimension + chosen coordinates/basis). Kosmos can add that later when gradients/Jacobians/Hessians become a focus.

---

## Status

- `gradient` is intentionally not implemented yet: it requires a metric (inner product) and a way to invert it (a `sharp` map), which in practice needs finite-dimensional coordinates or a linear solver.


Here is the former version of this file, some of which is salvageable and useful for moving forward:


# org.vorpal.kosmos.analysis

The **analysis** package in **Kosmos** provides algebraic and geometric abstractions for fields and differential structures — including **scalar fields**, **vector fields**, **covectors**, and **covector fields**.  
It bridges algebraic structures (`Field`, `VectorSpace`) with differential geometry (gradients, differentials, and flows).

---

## Core Concepts

| Concept | Mathematical Type | Kotlin Type | Description |
|----------|------------------|--------------|--------------|
| Scalar Field | f : V → F | `ScalarField<F, V>` | A function assigning a scalar value to each point in a vector space. |
| Vector Field | X : V → V | `VectorField<F, V>` | A function assigning a vector (in the same space) to each point — e.g. a flow or direction field. |
| Covector (1-form) | ω : V → F | `Covector<F, V>` | A **linear** function mapping vectors to scalars — an element of the dual space V*. |
| Covector Field | ω : V → V* | `CovectorField<F, V>` | A function assigning a covector (linear functional) to each point — i.e. a field of 1-forms. |

---

## The Dual Space Intuition

A **vector space** V is a set of arrows that can be added and scaled.

A **dual space** V* is the set of *linear maps* that take a vector and return a number.  
Each such linear map is called a **covector**.

Example (for V = R²):

- Vector: v = (3, 4)
- Covector: ω(x, y) = 2x - y
- Dual pairing: ω(v) = 2·3 - 4 = 2

Vectors describe *directions*; covectors describe *measurements* of those directions.

---

## Comparison Table

| Concept | Type | Depends on point? | Linear? | Example |
|----------|------|------------------|----------|----------|
| ScalarField | f : V → F | ✅ Yes | ❌ No | f(x, y) = x² + y² |
| Covector | ω : V → F | ❌ No | ✅ Yes | ω(x, y) = 2x - y |
| CovectorField | ω : V → V* | ✅ Yes | ✅ (pointwise) | ω(x, y)(v) = 2x·vₓ + 2y·vᵧ |
| VectorField | X : V → V | ✅ Yes | — | X(x, y) = (−y, x) |

---

## Differential Relationship

Given a scalar field f : V → F, its **differential** (or gradient) is a covector field df : V → V*.  
At each point p, df(p) is a covector that returns the *directional derivative* of f at p in direction v:

    (df)_p(v) = Df(p)[v]

This defines the relationship:

    f : V → F    →    df : V → V*

---

## Geometric Intuition

- **Vector:** an arrow — direction and magnitude.
- **Covector:** a measuring plane — assigns a value to each direction.
- **Scalar field:** a height map — assigns a number to each point.
- **Covector field:** a field of measuring planes — how the scalar field changes in each direction.
- **Vector field:** a field of arrows — describes motion, flow, or direction at each point.

---

## Relationships Diagram

```
                 +-----------------------+
                 |     ScalarField f     |
                 |   f : V → F           |
                 +-----------+-----------+
                             |
                        Differential (df)
                             ↓
                 +-----------------------+
                 |   CovectorField ω     |
                 |   ω : V → V*          |
                 +-----------+-----------+
                             |
                     Dual pairing ⟨ω, X⟩
                             ↓
                 +-----------------------+
                 |     VectorField X     |
                 |   X : V → V           |
                 +-----------------------+
```

In shorthand:

    f : V → F  →  df : V → V*
    ⟨df, X⟩ = X(f)

---

## Duality Summary

| Space | Dual Space | Element | Interpretation |
|--------|-------------|----------|----------------|
| V | V* | v | Vector |
| V* | V** ≅ V | ω | Covector |
| F^V | (F^V)* ≅ V* | f | Scalar field |
| V^V | (V^V)* | X | Vector field |

---

## Code Example

```kotlin
val f = ScalarField.of(RealVectorSpace2D) { v -> v.x * v.x + v.y * v.y }

val df = CovectorField.of(RealVectorSpace2D) { p ->
    Covector.of(RealVectorSpace2D) { v -> 2 * p.x * v.x + 2 * p.y * v.y }
}

val X = VectorField.of(RealVectorSpace2D) { v -> Vec2R(-v.y, v.x) }

// Evaluate ⟨df, X⟩ at a point:
val value = df(Vec2R(1.0, 2.0))(X(Vec2R(1.0, 2.0))) // directional derivative
```

---

## Further Reading

- Spivak, *Calculus on Manifolds*
- Lee, *Introduction to Smooth Manifolds*
- Warner, *Foundations of Differentiable Manifolds and Lie Groups*

---

**In summary:**
- A ScalarField maps points to numbers.
- Its differential is a CovectorField (a field of linear functionals).
- A VectorField maps points to vectors.
- Covectors and vectors live in dual spaces, connected by the dual pairing ⟨ω, v⟩.

---

**Kosmos Analysis** - bringing algebraic precision to geometric intuition.


# Scalar, Vector, and Covector Fields in Kosmos

## Conceptual Overview

| Concept | Symbolic Type | Kotlin Type | Description |
|----------|----------------|--------------|--------------|
| Scalar Field | f : V → 𝔽 | `ScalarField<F, V>` | Assigns a scalar to each point. |
| Vector Field | X : V → V | `VectorField<F, V>` | Assigns a tangent vector to each point. |
| Covector Field | ω : V → V* | `CovectorField<F, V>` | Assigns a covector (linear functional) to each point. |

---

## Key Relationships

| Operation | Result | Type | Description |
|------------|----------|------|--------------|
| ω(X) | `ScalarField<F,V>` | Covector acting on vector. |
| df | `CovectorField<F,V>` | Differential of a scalar field. |
| grad(f) | `VectorField<F,V>` | Gradient (via inner product or metric). |

---

## Geometric Diagram

```
            ScalarField f : V → 𝔽
                     │
                     │  differential
                     ▼
             CovectorField df : V → V*
                     │
              (sharp via metric or inner product)
                     ▼
             VectorField grad(f) : V → V
```

At each point `p ∈ V`:

```
df(p) : V → 𝔽     (a covector)
grad(f)(p) ∈ V     (a vector)
df(p)(v) = ⟨grad(f)(p), v⟩
```

---

## Two Approaches to `grad(f)`

There are two conceptually equivalent but structurally distinct approaches in Kosmos:

### 1. **Via the Inner Product Space**
When your `VectorSpace` implements `InnerProductSpace`, the `flat()` and `sharp()` operations are defined geometrically:

```kotlin
fun <F : Any, V : InnerProductSpace<F, V>> gradient(
    f: ScalarField<F, V>,
    derivative: (V, (V) -> F) -> Covector<F, V>
): VectorField<F, V> =
    VectorFields.of(f.space) { p ->
        val dfp = derivative(p, f::invoke)
        p.sharp(dfp) // lift covector to vector via inner product
    }
```

This is the most elegant formulation, used when the geometry (metric) is intrinsic to the vector space.

---

### 2. **Via an Explicit Metric**
When your space is not necessarily an `InnerProductSpace`, you can specify a metric tensor manually:

```kotlin
fun <F : Any, V : Any> gradient(
    f: ScalarField<F, V>,
    metric: (V) -> (V, V) -> F
): VectorField<F, V> =
    VectorFields.of(f.space) { p ->
        val g = metric(p)
        val df = differential(f) { point, func ->
            // Build Covector<F, V> at point using the local derivative of f
            TODO("Implement derivative")
        }
        // Map covector to vector via metric inverse (sharp isomorphism)
        TODO("Implement sharp isomorphism")
    }
```

This version allows dynamic or curved metrics (e.g. Riemannian manifolds).

---

## Code Relationships

```kotlin
val df = differential(f, finiteDifferenceDerivative2D())
val gradF = gradient(f, finiteDifferenceDerivative2D())

val omega: CovectorField<Real, Vec2R> = df
val X: VectorField<Real, Vec2R> = gradF
val scalar: ScalarField<Real, Vec2R> = omega(X)
```

---

## Summary

✅ `InnerProductSpace` defines dot, flat, and sharp (musical isomorphisms).  
✅ `CovectorField.invoke(VectorField)` and `differential()` are functional.  
✅ `gradient()` works via intrinsic or explicit metrics.  
✅ The relationships between all field types are now explicit and geometrically meaningful.

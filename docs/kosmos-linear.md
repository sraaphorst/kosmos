# org.vorpal.kosmos.linear

The **`org.vorpal.kosmos.linear`** package provides the foundational abstractions
and data structures for **linear algebra** within the Kosmos mathematical framework.

It defines the algebraic backbone that supports all higher-level analytic,
geometric, and physical constructs in the system — including vector fields,
covector fields, differential forms, and numerical methods built on continuous
spaces.

---

## Overview

The package is centered on a hierarchy of algebraic interfaces that model
the categorical relationships between fields, modules, and vector spaces.
It then extends these abstractions to specialized vector types such as `Vec2`,
`Vec3`, `Vec4`, and `DenseVec`.

The hierarchy cleanly separates **algebraic** structure (e.g. addition,
scalar multiplication, found in `org.vorpal.kosmos.algebra`) from **metric** or **geometric** structure
(e.g. dot products, norms, distances).  
This design mirrors the mathematical progression from pure vector spaces to
inner product and normed spaces.

---

## Algebraic Hierarchy

| Level | Structure | Interface | Description | New Operations Introduced |
|:------|:-----------|:-----------|:-------------|:---------------------------|
| **1** | Additive Abelian Group | `AbelianGroup<T>` | Defines closure under addition and subtraction with an identity and inverses. | `+`, `−`, `identity` |
| **2** | Module | `RModule<R, M>` | Generalizes vector spaces to modules over a commutative ring. | Scalar multiplication `(r, m) ↦ r·m` |
| **3** | Vector Space | `VectorSpace<F, V>` | A module whose scalars form a field; represents abstract linear algebra. | Same as module, but over `Field<F>` |
| **4** | Inner Product Space | `InnerProductSpace<F, V>` | Introduces a bilinear, symmetric, positive-definite inner product. | `dot(a, b)`, `norm(a)` |
| **5** | Normed Space | `NormedSpace<F, V>` | Adds a norm derived from or independent of an inner product. | `‖v‖`, `distance(a, b)` |
| **6** | Metric Space | `MetricSpace<F, V>` | Defines a metric on the space independent of linearity. | `d(a, b)` |
| **7** | Affine Space | `AffineSpace<F, V>` | Associates points with displacement vectors from the same underlying vector space. | `p + v`, `p − q` |

Each layer is built on the one before it, allowing for modular composition
and categorical generalization.

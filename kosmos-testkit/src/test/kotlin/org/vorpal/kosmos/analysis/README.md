# VectorField Test Suite

## Overview
Comprehensive property-based test suite for `VectorField` using Kotest's property testing framework, with emphasis on module structure over the ring of scalar fields.

## Mathematical Background

### VectorField as a Module
Vector fields `VectorField<F, V>` form an **R-module** where:
- **R** = `ScalarField<F, V>` (the commutative ring of scalar fields)
- **M** = `VectorField<F, V>` (the additive abelian group of vector fields)

This structure captures the key property that:
- Scalar fields can multiply vector fields: `f * X` where `f: V → F` and `X: V → V`
- The result is `(f * X)(p) = f(p) · X(p)` (pointwise scalar multiplication)

## Structure

### Test Implementations
- **`DoubleField`**: Field implementation over Double
- **`Vec2D`**: Simple 2D vector type implementing `VectorSpace<Double, Vec2D>`
- **`Vec2DSpace`**: Complete `VectorSpace` implementation for Vec2D

### Arbitraries (Generators)
- **`arbVectorField()`**: Generates linear vector fields: `F(x,y) = (ax+by+c, dx+ey+f)`
- **`arbRotationVectorField()`**: Generates rotation-like fields for composition testing
- **`arbVectorTransform()`**: Generates simple vector-to-vector transformations

### Test Categories

#### 1. **Basic Operations** (4 tests)
- Constant field evaluation
- Zero field evaluation
- Custom field evaluation (via `of()`)
- Identity field preservation

#### 2. **Vector Addition (Abelian Group Laws)** (5 tests)
- Commutativity: `X + Y = Y + X`
- Associativity: `(X + Y) + Z = X + (Y + Z)`
- Identity: `X + 0 = X`
- Inverse: `X + (-X) = 0`
- Correctness with group operation

#### 3. **Scalar Multiplication (Field Elements)** (8 tests)
- Right multiplication: `X * c`
- Left multiplication: `c * X`
- Commutativity: `c * X = X * c`
- Identity: `1 * X = X`
- Annihilator: `0 * X = 0`
- Associativity: `(c * d) * X = c * (d * X)`
- Distribution over vector addition: `c * (X + Y) = c*X + c*Y`
- Distribution over field addition: `(c + d) * X = c*X + d*X`

#### 4. **Scalar Field Multiplication (Module Action)** (8 tests)
- Basic multiplication: `(f * X)(p) = f(p) · X(p)`
- Constant field equivalence
- Module action via `actOn`
- Zero scalar field annihilates
- One scalar field is identity
- Distribution over vector addition: `f * (X + Y) = f*X + f*Y`
- Distribution over scalar field addition: `(f + g) * X = f*X + g*X`
- Associativity: `(f * g) * X = f * (g * X)`

#### 5. **Unary Negation** (5 tests)
- Vector inversion: `(-X)(p) = -(X(p))`
- Double negation: `-(-X) = X`
- Distribution: `-(X + Y) = (-X) + (-Y)`
- Relation to scalar multiplication: `-X = (-1) * X`
- Compatibility with scaling: `-(c * X) = (-c) * X = c * (-X)`

#### 6. **Map Transformation** (4 tests)
- Pointwise application
- Identity preservation: `X.map { it } = X`
- Composition: `X.map(f).map(g) = X.map(g ∘ f)`
- Linearity preservation

#### 7. **Vector Field Composition (then)** (5 tests)
- Basic composition: `(X then Y)(p) = Y(X(p))`
- Right identity: `X then id = X`
- Left identity: `id then X = X`
- Associativity: `(X then Y) then Z = X then (Y then Z)`
- Zero field behavior

#### 8. **Functional Composition** (3 tests)
- Function composition: `(f ∘ X)(p) = f(X(p))`
- Identity composition
- Associativity

#### 9. **Vector Field Composition (compose)** (5 tests)
- Composition: `(X compose Y)(p) = X(Y(p))`
- Relation to `then`: `X compose Y = Y then X`
- Left identity
- Right identity
- Associativity

#### 10. **Module Laws (Comprehensive)** (4 tests)
Critical tests for the module structure:
- `(f + g) actOn X = (f actOn X) + (g actOn X)` (scalar field addition distributes)
- `f actOn (X + Y) = (f actOn X) + (f actOn Y)` (distributes over vector field addition)
- `(f * g) actOn X = f actOn (g actOn X)` (scalar field multiplication associates)
- `1 actOn X = X` (multiplicative identity acts trivially)

#### 11. **Combined Operations** (4 tests)
- Complex expressions: `(f * X) + (g * Y)`
- Scaling and addition: `c * (X + Y) - d * Z`
- Composition with scaling: `(c * X) then Y`
- Scalar field multiplication with composition

#### 12. **Edge Cases** (4 tests)
- Constant vector field operations
- Zero field operations
- Additive inverse cancellation
- Composition with zero field

#### 13. **Rotation Vector Fields** (2 tests)
- Rotation field evaluation
- Composition of rotations

## Total Coverage
- **70 property-based tests**
- **100 iterations per test** (Kotest default)
- **~7,000 test cases** generated automatically

## Key Mathematical Properties Tested

### Module Axioms
For module `M = VectorField<F, V>` over ring `R = ScalarField<F, V>`:

1. **Distributivity (scalar)**: `(r₁ + r₂) · m = r₁ · m + r₂ · m`
2. **Distributivity (module)**: `r · (m₁ + m₂) = r · m₁ + r · m₂`
3. **Associativity**: `(r₁ · r₂) · m = r₁ · (r₂ · m)`
4. **Identity**: `1_R · m = m`

### Abelian Group Axioms
For the additive structure:

1. **Associativity**: `(X + Y) + Z = X + (Y + Z)`
2. **Commutativity**: `X + Y = Y + X`
3. **Identity**: `∃0 : X + 0 = X`
4. **Inverse**: `∀X, ∃(-X) : X + (-X) = 0`

### Vector Space Axioms
For scalar multiplication by field elements `F`:

1. **Compatibility**: `a(bX) = (ab)X`
2. **Identity**: `1X = X`
3. **Distributivity (scalar)**: `(a + b)X = aX + bX`
4. **Distributivity (vector)**: `a(X + Y) = aX + aY`

## Usage

```kotlin
// Add to your build.gradle.kts
dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
}

// Run tests
./gradlew test --tests VectorFieldPropertyTest
```

## Extending Tests

### Adding Custom Vector Spaces
```kotlin
data class Vec3D(val x: Double, val y: Double, val z: Double) : VectorSpace<Double, Vec3D> {
    override val ring: Field<Double> get() = DoubleField
}

object Vec3DSpace : VectorSpace<Double, Vec3D> {
    override val ring = DoubleField
    override val group = AbelianGroup.of(/* ... */)
    override val action = Action { scalar, vec -> /* ... */ }
}

fun arbVec3D(): Arb<Vec3D> = arbitrary {
    Vec3D(
        x = arbFieldDouble().bind(),
        y = arbFieldDouble().bind(),
        z = arbFieldDouble().bind()
    )
}

fun arbVectorField3D(): Arb<VectorField<Double, Vec3D>> = arbitrary {
    // Generate 3D vector field
    VectorFields.of(Vec3DSpace) { v -> /* ... */ }
}
```

### Testing Custom Module Operations
```kotlin
test("custom module operation") {
    checkAll(arbScalarField(), arbVectorField()) { f, X ->
        // Test custom property
        val result = customOperation(f, X)
        // Assertions...
    }
}
```

## Implementation Files

1. **`VectorFieldTest.kt`**: Main test suite
2. **`Vec2DImplementation.kt`**: Vec2D and Vec2DSpace implementations
3. **`AlgebraFactories.kt`**: Factory methods for algebraic structures
4. **`ArbDoubleAlgebra.kt`**: Kotest arbitraries for testing
5. **`TestUtils.kt`**: Comparison utilities with tolerance

## Notes

- Uses relative tolerance (`1e-9`) and absolute tolerance (`1e-12`) for floating-point comparisons
- Linear vector fields used for deterministic, predictable behavior
- Rotation fields test composition properties
- Module laws are the core focus, ensuring proper interaction between scalar fields and vector fields
- All tests verify algebraic laws, not just implementation details

## Common Pitfalls Avoided

1. **Floating-point precision**: Uses `shouldBeApproximately` with appropriate tolerances
2. **Division by zero**: Non-zero arbitraries filter out values near zero
3. **Numerical instability**: Linear fields avoid complex operations that compound errors
4. **Type confusion**: Clear separation between field elements `F` and scalar fields `ScalarField<F, V>`

## Future Extensions

- Lie bracket tests: `[X, Y] = X ∘ Y - Y ∘ X`
- Flow/integral curve tests
- Divergence and curl for 2D/3D fields
- Covariant derivative tests
- Tests on manifolds (not just vector spaces)
- 
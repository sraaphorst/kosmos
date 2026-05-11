# Using `ArbBijection.kt`

## Basic usage:
```kotlin
val permArb = generateArbEndoBijection(Arb.int(), 3, 8)
val bijArb = Arb.int().toBijectionArb(Arb.string(), 2, 6)
```

## For property testing:
```kotlin
checkAll(permArb) { perm ->
    perm.domain.all { a ->
        perm.backward.apply(perm.forward.apply(a)) == a
    }
}
```

## Testing composition:
```kotlin
checkAll(BijectionTestingCombinations.arbEndoBijectionPair(Arb.int(), 5)) { (f, g) ->
    val composed = f then g
    // Test properties of composed bijections
}
```

## Testing orbits:
```kotlin
checkAll(BijectionTestingCombinations.arbBijectionWithElement(Arb.int())) { (bij, elem) ->
    val orbit = bij.orbit(elem)
    orbit.all { it in bij.domain }
}
```

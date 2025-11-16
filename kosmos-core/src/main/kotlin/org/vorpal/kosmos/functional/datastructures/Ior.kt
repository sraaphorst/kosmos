package org.vorpal.kosmos.functional.datastructures

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.functional.optics.Prism

sealed class Ior<out A, out B> {
    data class Left<out A>(val value: A) : Ior<A, Nothing>()
    data class Right<out B>(val value: B) : Ior<Nothing, B>()
    data class Both<out A, out B>(val first: A, val second: B) : Ior<A, B>()

    companion object {
        fun <A> left(a: A): Ior<A, Nothing> = Left(a)
        fun <B> right(b: B): Ior<Nothing, B> = Right(b)
        fun <A, B> both(a: A, b: B): Ior<A, B> = Both(a, b)

        fun <A, B> fromOptions(left: Option<A>, right: Option<B>): Option<Ior<A, B>> = when {
            left is Option.Some && right is Option.Some ->
                Option.Some(Both(left.value, right.value))
            left is Option.Some ->
                Option.Some(Left(left.value))
            right is Option.Some ->
                Option.Some(Right(right.value))
            else -> Option.None
        }

        fun <A, B> fromEither(either: Either<A, B>): Ior<A, B> = when (either) {
            is Either.Left -> Left(either.value)
            is Either.Right -> Right(either.value)
        }

        fun <A, B> fromNullablesOption(a: A?, b: B?): Option<Ior<A, B>> = when {
            a != null && b != null -> Option.Some(Both(a, b))
            a != null -> Option.Some(Left(a))
            b != null -> Option.Some(Right(b))
            else -> Option.None
        }

        fun <A, B> fromNullables(a: A?, b: B?): Ior<A, B>? = when {
            a != null && b != null -> Both(a, b)
            a != null -> Left(a)
            b != null -> Right(b)
            else -> null
        }
    }
}

fun Ior<*, *>.isLeft(): Boolean = this is Ior.Left
fun Ior<*, *>.isRight(): Boolean = this is Ior.Right
fun Ior<*, *>.isBoth(): Boolean = this is Ior.Both
fun Ior<*, *>.hasLeft(): Boolean = isLeft() || isBoth()
fun Ior<*, *>.hasRight(): Boolean = isRight() || isBoth()

fun <A, B, C> Ior<A, B>.fold(
    ifLeft: (A) -> C,
    ifRight: (B) -> C,
    ifBoth: (A, B) -> C
): C = when (this) {
    is Ior.Left<A> -> ifLeft(value)
    is Ior.Right<B> -> ifRight(value)
    is Ior.Both<A, B> -> ifBoth(first, second)
}

fun <A, C> Ior<A, *>.foldLeft(ifLeft: (A) -> C): Option<C> = when (this) {
    is Ior.Left -> Option.Some(ifLeft(value))
    is Ior.Right -> Option.None
    is Ior.Both -> Option.None
}

fun <B, C> Ior<*, B>.foldRight(ifRight: (B) -> C): Option<C> = when (this) {
    is Ior.Left -> Option.None
    is Ior.Right -> Option.Some(ifRight(value))
    is Ior.Both -> Option.None
}

fun <A, B, C> Ior<A, B>.foldBoth(ifBoth: (A, B) -> C): Option<C> = when (this) {
    is Ior.Left -> Option.None
    is Ior.Right -> Option.None
    is Ior.Both -> Option.Some(ifBoth(first, second))
}

fun <A1, A2, B> Ior<A1, B>.mapLeft(f: (A1) -> A2): Ior<A2, B> = when (this) {
    is Ior.Left -> Ior.Left(f(value))
    is Ior.Right -> this
    is Ior.Both -> Ior.Both(f(first), second)
}

fun <A, B1, B2> Ior<A, B1>.mapRight(f: (B1) -> B2): Ior<A, B2> = when (this) {
    is Ior.Left -> this
    is Ior.Right -> Ior.Right(f(value))
    is Ior.Both -> Ior.Both(first, f(second))
}

fun <A1, A2, B1, B2> Ior<A1, B1>.bimap(
    f: (A1) -> A2,
    g: (B1) -> B2
): Ior<A2, B2> = when (this) {
    is Ior.Left -> Ior.Left(f(value))
    is Ior.Right -> Ior.Right(g(value))
    is Ior.Both -> Ior.Both(f(first), g(second))
}

context(semigroup: Semigroup<B>)
fun <A1, A2, B: Any> Ior<A1, B>.flatMapLeft(f: (A1) -> Ior<A2, B>): Ior<A2, B> =
    flatMapLeft(semigroup.op.combine, f)

fun <A1, A2, B: Any> Ior<A1, B>.flatMapLeft(
    semigroup: Semigroup<B>,
    f: (A1) -> Ior<A2, B>
): Ior<A2, B> = flatMapLeft(semigroup.op.combine, f)

fun <A1, A2, B: Any> Ior<A1, B>.flatMapLeft(
    combine: (B, B) -> B,
    f: (A1) -> Ior<A2, B>
): Ior<A2, B> = when (this) {
    is Ior.Left -> f(value)
    is Ior.Right -> this
    is Ior.Both -> when (val result = f(first)) {
        is Ior.Left -> Ior.Both(result.value, second)
        is Ior.Right -> Ior.Right(combine(second, result.value))
        is Ior.Both -> Ior.Both(result.first, combine(second, result.second))
    }
}

context(semigroup: Semigroup<A>)
fun <A: Any, B, C> Ior<A, B>.flatMap(f: (B) -> Ior<A, C>): Ior<A, C> =
    flatMapRight(semigroup.op.combine, f)

fun <A: Any, B1, B2> Ior<A, B1>.flatMapRight(
    semigroup: Semigroup<A>,
    g: (B1) -> Ior<A, B2>
): Ior<A, B2> = flatMapRight(semigroup.op.combine, g)

fun <A: Any, B1, B2> Ior<A, B1>.flatMapRight(
    combine: (A, A) -> A,
    g: (B1) -> Ior<A, B2>
): Ior<A, B2> = when (this) {
    is Ior.Left -> this
    is Ior.Right -> g(value)
    is Ior.Both -> when (val result = g(second)) {
        is Ior.Left -> Ior.Left(combine(first, result.value))
        is Ior.Right -> Ior.Both(first, result.value)
        is Ior.Both -> Ior.Both(combine(first, result.first), result.second)
    }
}

fun <A> Ior<A, *>.leftOrElse(default: () -> A): Ior<A, Nothing> = when (this) {
    is Ior.Left -> this
    else -> Ior.Left(default())
}

fun <A, B> Ior<A, B>.leftJoinOrElse(default: () -> A): Ior<A, B> = when (this) {
    is Ior.Right -> Ior.Both(default(), value)
    else -> this
}

fun <B> Ior<*, B>.rightOrElse(default: () -> B): Ior<Nothing, B> = when (this) {
    is Ior.Right -> this
    else -> Ior.Right(default())
}

fun <A, B> Ior<A, B>.rightJoinOrElse(default: () -> B): Ior<A, B> = when (this) {
    is Ior.Left -> Ior.Both(value, default())
    else -> this
}

fun <A> Ior<A, *>.getLeftOrElse(default: () -> A): A = when (this) {
    is Ior.Left -> value
    is Ior.Right -> default()
    is Ior.Both -> first
}

fun <A> Ior<A, *>.getLeftOrNull(): A? = when (this) {
    is Ior.Left -> value
    is Ior.Right -> null
    is Ior.Both -> first
}

fun <B> Ior<*, B>.getRightOrElse(default: () -> B): B = when (this) {
    is Ior.Left -> default()
    is Ior.Right -> value
    is Ior.Both -> second
}

fun <B> Ior<*, B>.getRightOrNull(): B? = when (this) {
    is Ior.Left -> null
    is Ior.Right -> value
    is Ior.Both -> second
}

fun <A> Ior<A, *>.toLeftOption(): Option<A> = when (this) {
    is Ior.Left -> Option.Some(value)
    is Ior.Right -> Option.None
    is Ior.Both -> Option.Some(first)
}

fun <B> Ior<*, B>.toRightOption(): Option<B> = when (this) {
    is Ior.Left -> Option.None
    is Ior.Right -> Option.Some(value)
    is Ior.Both -> Option.Some(second)
}

fun <A, B> Ior<A, B>.toEither(): Either<A, B> = when (this) {
    is Ior.Left -> Either.Left(value)
    is Ior.Right -> Either.Right(value)
    is Ior.Both -> Either.Right(second)
}

fun <A, B> Ior<A, B>.toPairOrElse(
    defaultLeft: () -> A,
    defaultRight: () -> B
): Pair<A, B> = when (this) {
    is Ior.Left -> Pair(value, defaultRight())
    is Ior.Right -> Pair(defaultLeft(), value)
    is Ior.Both -> Pair(first, second)
}

fun <A, B> Ior<A, B>.toPairOrNull(): Pair<A, B>? = when (this) {
    is Ior.Both -> Pair(first, second)
    else -> null
}

fun <A, B> Ior<A, B>.toPairOption(): Option<Pair<A, B>> = when (this) {
    is Ior.Both -> Option.Some(Pair(first, second))
    else -> Option.None
}

fun <A, B> Ior<A, B>.toOptionPair(): Pair<Option<A>, Option<B>> = when (this) {
    is Ior.Left -> Pair(Option.Some(value), Option.None)
    is Ior.Right -> Pair(Option.None, Option.Some(value))
    is Ior.Both -> Pair(Option.Some(first), Option.Some(second))
}

fun <A, B> Ior<A, B>.swap(): Ior<B, A> = when (this) {
    is Ior.Left -> Ior.Right(value)
    is Ior.Right -> Ior.Left(value)
    is Ior.Both -> Ior.Both(second, first)
}

fun <A, B> Ior<A, B>.padLeft(f: () -> A): Pair<A, B?> = when (this) {
    is Ior.Left -> Pair(value, null)
    is Ior.Right -> Pair(f(), value)
    is Ior.Both -> Pair(first, second)
}

fun <A, B> Ior<A, B>.padRight(g: () -> B): Pair<A?, B> = when (this) {
    is Ior.Left -> Pair(value, g())
    is Ior.Right -> Pair(null, value)
    is Ior.Both -> Pair(first, second)
}

fun <A, B> Ior<A, B>.pad(): Pair<A?, B?> = when (this) {
    is Ior.Left -> Pair(value, null)
    is Ior.Right -> Pair(null, value)
    is Ior.Both -> Pair(first, second)
}

fun <A: Any, B: Any> Ior<A, B>.mergeWith(
    combineA: (A, A) -> A,
    combineB: (B, B) -> B,
    other: Ior<A, B>
): Ior<A, B> = when (this) {
    is Ior.Left -> when (other) {
        is Ior.Left -> Ior.Left(combineA(value, other.value))
        is Ior.Right -> Ior.Both(value, other.value)
        is Ior.Both -> Ior.Both(combineA(value, other.first), other.second)
    }
    is Ior.Right -> when (other) {
        is Ior.Left -> Ior.Both(other.value, value)
        is Ior.Right -> Ior.Right(combineB(value, other.value))
        is Ior.Both -> Ior.Both(other.first, combineB(value, other.second))
    }
    is Ior.Both -> when (other) {
        is Ior.Left -> Ior.Both(combineA(first, other.value), second)
        is Ior.Right -> Ior.Both(first, combineB(second, other.value))
        is Ior.Both -> Ior.Both(
            combineA(first, other.first),
            combineB(second, other.second)
        )
    }
}

fun <A: Any, B: Any> Ior<A, B>.mergeWith(
    semigroupA: Semigroup<A>,
    semigroupB: Semigroup<B>,
    other: Ior<A, B>
): Ior<A, B> = mergeWith(semigroupA.op.combine, semigroupB.op.combine, other)

fun <A, B> Ior<A, B>.tapLeft(f: (A) -> Unit): Ior<A, B> = apply {
    getLeftOrNull()?.let(f)
}

fun <A, B> Ior<A, B>.tapRight(g: (B) -> Unit): Ior<A, B> = apply {
    getRightOrNull()?.let(g)
}

fun <A, B> Ior<A, B>.tapBoth(f: (A, B) -> Unit): Ior<A, B> = apply {
    toPairOrNull()?.let { f(it.first, it.second) }
}

fun <A, B> Ior<A, B>.tapBoth(f: (A) -> Unit, g: (B) -> Unit): Ior<A, B> = apply {
    toPairOrNull()?.let { (a, b) ->
        f(a)
        g(b)
    }
}

object Iors {
    fun <A: Any, B: Any> sequence(
        iors: Collection<Ior<A, B>>
    ): Ior<List<A>, List<B>> {
        tailrec fun aux(iter: Iterator<Ior<A, B>> = iors.iterator(),
                        leftList: MutableList<A> = mutableListOf(),
                        rightList: MutableList<B> = mutableListOf()): Ior<List<A>, List<B>> {
            // We call toList on the lists to make them immutable.
            if (!iter.hasNext()) return when {
                leftList.isEmpty() -> Ior.Right(rightList.toList())
                rightList.isEmpty() -> Ior.Left(leftList.toList())
                else -> Ior.Both(leftList.toList(), rightList.toList())
            }
            return when (val head = iter.next()) {
                is Ior.Left -> aux(iter, leftList.apply { add(head.value) }, rightList)
                is Ior.Right -> aux(iter, leftList, rightList.apply { add(head.value) })
                is Ior.Both -> aux(iter, leftList.apply { add(head.first) }, rightList.apply { add(head.second) })
            }
        }
        return aux()
    }

    fun <A, B: Any, C: Any> traverse(
        semigroup: Semigroup<B>,
        xs: Collection<A>,
        f: (A) -> Ior<B, C>
    ): Ior<B, List<C>> {
        tailrec fun aux(iter: Iterator<A> = xs.iterator(),
                        b: B? = null,
                        cs: MutableList<C> = mutableListOf()): Ior<B, List<C>> {
            // We call toList on the lists to make them immutable.
            if (!iter.hasNext()) return when {
                b == null -> Ior.Right(cs.toList())
                cs.isEmpty() -> Ior.Left(b)
                else -> Ior.Both(b, cs.toList())
            }
            return when (val head = f(iter.next())) {
                is Ior.Left -> {
                    val newB = b?.let { semigroup.op.combine(b, head.value)} ?: head.value
                    aux(iter, newB, cs)
                }
                is Ior.Right -> aux(iter, b, cs.apply { add(head.value) })
                is Ior.Both -> {
                    val newB = b?.let { semigroup.op.combine(b, head.first)} ?: head.first
                    aux(iter, newB, cs.apply { add(head.second) })
                }
            }
        }
        return aux()
    }
}

object IorOptics {
    fun <A> left(): Prism<Ior<A, *>, A> = Prism(
        getterOrNull = { it.getLeftOrNull() },
        reverseGetter = { Ior.Left(it) },
        identityT = Identity()
    )

    fun <B> right(): Prism<Ior<*, B>, B> = Prism(
        getterOrNull = { it.getRightOrNull() },
        reverseGetter = { Ior.Right(it) },
        identityT = Identity()
    )

    fun <A, B> both(): Prism<Ior<A, B>, Pair<A, B>> = Prism(
        getterOrNull = { it.toPairOrNull() },
        reverseGetter = { Ior.Both(it.first, it.second) },
        identityT = Identity()
    )
}

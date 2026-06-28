package org.vorpal.kosmos.linear.ops

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.linear.instance.arbRationalMat
import org.vorpal.kosmos.linear.instance.arbSquareRationalMat
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.ReducedRowEchelonForm
import org.vorpal.kosmos.linear.values.RowEchelonForm
import org.vorpal.kosmos.linear.values.RowOp
import org.vorpal.kosmos.linear.values.arePivotsNormalized
import org.vorpal.kosmos.linear.values.areZeroRowsAtBottom
import org.vorpal.kosmos.linear.values.isAbovePivotsZero
import org.vorpal.kosmos.linear.values.isBelowPivotsZero
import org.vorpal.kosmos.linear.values.isPivotLeading
import org.vorpal.kosmos.linear.values.isStaircase
import org.vorpal.kosmos.linear.values.replay

/**
 * Property tests for the row-reduction operations on [DenseMatOps]:
 *
 *  - [DenseMatOps.rowEchelonForm];
 *  - [DenseMatOps.rowEchelonFormTrace];
 *  - [DenseMatOps.reducedRowEchelonForm];
 *  - [DenseMatOps.reducedRowEchelonFormTrace].
 *
 * Reductions are checked over the rationals ℚ for exact arithmetic. Tests are a mix of
 * hand-computed concrete cases and property-based checks over random rational matrices.
 *
 * The trace variants are validated by **replaying** the recorded row operations on the
 * original matrix and checking that the result matches the returned form — this is the
 * key contract for the trace API. Replay machinery and structural predicates live in
 * `kosmos-testkit/.../linear/values/`.
 */
class DenseMatRowReductionSpec : FunSpec({

    val q = RationalAlgebras.RationalField
    val zero = Rational.ZERO
    val one = Rational.ONE

    /** Build a rational matrix from rows of `Int`s. */
    fun qm(vararg rows: List<Int>): DenseMat<Rational> =
        DenseMat.ofRows(rows.map { row -> row.map { it.toRational() } })

    // ── rowEchelonForm: concrete examples ─────────────────────────────────

    context("rowEchelonForm — concrete examples over ℚ") {

        test("identity is its own REF") {
            val id3 = DenseMatOps.identity(q, 3)
            val ref = DenseMatOps.rowEchelonForm(q, id3)
            ref.matrix shouldBe id3
            ref.rank shouldBe 3
            ref.pivots shouldBe listOf(0 to 0, 1 to 1, 2 to 2)
        }

        test("zero matrix has rank 0 with no pivots") {
            val z = DenseMatOps.zero(q, 3, 4)
            val ref = DenseMatOps.rowEchelonForm(q, z)
            ref.matrix shouldBe z
            ref.rank shouldBe 0
            ref.pivots shouldBe emptyList()
        }

        test("2×2 invertible matrix is full rank with pivots on the diagonal") {
            val m = qm(listOf(2, 4), listOf(3, 9))
            val ref = DenseMatOps.rowEchelonForm(q, m)
            ref.rank shouldBe 2
            ref.pivots shouldBe listOf(0 to 0, 1 to 1)
            arePivotsNormalized(ref.matrix, ref.pivots, one) shouldBe true
            isBelowPivotsZero(ref.matrix, ref.pivots, zero) shouldBe true
        }

        test("3×3 with a duplicated row has rank 2") {
            val m = qm(
                listOf(1, 2, 3),
                listOf(2, 4, 6), // 2 × first row
                listOf(1, 1, 1)
            )
            val ref = DenseMatOps.rowEchelonForm(q, m)
            ref.rank shouldBe 2
            (0 until 3).all { c -> ref.matrix[2, c] == zero } shouldBe true
        }

        test("rectangular 2×4 in REF preserves leading-1 staircase") {
            val m = qm(
                listOf(1, 0, 2, 1),
                listOf(0, 1, 1, 3)
            )
            val ref = DenseMatOps.rowEchelonForm(q, m)
            ref.rank shouldBe 2
            ref.pivots shouldBe listOf(0 to 0, 1 to 1)
            arePivotsNormalized(ref.matrix, ref.pivots, one) shouldBe true
        }

        test("matrix with a non-pivot leading column") {
            val m = qm(
                listOf(0, 1, 2),
                listOf(0, 2, 4),
                listOf(0, 0, 1)
            )
            val ref = DenseMatOps.rowEchelonForm(q, m)
            ref.rank shouldBe 2
            ref.pivots.first().second shouldBe 1
        }
    }

    // ── rowEchelonForm: structural properties ─────────────────────────────

    context("rowEchelonForm — structural properties") {

        test("pivot entries are all one") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                arePivotsNormalized(ref.matrix, ref.pivots, one) shouldBe true
            }
        }

        test("each entry below a pivot's column is zero") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                isBelowPivotsZero(ref.matrix, ref.pivots, zero) shouldBe true
            }
        }

        test("each entry to the left of a pivot in its row is zero") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                isPivotLeading(ref.matrix, ref.pivots, zero) shouldBe true
            }
        }

        test("pivots form a strictly increasing staircase") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                isStaircase(ref.pivots) shouldBe true
            }
        }

        test("zero rows occur only after the last pivot row") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                areZeroRowsAtBottom(ref.matrix, ref.pivots, zero) shouldBe true
            }
        }

        test("rank ≤ min(rows, cols)") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                (ref.rank <= minOf(mat.rows, mat.cols)) shouldBe true
            }
        }

        test("REF preserves the matrix shape") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                ref.matrix.rows shouldBe mat.rows
                ref.matrix.cols shouldBe mat.cols
            }
        }
    }

    // ── reducedRowEchelonForm: concrete examples ──────────────────────────

    context("reducedRowEchelonForm — concrete examples over ℚ") {

        test("identity is its own RREF") {
            val id3 = DenseMatOps.identity(q, 3)
            val rref = DenseMatOps.reducedRowEchelonForm(q, id3)
            rref.matrix shouldBe id3
            rref.rank shouldBe 3
        }

        test("zero matrix has rank 0") {
            val z = DenseMatOps.zero(q, 2, 3)
            val rref = DenseMatOps.reducedRowEchelonForm(q, z)
            rref.matrix shouldBe z
            rref.rank shouldBe 0
            rref.pivots shouldBe emptyList()
        }

        test("2×2 invertible reduces to the identity") {
            val m = qm(listOf(2, 4), listOf(3, 9))
            val rref = DenseMatOps.reducedRowEchelonForm(q, m)
            rref.matrix shouldBe DenseMatOps.identity(q, 2)
            rref.rank shouldBe 2
        }

        test("3×4 with a dependent row reduces to canonical form") {
            val m = qm(
                listOf(1, 2, 0, 3),
                listOf(0, 0, 1, 4),
                listOf(2, 4, 1, 10) // = 2·row0 + row1
            )
            val rref = DenseMatOps.reducedRowEchelonForm(q, m)
            rref.rank shouldBe 2
            rref.pivots shouldBe listOf(0 to 0, 1 to 2)
            isAbovePivotsZero(rref.matrix, rref.pivots, zero) shouldBe true
        }
    }

    // ── reducedRowEchelonForm: structural properties ──────────────────────

    context("reducedRowEchelonForm — structural properties") {

        test("pivot entries are all one") {
            checkAll(arbRationalMat()) { mat ->
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                arePivotsNormalized(rref.matrix, rref.pivots, one) shouldBe true
            }
        }

        test("each entry below a pivot's column is zero") {
            checkAll(arbRationalMat()) { mat ->
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                isBelowPivotsZero(rref.matrix, rref.pivots, zero) shouldBe true
            }
        }

        test("each entry above a pivot's column is also zero (the RREF distinction)") {
            checkAll(arbRationalMat()) { mat ->
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                isAbovePivotsZero(rref.matrix, rref.pivots, zero) shouldBe true
            }
        }

        test("pivots form a strictly increasing staircase") {
            checkAll(arbRationalMat()) { mat ->
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                isStaircase(rref.pivots) shouldBe true
            }
        }

        test("zero rows occur only after the last pivot row") {
            checkAll(arbRationalMat()) { mat ->
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                areZeroRowsAtBottom(rref.matrix, rref.pivots, zero) shouldBe true
            }
        }

        test("for square matrices of full rank, RREF is the identity") {
            checkAll(arbSquareRationalMat()) { mat ->
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                if (rref.rank == mat.rows) {
                    rref.matrix shouldBe DenseMatOps.identity(q, mat.rows)
                }
            }
        }
    }

    // ── Trace replay: the key contract ────────────────────────────────────

    context("rowEchelonFormTrace — replay invariant") {

        test("the recorded form equals what plain rowEchelonForm returns") {
            checkAll(arbRationalMat()) { mat ->
                val direct: RowEchelonForm<Rational> = DenseMatOps.rowEchelonForm(q, mat)
                val traced = DenseMatOps.rowEchelonFormTrace(q, mat)
                traced.form.matrix shouldBe direct.matrix
                traced.form.pivots shouldBe direct.pivots
                traced.form.rank shouldBe direct.rank
                traced.form.rowSwaps shouldBe direct.rowSwaps
            }
        }

        test("replaying the operations on the original matrix yields the form") {
            checkAll(arbRationalMat()) { mat ->
                val traced = DenseMatOps.rowEchelonFormTrace(q, mat)
                val replayed = replay(q, mat, traced.operations)
                replayed shouldBe traced.form.matrix
            }
        }

        test("rowSwaps counts exactly the Swap operations in the trace") {
            checkAll(arbRationalMat()) { mat ->
                val traced = DenseMatOps.rowEchelonFormTrace(q, mat)
                val swapCount = traced.operations.count { it is RowOp.Swap }
                traced.form.rowSwaps shouldBe swapCount
            }
        }
    }

    context("reducedRowEchelonFormTrace — replay invariant") {

        test("the recorded form equals what plain reducedRowEchelonForm returns") {
            checkAll(arbRationalMat()) { mat ->
                val direct: ReducedRowEchelonForm<Rational> =
                    DenseMatOps.reducedRowEchelonForm(q, mat)
                val traced = DenseMatOps.reducedRowEchelonFormTrace(q, mat)
                traced.form.matrix shouldBe direct.matrix
                traced.form.pivots shouldBe direct.pivots
                traced.form.rank shouldBe direct.rank
                traced.form.rowSwaps shouldBe direct.rowSwaps
            }
        }

        test("replaying the operations on the original matrix yields the form") {
            checkAll(arbRationalMat()) { mat ->
                val traced = DenseMatOps.reducedRowEchelonFormTrace(q, mat)
                val replayed = replay(q, mat, traced.operations)
                replayed shouldBe traced.form.matrix
            }
        }

        test("rowSwaps counts exactly the Swap operations in the trace") {
            checkAll(arbRationalMat()) { mat ->
                val traced = DenseMatOps.reducedRowEchelonFormTrace(q, mat)
                val swapCount = traced.operations.count { it is RowOp.Swap }
                traced.form.rowSwaps shouldBe swapCount
            }
        }
    }

    // ── REF / RREF consistency ────────────────────────────────────────────

    context("REF and RREF agree on rank and pivot columns") {

        test("ranks are equal") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                rref.rank shouldBe ref.rank
            }
        }

        test("pivot columns are the same set") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                val rref = DenseMatOps.reducedRowEchelonForm(q, mat)
                rref.pivots.map { it.second } shouldBe ref.pivots.map { it.second }
            }
        }

        test("rank matches DenseMatOps.rank") {
            checkAll(arbRationalMat()) { mat ->
                val ref = DenseMatOps.rowEchelonForm(q, mat)
                DenseMatOps.rank(q, mat) shouldBe ref.rank
            }
        }
    }

    // ── Edge cases ────────────────────────────────────────────────────────

    context("edge cases") {

        test("1×1 zero matrix") {
            val m = qm(listOf(0))
            val ref = DenseMatOps.rowEchelonForm(q, m)
            ref.rank shouldBe 0
            ref.matrix shouldBe m
        }

        test("1×1 non-zero matrix normalizes to one") {
            val m = qm(listOf(7))
            val ref = DenseMatOps.rowEchelonForm(q, m)
            ref.rank shouldBe 1
            ref.matrix shouldBe qm(listOf(1))
        }

        test("REF of a tall, single-column non-zero matrix") {
            val m = qm(listOf(2), listOf(4), listOf(6))
            val ref = DenseMatOps.rowEchelonForm(q, m)
            ref.rank shouldBe 1
            ref.pivots shouldBe listOf(0 to 0)
            ref.matrix shouldBe qm(listOf(1), listOf(0), listOf(0))
        }

        test("RREF of a wide, single-row non-zero matrix") {
            val m = qm(listOf(2, 4, 6))
            val rref = DenseMatOps.reducedRowEchelonForm(q, m)
            rref.rank shouldBe 1
            rref.pivots shouldBe listOf(0 to 0)
            rref.matrix shouldBe qm(listOf(1, 2, 3))
        }

        test("trace replay is empty-ops-safe for the identity matrix") {
            val id3 = DenseMatOps.identity(q, 3)
            val traced = DenseMatOps.reducedRowEchelonFormTrace(q, id3)
            replay(q, id3, traced.operations) shouldBe id3
        }
    }
})

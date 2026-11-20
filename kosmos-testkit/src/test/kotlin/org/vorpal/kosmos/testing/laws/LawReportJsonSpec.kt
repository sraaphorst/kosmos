package org.vorpal.kosmos.testing.laws

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawRunConfig
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.verify

class LawReportJsonSpec : FunSpec({

    val subtraction = BinOp<Int>("-") { a, b -> a - b }
    val shared = LawNode.branch("Magma (+)", TestingLaw.named("totality (+)") { }.asNode())
    val root = lawGraph("Root") {
        node(LawNode.branch("Left", shared))
        node(LawNode.branch("Right", shared))
        law(AssociativityLaw(subtraction, Arb.int(-9..9), Eq { a, b -> a == b }, Printable { "⟨$it⟩" }))
        law(TestingLaw.named("broken") { lawCheck(true); error("boom") })
        skipped(TestingLaw.named("later") { }, "not yet")
    }
    val report = kotlinx.coroutines.runBlocking { root.verify(LawRunConfig(seed = 5)) }
    val dto = LawReportJson.toDto(report)

    test("the header names the schema and version and summarises the run") {
        dto.schema shouldBe "kosmos-law-report"
        dto.version shouldBe 1
        dto.root shouldBe "n0"
        dto.status shouldBe "ERRORED"
        dto.seed shouldBe 5
        dto.counts shouldBe CountsDto(passed = 2, failed = 1, errored = 1, skipped = 1)
        dto.executions shouldBe 3
    }

    test("nodes are listed depth-first and children refer to them by id") {
        val byId = dto.nodes.associateBy { it.id }
        dto.nodes.map { it.label } shouldContainExactly listOf(
            "Root", "Left", "Magma (+)", "totality (+)", "Right", "Magma (+)", "totality (+)",
            "associativity (-)", "broken", "later"
        )
        byId.getValue("n0").children shouldContainExactly listOf("n1", "n4", "n7", "n8", "n9")
        dto.nodes.forEach { n -> n.children.forEach { c -> byId.getValue(c).indices.dropLast(1) shouldBe n.indices } }
    }

    test("a shared repeat points at the occurrence that executed") {
        val repeat = dto.nodes.single { it.id == "n5" }
        repeat.sharedFrom shouldBe "n2"
        dto.nodes.single { it.id == "n6" }.sharedFrom shouldBe "n3"
        dto.nodes.single { it.id == "n2" }.sharedFrom.shouldBeNull()
    }

    test("a property failure exports its exception, cause and replay metadata") {
        val leaf = dto.nodes.single { it.label == "associativity (-)" }
        val o = leaf.outcome.shouldNotBeNull()
        o.status shouldBe "FAILED"
        o.exception.shouldNotBeNull().message.shouldNotBeNull()   // Kotest's wrapper (an AssertionError subtype)
        o.cause.shouldNotBeNull().type shouldBe "org.vorpal.kosmos.laws.LawViolation"
        o.seed.shouldNotBeNull()
        o.counterexample.shouldNotBeNull() shouldStartWith "a = ⟨"
        leaf.indices shouldBe listOf(2)
    }

    test("errors and skips export what is available") {
        dto.nodes.single { it.label == "broken" }.outcome!!.let {
            it.status shouldBe "ERRORED"
            it.cause.shouldNotBeNull().message shouldBe "boom"
            it.seed.shouldBeNull()
            it.unavailable.size shouldBe 2
        }
        dto.nodes.single { it.label == "later" }.outcome!!.skipReason shouldBe "not yet"
    }

    test("the JSON text decodes back to the same DTOs") {
        val text = report.toJson()
        val tree = Json.parseToJsonElement(text).jsonObject
        tree.getValue("version").jsonPrimitive.int shouldBe 1
        Json.decodeFromString(ReportDto.serializer(), text) shouldBe dto
    }
})

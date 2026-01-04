package org.vorpal.kosmos.rec.cjk

import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.std.Rational
import java.math.BigDecimal
import java.math.BigInteger


enum class Script(val englishName: String, val nativeName: String) {
    ZH_HANT("Chinese (Traditional)", "中文繁體"),
    ZH_HANS("Chinese (Simplified)", "中文简体"),
    JP("Japanese", "日本語");


    override fun toString(): String =
        "$englishName / $nativeName"
}

private val ZHScripts: Set<Script> = setOf(Script.ZH_HANT, Script.ZH_HANS)
private val ZHTradScript: Set<Script> = setOf(Script.ZH_HANT)
private val ZHSimpScript: Set<Script> = setOf(Script.ZH_HANS)
private val JPScript: Set<Script> = setOf(Script.JP)
private val AllScripts: Set<Script> = Script.entries.toSet()

sealed class ScriptTyped(
    private val scripts: Set<Script>,
    private val description: String)
{
    init {
        require(scripts.isNotEmpty()) { "No script associated with setting $description" }
    }

    private val scriptRepresentation: String by lazy {
        when (scripts.size) {
            1 -> scripts.single().toString()
            else -> scripts.joinToString(separator = ", ")
        }
    }

    fun checkCompatability(selectedScript: Script) {
        require(selectedScript in scripts) {
            "Script incompatibility: selected is $selectedScript, permitted: $scriptRepresentation"
        }
    }
}

sealed class ZeroStyle(val value: String, scripts: Set<Script>):
    ScriptTyped(scripts, "ZeroStyle") {

    object Chinese: ZeroStyle("零", ZHScripts)
    object JapaneseRei: ZeroStyle("零", JPScript)
    object JapaneseCircle: ZeroStyle("〇", JPScript)
}

sealed class PositiveStyle(val prefix: String, scripts: Set<Script>):
    ScriptTyped(scripts, "PositiveStyle") {

    object Omit: PositiveStyle("", AllScripts)
    object Plus: PositiveStyle("+", AllScripts)
    object ChineseZheng: PositiveStyle("正", ZHScripts)
    object JapaneseKatakana: PositiveStyle("プラス", JPScript)
}

sealed class NegativeStyle(val prefix: String, scripts: Set<Script>):
    ScriptTyped(scripts, "NegativeStyle") {

    object Minus: NegativeStyle("-", AllScripts)
    object TraditionalChineseFu: NegativeStyle("負", ZHTradScript)
    object SimplifiedChineseFu: NegativeStyle("负", ZHSimpScript)
    object JapaneseKatakana: NegativeStyle("マイナス", JPScript)
}

sealed class DecimalPointStyle(val point: String, scripts: Set<Script>):
    ScriptTyped(scripts, "DecimalPointStyle") {

    object Point: DecimalPointStyle(".", AllScripts)
    object CJKPoint: DecimalPointStyle("．", AllScripts)
    object Comma: DecimalPointStyle(",", AllScripts)
    object TraditionalChinese: DecimalPointStyle("點", ZHTradScript)
    object SimplifiedChinese: DecimalPointStyle("点", ZHSimpScript)
}

/**
 * Specify the type of the fraction style output when converting from Arabic numerals to CJK characters.
 *
 * Note that denominator is always positive.
 * - IMPROPER: numerator, denominator
 * - MIXED: whole number, numerator, denominator with the caveat that abs(numerator) < denominator
 */
enum class FractionStyle {
    IMPROPER,
    MIXED
}

/**
 * Specify the style of digits to use:
 * - COMMON: standard digits used in writing
 * - FINANCIAL: banker's anti-fraud digits
 */
enum class DigitStyle {
    COMMON,
    FINANCIAL
}

/**
 * Applicable only to Chinese.
 *
 * [Never] means:
 * - 二 will always be used, regardless of position.
 *
 * [TraditionalChineseSpoken] and [SimplifiedChineseSpoken] mean:
 * - 二 will be used before 十 / 百.
 * - 兩 / 两 will be used before 百 / 千 / 萬 / 億 /...
 */
sealed class LiangPolicy(val value: String, scripts: Set<Script>): ScriptTyped(scripts, "LiangPolicy") {
    object Never: LiangPolicy("二", ZHScripts)
    object TraditionalChineseSpoken: LiangPolicy("兩", ZHTradScript)
    object SimplifiedChineseSpoken: LiangPolicy("两", ZHSimpScript)
}


internal typealias ScriptDict = Map<Script, String>

/**
 * Create a map from Script to representation. Note that this is flexible to simplify the case where
 * the simplified Chinese character
 */
private fun makeMap(zhTrad: String, zhSimp: String? = null, jp: String? = null): ScriptDict =
    buildMap {
        put(Script.ZH_HANT, zhTrad)
        put(Script.ZH_HANS, (zhSimp ?: zhTrad))
        if (jp != null) put(Script.JP, jp)
    }

/**
 * Note: standard is identical across all three scripts.
 */
internal enum class SmallNumbers(
    val value: Int,
    val standardChar: String,
    val financialChars: ScriptDict
) {
    LING(0, "零", makeMap("零", "零", "零")),
    YI(1, "一", makeMap("壹", "壹", "壱")),
    ER(2, "二", makeMap("貳", "贰", "弐")),
    SAN(3, "三", makeMap("參", "叁", "参")),
    SI(4, "四", makeMap("肆")),
    WU(5, "五", makeMap("伍")),
    LIU(6, "六", makeMap("陸", "陆")),
    QI(7, "七", makeMap("柒")),
    BA(8, "八", makeMap("捌")),
    JIU(9, "九", makeMap("玖")),
    SHI(10, "十", makeMap("拾")),
    BAI(100, "百", makeMap("佰")),
    QIAN(1000, "千", makeMap("仟"))
}


internal enum class Myriad(
    val power10: Int,
    val chars: ScriptDict
) {
    WAN(4, makeMap("萬", "万", "万")),
    YI(8, makeMap("億", "亿", "億")),
    ZHAO(12, makeMap("兆", "兆", "兆")),
    JING(16, makeMap("京", "京", "京")),
    GAI(20, makeMap("垓", "垓", "垓")),
    ZI(24, makeMap("秭", "秭", "秭")),
    RANG(28, makeMap("穰", "穰", "穰")),
    GOU(32, makeMap("溝", "沟", "溝")),
    JIAN(36, makeMap("澗", "涧", "澗")),
    ZHENG(40, makeMap("正", "正", "正")),
    ZAI(44, makeMap("載", "载", "載")),
    JI(48, makeMap("極", "极", "極")),
    HENG_HE_SHA(52, makeMap("恆河沙", "恒河沙", "恒河沙")),
    A_SENG_QI(56, makeMap("阿僧祇", "阿僧祇", "阿僧祇")),
    NA_YOU_TA(60, makeMap("那由他", "那由他", "那由他")),
    BU_KE_SI_YI(64, makeMap("不可思議", "不可思议", "不可思議")),
    WU_LIANG_DA_SHU(68, makeMap("無量大數", "无量大数", "無量大数"))
}


/**
 * Information required for taking an arabic number and formatting it in Chinese characters.
 */
sealed interface CjkNumberFormat {
    val script: Script
    val positiveStyle: PositiveStyle
    val negativeStyle: NegativeStyle
    val digitStyle: DigitStyle
    val decimalPointStyle: DecimalPointStyle
    val fractionStyle: FractionStyle

    fun format(bigInteger: BigInteger): String
    fun format(decimal: BigDecimal): String
    fun format(rational: Rational): String
    fun formatPercent(decimal: BigDecimal): String
}


class ChineseNumberFormat(
    override val script: Script,
    override val positiveStyle: PositiveStyle = PositiveStyle.Omit,
    override val negativeStyle: NegativeStyle = defaultNegativeStyle(script),
    override val digitStyle: DigitStyle = DigitStyle.COMMON,
    val liangPolicy: LiangPolicy = defaultLiangPolicy(script),
    override val decimalPointStyle: DecimalPointStyle = defaultDecimalStyle(script),
    override val fractionStyle: FractionStyle = FractionStyle.MIXED
): CjkNumberFormat, ScriptTyped(ZHScripts, "ChineseNumberFormat") {
    init {
        checkCompatability(script)
        positiveStyle.checkCompatability(script)
        negativeStyle.checkCompatability(script)
        liangPolicy.checkCompatability(script)
        decimalPointStyle.checkCompatability(script)
    }

    companion object {
        private fun <A : Any> defaulter(zhHant: A, zhHans: A): (Script) -> A = {
            if (it == Script.ZH_HANT) zhHant else zhHans
        }

        internal val defaultLiangPolicy = defaulter(
            LiangPolicy.TraditionalChineseSpoken,
            LiangPolicy.SimplifiedChineseSpoken
        )

        internal val defaultNegativeStyle = defaulter(
            NegativeStyle.TraditionalChineseFu,
            NegativeStyle.SimplifiedChineseFu
        )

        internal val defaultDecimalStyle = defaulter(
            DecimalPointStyle.TraditionalChinese,
            DecimalPointStyle.SimplifiedChinese
        )
    }
}

class JapaneseNumberFormat(
    override val positiveStyle: PositiveStyle = PositiveStyle.Omit,
    override val negativeStyle: NegativeStyle = NegativeStyle.JapaneseKatakana,
    override val digitStyle: DigitStyle = DigitStyle.COMMON,
    override val decimalPointStyle: DecimalPointStyle = DecimalPointStyle.Point,
    override val fractionStyle: FractionStyle = FractionStyle.MIXED,
): CjkNumberFormat, ScriptTyped(JPScript, "JapaneseNumberFormat") {
    override val script = Script.JP

    init {
        checkCompatability(script)
        positiveStyle.checkCompatability(script)
        negativeStyle.checkCompatability(script)
        decimalPointStyle.checkCompatability(script)
    }
}

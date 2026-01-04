package org.vorpal.kosmos.rec.cjk

import org.vorpal.kosmos.std.Rational
import java.math.BigDecimal
import java.math.BigInteger


sealed class Locale(
    val englishName: String,
    val nativeName: String
) {
    override fun toString(): String =
        "$englishName / $nativeName"
}

sealed class HanLocale(englishName: String, nativeName: String): Locale(englishName, nativeName) {
    object Traditional: HanLocale("Chinese (Traditional)", "中文繁體")
    object Simplified:  HanLocale("Chinese (Simplified)", "中文简体")
}

sealed class JapaneseLocale(englishName: String, nativeName: String) : Locale(englishName, nativeName) {
    object Japanese : JapaneseLocale("Japanese", "日本語")
}

private val ZHLocaleSet: Set<Locale> = setOf(HanLocale.Traditional, HanLocale.Simplified)
private val ZHTradLocaleSet: Set<Locale> = setOf(HanLocale.Traditional)
private val ZHSimpLocaleSet: Set<Locale> = setOf(HanLocale.Simplified)
private val JPLocaleSet: Set<Locale> = setOf(JapaneseLocale.Japanese)
private val FullLocaleSet: Set<Locale> = ZHLocaleSet + JPLocaleSet

sealed class LocaleTyped(
    private val locales: Set<Locale>,
    private val description: String)
{
    init {
        require(locales.isNotEmpty()) { "No locale associated with setting $description" }
    }

    private val localeRepresentation: String by lazy {
        when (locales.size) {
            1 -> locales.single().toString()
            else -> locales.joinToString(separator = ", ")
        }
    }

    fun checkCompatibility(selectedLocale: Locale) {
        require(selectedLocale in locales) {
            "Locale incompatibility: selected is $selectedLocale, permitted: $localeRepresentation"
        }
    }
}

sealed class ZeroStyle(val value: String, locales: Set<Locale>):
    LocaleTyped(locales, "ZeroStyle") {

    object Chinese: ZeroStyle("零", ZHLocaleSet)
    object JapaneseKanji: ZeroStyle("零", JPLocaleSet)
    object JapaneseCircle: ZeroStyle("〇", JPLocaleSet)
}

sealed class PositiveStyle(val prefix: String, locales: Set<Locale>):
    LocaleTyped(locales, "PositiveStyle") {

    object Omit: PositiveStyle("", FullLocaleSet)
    object Plus: PositiveStyle("+", FullLocaleSet)
    object ChineseZheng: PositiveStyle("正", ZHLocaleSet)
    object JapaneseKatakana: PositiveStyle("プラス", JPLocaleSet)
}

sealed class NegativeStyle(val prefix: String, locales: Set<Locale>):
    LocaleTyped(locales, "NegativeStyle") {

    object Minus: NegativeStyle("-", FullLocaleSet)
    object TraditionalChineseFu: NegativeStyle("負", ZHTradLocaleSet)
    object SimplifiedChineseFu: NegativeStyle("负", ZHSimpLocaleSet)
    object JapaneseKatakana: NegativeStyle("マイナス", JPLocaleSet)
}

sealed class DecimalPointStyle(val point: String, locales: Set<Locale>):
    LocaleTyped(locales, "DecimalPointStyle") {

    object CJKPoint: DecimalPointStyle("．", FullLocaleSet)
    object CJKMiddleDot: DecimalPointStyle("・", JPLocaleSet)
    object CJKPeriod: DecimalPointStyle("。", FullLocaleSet)
    object CJKComma: DecimalPointStyle("，", FullLocaleSet)
    object TraditionalChineseDian: DecimalPointStyle("點", ZHTradLocaleSet)
    object SimplifiedChineseDian: DecimalPointStyle("点", ZHSimpLocaleSet)
    object Point: DecimalPointStyle(".", FullLocaleSet)
    object Comma: DecimalPointStyle(",", FullLocaleSet)
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
 * [TraditionalChineseLiang] and [SimplifiedChineseLiang] mean:
 * - 二 will be used before 十 / 百.
 * - 兩 / 两 will be used before 百 / 千 / 萬 / 億 /...
 */
sealed class LiangPolicy(val value: String, locales: Set<Locale>): LocaleTyped(locales, "LiangPolicy") {
    object Never: LiangPolicy("二", ZHLocaleSet)
    object TraditionalChineseLiang: LiangPolicy("兩", ZHTradLocaleSet)
    object SimplifiedChineseLiang: LiangPolicy("两", ZHSimpLocaleSet)
}


internal typealias LocaleDict = Map<Locale, String>

/**
 * Create a map from [Locale] to representation. Note that this is flexible to simplify the case where
 * the simplified Chinese character either differs or doesn't differ from the traditional character.
 */
private fun makeMap(zhTrad: String, zhSimp: String? = null, jp: String? = null): LocaleDict =
    buildMap {
        put(HanLocale.Traditional, zhTrad)
        put(HanLocale.Simplified, (zhSimp ?: zhTrad))
        if (jp != null) put(JapaneseLocale.Japanese, jp)
    }

/**
 * Note: standardChar is identical across all three locales, and is always used when financial mode
 * is enabled and there is no entry for the financial mode character (which happens frequently in
 * Japanese).
 */
internal enum class SmallNumbers(
    val value: Int,
    val standardChar: String,
    val financialChars: LocaleDict
) {
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
    val chars: LocaleDict,
    val lowerValue: BigInteger = BigInteger.TEN.pow(power10),
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

private val myriadByPow10 = Myriad.entries.associateBy { it.power10 }


/**
 * Information required for taking an Arabic number and formatting it in Chinese characters.
 */
sealed interface CjkNumberFormat {
    val locale: Locale
    val positiveStyle: PositiveStyle
    val negativeStyle: NegativeStyle
    val digitStyle: DigitStyle
    val zeroStyle: ZeroStyle
    val decimalPointStyle: DecimalPointStyle
    val fractionStyle: FractionStyle

    fun format(bigInteger: BigInteger): String
    fun format(rational: Rational): String
    fun formatPercent(decimal: BigDecimal): String

    /**
     * Format a BigDecimal precisely. This relies on simple formatting rules, i.e.:
     * - The formatting for the BigInteger part of the BigDecimal.
     * - Then simply appending the separator and listing off the digits one after another.
     */
    fun format(decimal: BigDecimal): String {
        // Divide into the integer part and the fractional part.
        val integerPart = decimal.toBigInteger()
        val fractionalPart = decimal.remainder(BigDecimal.ONE).abs()

        return buildString {
            append(format(integerPart))

            // Only append the fractional part if it is nonzero.
            if (fractionalPart.signum() != 0) {
                append(decimalPointStyle.point)

                // Strip the "0." prefix and format each digit
                fractionalPart.toPlainString()
                    .substringAfter(".")
                    .forEach { digitChar ->
                        // For decimals, just use the digit character directly
                        val digit = digitChar.digitToInt()
                        val digitStr = if (digit == 0) zeroStyle.value
                                       else findSmallNumber(digit).charFor(locale, digitStyle)
                        append(digitStr)
                    }
            }
        }
    }
}

private fun SmallNumbers.charFor(
    locale: Locale,
    digitStyle: DigitStyle
): String =
    if (digitStyle == DigitStyle.FINANCIAL) financialChars[locale] ?: standardChar
    else standardChar

private val smallByValue = SmallNumbers.entries.associateBy { it.value }
private fun findSmallNumber(value: Int): SmallNumbers =
    requireNotNull(smallByValue[value]) { "No SmallNumbers entry for value=$value" }


private fun unitChar(
    pos: Int,
    locale: Locale,
    digitStyle: DigitStyle
): String {
    val unit = findSmallNumber(pos)
    return unit.charFor(locale, digitStyle)
}

private fun splitBase10000(n: BigInteger): List<Int> {
    val base = BigInteger.valueOf(10000)
    var remain = n
    val groups = mutableListOf<Int>()

    while (remain > BigInteger.ZERO) {
        val divRem = remain.divideAndRemainder(base)
        groups.add(divRem[1].toInt())
        remain = divRem[0]
    }

    return groups
}

class ChineseNumberFormat(
    override val locale: HanLocale,
    override val positiveStyle: PositiveStyle = PositiveStyle.Omit,
    override val negativeStyle: NegativeStyle = defaultNegativeStyle(locale),
    override val digitStyle: DigitStyle = DigitStyle.COMMON,
    val liangPolicy: LiangPolicy = defaultLiangPolicy(locale),
    override val decimalPointStyle: DecimalPointStyle = defaultDecimalStyle(locale),
    override val fractionStyle: FractionStyle = FractionStyle.MIXED
): CjkNumberFormat, LocaleTyped(ZHLocaleSet, "ChineseNumberFormat") {
    override val zeroStyle: ZeroStyle = ZeroStyle.Chinese

    init {
        checkCompatibility(locale)
        positiveStyle.checkCompatibility(locale)
        negativeStyle.checkCompatibility(locale)
        liangPolicy.checkCompatibility(locale)
        zeroStyle.checkCompatibility(locale)
        decimalPointStyle.checkCompatibility(locale)
    }

    private fun formatGroup0to9999(group: Int): String {
        require(group in 0..9999)

        if (group == 0) return ""

        val thousands = group / 1000
        val hundreds = (group % 1000) / 100
        val tens = (group % 100) / 10
        val ones = group % 10

        fun digitChar(value: Int, pos: Int): String {
            if (value == 0) return ""

            val small = findSmallNumber(value)
            if (digitStyle == DigitStyle.FINANCIAL) return small.charFor(locale, digitStyle)
            if (value == 2 && (pos == 1000 || pos == 100)) return liangPolicy.value
            return small.standardChar
        }

        return buildString {
            var emittedAny = false
            var pendingZero = false

            fun emitDigitAndUnit(value: Int, pos: Int) {
                if (value == 0) {
                    if (emittedAny) pendingZero = true
                    return
                }
                if (pendingZero) {
                    append(zeroStyle.value)
                    pendingZero = false
                }

                append(digitChar(value, pos))
                append(unitChar(pos, locale, digitStyle))
                emittedAny = true
            }

            emitDigitAndUnit(thousands, 1000)
            emitDigitAndUnit(hundreds, 100)

            // 十 (canonical: 10..19 omit leading 一)
            if (tens == 0) {
                if (emittedAny && ones != 0) pendingZero = true
            } else {
                if (pendingZero) {
                    append(zeroStyle.value)
                    pendingZero = false
                }

                if (tens == 1 && !emittedAny && digitStyle != DigitStyle.FINANCIAL) {
                    // 十, 十一, 十二, ...
                    append(unitChar(10, locale, digitStyle))
                } else {
                    append(digitChar(tens, 10))
                    append(unitChar(10, locale, digitStyle))
                }
                emittedAny = true
            }

            if (ones != 0) {
                if (pendingZero) append(zeroStyle.value)
                append(digitChar(ones, 1))
            }
        }
    }

    override fun format(bigInteger: BigInteger): String {
        if (bigInteger == BigInteger.ZERO) return zeroStyle.value

        val isNegative = bigInteger.signum() < 0
        val abs = bigInteger.abs()
        val groups = splitBase10000(abs)

        val body = buildString {
            var zeroPending = false

            for (i in groups.lastIndex downTo 0) {
                val g = groups[i]
                if (g != 0 && i > 0)
                    require(i * 4 <= Myriad.entries.last().power10) {
                        "Number too large for configured myriad table."
                    }

                if (g == 0) {
                    if (isNotEmpty()) zeroPending = true
                    continue
                }

                val needsLeadingZeroBridge =
                    isNotEmpty() &&
                        i != groups.lastIndex &&
                        g < 1000

                if (zeroPending || needsLeadingZeroBridge) {
                    append(zeroStyle.value)
                    zeroPending = false
                }

                val groupStr = formatGroup0to9999(g)
                val adjustedGroupStr =
                    if (digitStyle != DigitStyle.FINANCIAL &&
                        liangPolicy !is LiangPolicy.Never &&
                        g == 2 &&
                        i >= 1
                    ) liangPolicy.value
                    else groupStr
                append(adjustedGroupStr)

                val label = if (i == 0) ""
                            else myriadByPow10[i * 4]?.chars?.get(locale).orEmpty()
                append(label)
            }
        }

        val signPrefix = if (isNegative) negativeStyle.prefix else positiveStyle.prefix
        return signPrefix + body
    }

    override fun format(rational: Rational): String {
        if (rational.isInteger) return format(rational.n)
        val abs = rational.abs()
        val sgn = rational.signum.toBigInteger()

        // Divide the number up into its constituents.
        // These should always be positive.
        val wholeNumber = if (fractionStyle == FractionStyle.MIXED) abs.whole() else BigInteger.ZERO
        val numerator = if (fractionStyle == FractionStyle.MIXED) abs.frac().n else abs.n
        val denominator = abs.d

        return if (fractionStyle == FractionStyle.MIXED)
            "${format(sgn * wholeNumber)}又${format(denominator)}分之${format(numerator)}"
        else "${format(denominator)}分之${format(sgn * numerator)}"
    }

    companion object {
        private fun <A : Any> defaulter(trad: A, simp: A): (HanLocale) -> A = {
            if (it == HanLocale.Traditional) trad else simp
        }

        internal val defaultLiangPolicy = defaulter(
            LiangPolicy.TraditionalChineseLiang,
            LiangPolicy.SimplifiedChineseLiang
        )

        internal val defaultNegativeStyle = defaulter(
            NegativeStyle.TraditionalChineseFu,
            NegativeStyle.SimplifiedChineseFu
        )

        internal val defaultDecimalStyle = defaulter(
            DecimalPointStyle.TraditionalChineseDian,
            DecimalPointStyle.SimplifiedChineseDian
        )
    }
}

class JapaneseNumberFormat(
    override val positiveStyle: PositiveStyle = PositiveStyle.Omit,
    override val negativeStyle: NegativeStyle = NegativeStyle.JapaneseKatakana,
    override val digitStyle: DigitStyle = DigitStyle.COMMON,
    override val zeroStyle: ZeroStyle = ZeroStyle.JapaneseCircle,
    override val decimalPointStyle: DecimalPointStyle = DecimalPointStyle.CJKPoint,
    override val fractionStyle: FractionStyle = FractionStyle.MIXED,
): CjkNumberFormat, LocaleTyped(JPLocaleSet, "JapaneseNumberFormat") {
    override val locale: JapaneseLocale = JapaneseLocale.Japanese

    init {
        checkCompatibility(locale)
        positiveStyle.checkCompatibility(locale)
        negativeStyle.checkCompatibility(locale)
        zeroStyle.checkCompatibility(locale)
        decimalPointStyle.checkCompatibility(locale)
    }
}

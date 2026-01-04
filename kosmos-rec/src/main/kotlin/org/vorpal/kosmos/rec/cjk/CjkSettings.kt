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
    object ZH_HANT: HanLocale("Chinese (Traditional)", "中文繁體")
    object ZH_HANS: HanLocale("Chinese (Simplified)", "中文简体")
}

sealed class JapaneseLocale(englishName: String, nativeName: String) : Locale(englishName, nativeName) {
    object JP : JapaneseLocale("Japanese", "日本語")
}

private val ZHLocaleSet: Set<Locale> = setOf(HanLocale.ZH_HANT, HanLocale.ZH_HANS)
private val ZHTradLocaleSet: Set<Locale> = setOf(HanLocale.ZH_HANT)
private val ZHSimpLocaleSet: Set<Locale> = setOf(HanLocale.ZH_HANS)
private val JPLocaleSet: Set<Locale> = setOf(JapaneseLocale.JP)
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
private val zeros: Set<String> = setOf(
    ZeroStyle.Chinese.value,
    ZeroStyle.JapaneseKanji.value,
    ZeroStyle.JapaneseCircle.value
)

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
        put(HanLocale.ZH_HANT, zhTrad)
        put(HanLocale.ZH_HANS, (zhSimp ?: zhTrad))
        if (jp != null) put(JapaneseLocale.JP, jp)
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
    fun format(decimal: BigDecimal): String
    fun format(rational: Rational): String
    fun formatPercent(decimal: BigDecimal): String
}
private fun SmallNumbers.charFor(
    locale: Locale,
    digitStyle: DigitStyle
): String =
    if (digitStyle == DigitStyle.FINANCIAL) financialChars[locale] ?: standardChar
    else standardChar

private fun findSmallNumber(value: Int): SmallNumbers =
    SmallNumbers.entries.first { it.value == value }

private fun unitChar(
    pos: Int,
    locale: Locale,
    digitStyle: DigitStyle
): String {
    val unit = findSmallNumber(pos)
    return unit.charFor(locale, digitStyle)
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

    override fun format(bigInteger: BigInteger): String {
        // Format a single digit at a certain position.
        fun formatDigit(value: Int, position: Int): String =
            if (value > 0) {
                val smallNumber = findSmallNumber(value)

                // Financial style always takes precedence.
                if (digitStyle == DigitStyle.FINANCIAL)
                    smallNumber.financialChars.getOrDefault(locale, smallNumber.standardChar)
                else if (value == 2 && (position == 1000 || position == 100))
                    liangPolicy.value
                else
                    smallNumber.standardChar
                }
            else ""

        // Format a myriad group.
        fun formatMyriad(group: Int): String =
            listOf(1000, 100, 10, 1).fold(group to "") { (remain, str), pos ->
                val nextRemain = remain % pos
                val digitSymbol = formatDigit(remain / pos, pos)
                if (digitSymbol.isNotEmpty()) {
                    // We only want the position symbol for 10, 100, 1000.
                    val posSymbol = if (pos > 1) {
                        val posLookup = findSmallNumber(pos)
                        if (digitStyle == DigitStyle.FINANCIAL) posLookup.financialChars.getOrDefault(
                            locale,
                            posLookup.standardChar
                        )
                        else posLookup.standardChar
                    } else ""

                    nextRemain to ("$str$digitSymbol$posSymbol")
                } else nextRemain to str
            }.second

        // Iterate over the myriads in reverse order and determine if they apply.
        val unsignedFormat = Myriad.entries.reversed().fold(bigInteger.abs() to "") { (remain, str), myriad ->
            val nextRemain = remain % myriad.lowerValue
            when (val group = remain / myriad.lowerValue) {
                BigInteger.ZERO if str.isEmpty() -> nextRemain to str
                BigInteger.ZERO if str.last().toString() !in zeros -> nextRemain to (str + zeroStyle.value + myriad.chars)
                else -> nextRemain to (str + formatMyriad(group.toInt()) + myriad.chars)
            }
        }.second

        return if (bigInteger > BigInteger.ZERO) positiveStyle.prefix + unsignedFormat
        else if (bigInteger < BigInteger.ZERO) negativeStyle.prefix + unsignedFormat
        else unsignedFormat
    }

    companion object {
        private fun <A : Any> defaulter(zhHant: A, zhHans: A): (HanLocale) -> A = {
            if (it == HanLocale.ZH_HANT) zhHant else zhHans
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
    override val locale: JapaneseLocale = JapaneseLocale.JP

    init {
        checkCompatibility(locale)
        positiveStyle.checkCompatibility(locale)
        negativeStyle.checkCompatibility(locale)
        zeroStyle.checkCompatibility(locale)
        decimalPointStyle.checkCompatibility(locale)
    }
}

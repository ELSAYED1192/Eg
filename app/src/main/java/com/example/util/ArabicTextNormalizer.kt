package com.example.util

object ArabicTextNormalizer {

    private val DIACRITICS = Regex("[\u064B-\u065F\u0670\u0640]") // Tanween, Harakat, Shadda, Sukoon, Tatweel

    fun normalize(text: String): String {
        if (text.isBlank()) return ""
        var result = text.trim()

        // Remove diacritics and tatweel
        result = DIACRITICS.replace(result, "")

        // Normalize Arabic numerals to Latin numerals
        result = convertArabicDigitsToLatin(result)

        // Normalize Alif forms
        result = result.replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')

        // Normalize Taa Marbuta and Haa
        result = result.replace('ة', 'ه')

        // Normalize Alif Maqsura and Yaa
        result = result.replace('ى', 'ي')

        // Normalize Hamza forms
        result = result.replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace("ء", "")

        return result.lowercase().trim()
    }

    fun convertArabicDigitsToLatin(input: String): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        var output = input
        for (i in arabicDigits.indices) {
            output = output.replace(arabicDigits[i], (i + 48).toChar())
        }
        return output
    }

    /**
     * Fuzzy contains check: checks if query keywords are all present in target text,
     * or if target text contains the query as a continuous sequence.
     */
    fun matchesFuzzy(target: String, query: String): Boolean {
        val normTarget = normalize(target)
        val normQuery = normalize(query)

        if (normTarget.contains(normQuery) || normQuery.contains(normTarget)) {
            return true
        }

        val queryWords = normQuery.split(" ").filter { it.length > 2 }
        if (queryWords.isEmpty()) return false

        // Match if all significant words appear, or at least 2 words with 75% match
        val matchedWords = queryWords.count { word -> normTarget.contains(word) }
        return matchedWords == queryWords.size || (matchedWords >= 2 && (matchedWords.toDouble() / queryWords.size) >= 0.75)
    }
}

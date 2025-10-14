package com.example.shared.utils

import java.text.Normalizer
import java.util.UUID

object SlugUtils {
    private const val RANDOM_SUFFIX_LENGTH = 8

    fun toSlug(title: String): String {
        val normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
        val slug =
            normalized
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                .lowercase()
                .replace("[^a-z0-9\\s-]".toRegex(), "")
                .trim()
                .replace("\\s+".toRegex(), "-")
                .replace("-+".toRegex(), "-")

        val suffix = UUID.randomUUID().toString().substring(0, RANDOM_SUFFIX_LENGTH)
        return "$slug-$suffix"
    }
}

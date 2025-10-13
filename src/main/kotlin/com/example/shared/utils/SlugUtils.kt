package com.example.shared.utils

import java.text.Normalizer
import java.util.UUID

object SlugUtils {
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

        val suffix = UUID.randomUUID().toString().substring(0, 8)
        return "$slug-$suffix"
    }
}

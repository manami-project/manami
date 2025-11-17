package io.github.manamiproject.manami.app.search

enum class SearchConjunction {
    OR,
    AND;

    companion object {
        fun of(value: String): SearchConjunction {
            return entries.find { it.toString().equals(value, ignoreCase = true) } ?: throw IllegalArgumentException("No value for [$value]")
        }
    }
}
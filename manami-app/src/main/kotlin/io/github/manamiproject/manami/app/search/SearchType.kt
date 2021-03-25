package io.github.manamiproject.manami.app.search

enum class SearchType {
    OR,
    AND;

    companion object {
        fun of(value: String): SearchType {
            return values().find { it.toString().equals(value, ignoreCase = true) } ?: throw IllegalArgumentException("No value for [$value]")
        }
    }
}
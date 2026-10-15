package com.morillo.spark

/** Word normalisation shared by the RDD and DataFrame implementations, so both produce identical counts. */
object WordCleaner {

  /** Regex used to split a line into words. */
  val Whitespace: String = "\\s+"

  /** Regex matching every character that is stripped from a word. */
  val NonAlphanumeric: String = "[^a-zA-Z0-9]"

  /** Lowercases the word and removes everything that is not a letter or digit. May return an empty string. */
  def clean(word: String): String = word.toLowerCase.replaceAll(NonAlphanumeric, "")
}

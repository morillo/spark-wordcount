package com.morillo.spark

import org.scalatest.funsuite.AnyFunSuite

class WordCleanerTest extends AnyFunSuite {

  test("clean lowercases the word") {
    assert(WordCleaner.clean("Stark") == "stark")
  }

  test("clean strips punctuation and keeps digits") {
    assert(WordCleaner.clean("Lannister,") == "lannister")
    assert(WordCleaner.clean("\"Winterfell\"") == "winterfell")
    assert(WordCleaner.clean("7Kingdoms!") == "7kingdoms")
  }

  test("clean returns an empty string for pure punctuation") {
    assert(WordCleaner.clean("--") == "")
    assert(WordCleaner.clean("") == "")
  }
}

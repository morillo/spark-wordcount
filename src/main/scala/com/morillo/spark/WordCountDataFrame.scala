package com.morillo.spark

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

/** Word count with the DataFrame API. Writes the same `(word,count)` text format as [[WordCount]]. */
object WordCountDataFrame {

  /** Counts cleaned words in the `value` column of `lines`, ordered by descending count then word. */
  def countWords(lines: DataFrame): DataFrame =
    lines
      .select(explode(split(col("value"), WordCleaner.Whitespace)).as("word"))
      .select(regexp_replace(lower(col("word")), WordCleaner.NonAlphanumeric, "").as("clean_word"))
      .filter(col("clean_word") =!= "")
      .groupBy("clean_word")
      .agg(count("*").as("count"))
      .orderBy(desc("count"), asc("clean_word"))

  /** Formats a `(clean_word, count)` DataFrame as single-column `(word,count)` text, matching the RDD output. */
  def formatForOutput(wordCounts: DataFrame): DataFrame =
    wordCounts.select(concat(lit("("), col("clean_word"), lit(","), col("count"), lit(")")).as("result"))

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      System.err.println("Usage: WordCountDataFrame <input_path> <output_path>")
      System.exit(1)
    }

    val inputPath = args(0)
    val outputPath = args(1)

    val spark = SparkSession
      .builder()
      .appName("Game of Thrones Word Count - DataFrame API")
      .getOrCreate()

    try {
      val wordCounts = countWords(spark.read.text(inputPath))

      // Equivalent SQL:
      //   SELECT clean_word, COUNT(*) AS count FROM words GROUP BY clean_word ORDER BY count DESC, clean_word ASC

      println("Top 20 most frequent words:")
      wordCounts.show(20)

      formatForOutput(wordCounts).write.mode("overwrite").text(outputPath)

      println("Word count completed successfully!")
      println(s"Input: $inputPath")
      println(s"Output: $outputPath")
      println(s"Total unique words: ${wordCounts.count()}")
    } finally {
      spark.stop()
    }
  }
}

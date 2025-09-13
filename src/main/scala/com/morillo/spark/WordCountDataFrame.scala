package com.morillo.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructField, StructType}

object WordCountDataFrame {

  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      System.err.println("Usage: WordCountDataFrame <input_path> <output_path>")
      System.exit(1)
    }

    val inputPath = args(0)
    val outputPath = args(1)

    val spark = SparkSession.builder()
      .appName("Game of Thrones Word Count - DataFrame API")
      .getOrCreate()

    import spark.implicits._

    try {
      // Read text file as DataFrame
      val textDF = spark.read
        .option("wholetext", "false")
        .text(inputPath)

      // Split lines into words and explode into individual rows
      val wordsDF = textDF
        .select(explode(split(col("value"), "\\s+")).as("word"))
        .filter(col("word") =!= "")

      // Clean and normalize words: lowercase and remove punctuation
      val cleanWordsDF = wordsDF
        .select(regexp_replace(lower(col("word")), "[^a-zA-Z0-9]", "").as("clean_word"))
        .filter(col("clean_word") =!= "")

      // Count words using DataFrame aggregation
      val wordCountsDF = cleanWordsDF
        .groupBy("clean_word")
        .agg(count("*").as("count"))
        .orderBy(desc("count"), asc("clean_word"))

      // Alternative using SQL syntax
      // cleanWordsDF.createOrReplaceTempView("words")
      // val wordCountsDF = spark.sql("""
      //   SELECT clean_word, COUNT(*) as count
      //   FROM words
      //   GROUP BY clean_word
      //   ORDER BY count DESC, clean_word ASC
      // """)

      // Show sample results
      println("Top 20 most frequent words:")
      wordCountsDF.show(20)

      // Save results
      wordCountsDF
        .select(concat(lit("("), col("clean_word"), lit(","), col("count"), lit(")")).as("result"))
        .write
        .mode("overwrite")
        .option("header", "false")
        .text(outputPath)

      println(s"Word count completed successfully!")
      println(s"Input: $inputPath")
      println(s"Output: $outputPath")
      println(s"Total unique words: ${wordCountsDF.count()}")

    } finally {
      spark.stop()
    }
  }
}
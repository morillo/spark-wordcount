package com.morillo.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/** Word count with the classic RDD API. Output lines look like `(word,count)`, most frequent first. */
object WordCount {

  /** Counts cleaned words in `lines`, sorted by descending frequency. */
  def countWords(lines: RDD[String]): RDD[(String, Int)] =
    lines
      .flatMap(_.split(WordCleaner.Whitespace))
      .map(WordCleaner.clean)
      .filter(_.nonEmpty)
      .map(word => (word, 1))
      .reduceByKey(_ + _)
      .sortBy(_._2, ascending = false)

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      System.err.println("Usage: WordCount <input_path> <output_path>")
      System.exit(1)
    }

    val inputPath = args(0)
    val outputPath = args(1)

    val spark = SparkSession
      .builder()
      .appName("Game of Thrones Word Count")
      .getOrCreate()

    try {
      countWords(spark.sparkContext.textFile(inputPath)).saveAsTextFile(outputPath)

      println("Word count completed successfully!")
      println(s"Input: $inputPath")
      println(s"Output: $outputPath")
    } finally {
      spark.stop()
    }
  }
}

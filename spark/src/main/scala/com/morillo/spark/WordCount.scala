package com.morillo.spark

import org.apache.spark.sql.SparkSession

object WordCount {

  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      System.err.println("Usage: WordCount <input_path> <output_path>")
      System.exit(1)
    }

    val inputPath = args(0)
    val outputPath = args(1)

    val spark = SparkSession.builder()
      .appName("Game of Thrones Word Count")
      .getOrCreate()

    val sc = spark.sparkContext

    try {
      val textFile = sc.textFile(inputPath)
      val wordCounts = textFile
        .flatMap(line => line.split("\\s+"))
        .filter(_.nonEmpty)
        .map(word => (word.toLowerCase.replaceAll("[^a-zA-Z0-9]", ""), 1))
        .filter(_._1.nonEmpty)
        .reduceByKey(_ + _)
        .sortBy(_._2, ascending = false)

      wordCounts.saveAsTextFile(outputPath)

      println(s"Word count completed successfully!")
      println(s"Input: $inputPath")
      println(s"Output: $outputPath")

    } finally {
      spark.stop()
    }
  }
}
package com.morillo.spark

import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

class WordCountTest extends AnyFunSuite with BeforeAndAfterAll {

  private var spark: SparkSession = _

  private val lines = Seq(
    "House Stark of Winterfell, House Lannister",
    "stark STARK Stark.",
    "\"Lannister\" -- Baratheon",
    "   "
  )

  private val expected =
    Map("stark" -> 4, "lannister" -> 2, "house" -> 2, "of" -> 1, "winterfell" -> 1, "baratheon" -> 1)

  override def beforeAll(): Unit = {
    spark = SparkSession
      .builder()
      .master("local[2]")
      .appName("WordCountTest")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.shuffle.partitions", "2")
      .getOrCreate()
  }

  override def afterAll(): Unit = spark.stop()

  test("RDD API counts cleaned words, most frequent first") {
    val result = WordCount.countWords(spark.sparkContext.parallelize(lines)).collect()

    assert(result.toMap == expected)
    assert(result.head == ("stark", 4))
  }

  test("DataFrame API counts cleaned words ordered by count desc, then word asc") {
    val result = WordCountDataFrame
      .countWords(spark.createDataFrame(lines.map(Tuple1(_))).toDF("value"))
      .collect()
      .map(row => (row.getString(0), row.getLong(1).toInt))

    assert(result.toMap == expected)
    assert(result.take(3).toSeq == Seq(("stark", 4), ("house", 2), ("lannister", 2)))
  }

  test("DataFrame output lines use the same (word,count) format as the RDD API") {
    val wordCounts = WordCountDataFrame.countWords(spark.createDataFrame(lines.map(Tuple1(_))).toDF("value"))
    val formatted = WordCountDataFrame.formatForOutput(wordCounts).collect().map(_.getString(0))
    val rddLines = WordCount.countWords(spark.sparkContext.parallelize(lines)).collect().map(_.toString)

    assert(formatted.head == "(stark,4)")
    assert(formatted.toSet == rddLines.toSet)
  }
}

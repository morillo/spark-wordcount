package com.cloudera.spark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object WordCount {
  
  def main(args: Array[String]) = {
    
    val conf = new SparkConf()
    .setAppName("WordCount")
    .setMaster("local")
    val sc = new SparkContext(conf)
    
    val sevenKingdoms = sc.textFile("westeros.txt")
    sevenKingdoms.flatMap { line => line.split(" ") }
    .map { word => (word, 1) }
    .reduceByKey(_ + _)
    .saveAsTextFile("sevenkingdoms")
     
  }
}
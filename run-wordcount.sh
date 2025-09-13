#!/bin/bash

# Game of Thrones Word Count - RDD API - Spark Submit Runner
# Usage: ./run-wordcount.sh <input_file> <output_directory>

set -e

# Check arguments
if [ $# -ne 2 ]; then
    echo "Usage: $0 <input_file> <output_directory>"
    echo "Example: $0 westeros.txt sevenkingdoms"
    echo ""
    echo "This script uses the classic RDD API. For DataFrame API, use:"
    echo "./run-wordcount-dataframe.sh <input_file> <output_directory>"
    exit 1
fi

INPUT_FILE=$1
OUTPUT_DIR=$2

# Check if input file exists
if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file '$INPUT_FILE' not found!"
    exit 1
fi

# Check if jar was created by Maven build
JAR_FILE="target/spark-wordcount-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first with: mvn clean package"
    exit 1
fi

# Remove output directory if it exists (Spark requires non-existent output path)
if [ -d "$OUTPUT_DIR" ]; then
    echo "Warning: Output directory '$OUTPUT_DIR' exists. Removing it..."
    rm -rf "$OUTPUT_DIR"
fi

# Submit the job to Spark using RDD API
echo "Submitting Spark job (RDD API)..."
echo "Input: $INPUT_FILE"
echo "Output: $OUTPUT_DIR"

spark-submit \
  --class com.morillo.spark.WordCount \
  --master local[*] \
  --deploy-mode client \
  --driver-memory 2g \
  --executor-memory 1g \
  "$JAR_FILE" \
  "$INPUT_FILE" \
  "$OUTPUT_DIR"

echo ""
echo "Job completed! Check output in: $OUTPUT_DIR"
echo ""
echo "To view results:"
echo "cat $OUTPUT_DIR/part-*"
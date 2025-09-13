# Game of Thrones Word Count - Apache Spark Application

A modernized Apache Spark application that performs word counting on text files, featuring Game of Thrones character data from the HBO series.

## 🚀 Features

- **Modern Spark 4.0.1** with Scala 2.13 support
- **Java 17** compatibility
- **Two implementations**: Classic RDD API and modern DataFrame/SparkSQL API
- **Command-line arguments** for flexible input/output paths
- **Automated build and execution** via shell scripts
- **Word processing** with case normalization and punctuation removal
- **Frequency-sorted results** (most common words first)

## 📋 Prerequisites

- **Apache Spark 4.0.1** (installed via Homebrew)
- **OpenJDK 17** (installed via Homebrew)
- **Maven 3.x** (installed via Homebrew)

Verify your installations:
```bash
spark-submit --version
java --version
mvn --version
```

## 🏗️ Project Structure

```
.
├── README.md
├── pom.xml                                    # Maven configuration
├── src/main/scala/com/morillo/spark/
│   ├── WordCount.scala                        # RDD API implementation
│   └── WordCountDataFrame.scala               # DataFrame/SparkSQL API implementation
├── run-wordcount.sh                          # RDD API execution script
├── run-wordcount-dataframe.sh                # DataFrame API execution script
├── westeros.txt                              # Sample Game of Thrones data
└── target/                                   # Build outputs (generated)
```

## 🔧 Building the Project

**You must build the project before running the execution scripts:**

```bash
# Set Java 17 for Maven (if using Java 24 by default)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Build the project
mvn clean package
```

This creates `target/spark-wordcount-1.0.0.jar` ready for Spark submission.

**Note**: The execution scripts (`run-wordcount.sh` and `run-wordcount-dataframe.sh`) do NOT build the project automatically. You must run the Maven build command first.

## 🚀 Running the Application

### Prerequisites: Build First
```bash
# Build the project (required before running scripts)
mvn clean package
```

### Option 1: RDD API Implementation (Classic Approach)

```bash
# Make script executable (first time only)
chmod +x run-wordcount.sh

# Run with Game of Thrones data
./run-wordcount.sh westeros.txt sevenkingdoms

# Run with custom input/output
./run-wordcount.sh /path/to/input.txt /path/to/output_directory
```

### Option 2: DataFrame API Implementation (Modern SparkSQL)

```bash
# Make script executable (first time only)
chmod +x run-wordcount-dataframe.sh

# Run with Game of Thrones data
./run-wordcount-dataframe.sh westeros.txt sevenkingdoms-dataframe

# Run with custom input/output
./run-wordcount-dataframe.sh /path/to/input.txt /path/to/output_directory
```

### Option 3: Direct spark-submit

```bash
# Build first
mvn clean package

# Submit RDD API version to Spark
spark-submit \
  --class com.morillo.spark.WordCount \
  --master local[*] \
  --deploy-mode client \
  --driver-memory 2g \
  --executor-memory 1g \
  target/spark-wordcount-1.0.0.jar \
  westeros.txt \
  sevenkingdoms

# Submit DataFrame API version to Spark
spark-submit \
  --class com.morillo.spark.WordCountDataFrame \
  --master local[*] \
  --deploy-mode client \
  --driver-memory 2g \
  --executor-memory 1g \
  target/spark-wordcount-1.0.0.jar \
  westeros.txt \
  sevenkingdoms-dataframe
```

## 📊 Sample Output

Running on the included `westeros.txt` produces frequency-sorted results:

```
(stark,6)
(baratheon,5)
(lannister,4)
(martell,4)
(tyrell,3)
(arryn,3)
(targaryen,3)
(robert,2)
(jon,2)
...
```

## 📁 Viewing Results

```bash
# View all output files
cat sevenkingdoms/part-*

# Or view individual partitions
ls sevenkingdoms/
cat sevenkingdoms/part-00000
```

## 🛠️ Configuration

### Maven Properties (pom.xml)

- **Spark Version**: 4.0.1
- **Scala Version**: 2.13.12
- **Java Target**: 17
- **Dependencies**: spark-core, spark-sql (scope: provided)

### Spark Configuration

The application uses these default Spark settings:
- **Master**: `local[*]` (all available cores)
- **Driver Memory**: 2GB
- **Executor Memory**: 1GB
- **Deploy Mode**: client

## 🔍 Application Logic

### RDD API Implementation (WordCount.scala)

1. **Input Validation**: Ensures exactly 2 arguments (input path, output path)
2. **Spark Session**: Creates session with descriptive app name
3. **Text Processing**:
   - Reads input file(s) using `sc.textFile()`
   - Splits lines on whitespace using `flatMap`
   - Filters empty strings
   - Normalizes to lowercase and removes punctuation using `map`
   - Filters empty results after cleaning
4. **Word Counting**: Uses `reduceByKey()` for distributed counting
5. **Sorting**: Results sorted by frequency using `sortBy()`
6. **Output**: Saves to specified directory path using `saveAsTextFile()`

### DataFrame API Implementation (WordCountDataFrame.scala)

1. **Input Validation**: Ensures exactly 2 arguments (input path, output path)
2. **Spark Session**: Creates session with descriptive app name
3. **Text Processing**:
   - Reads input file(s) using `spark.read.text()`
   - Splits lines using `explode(split())` functions
   - Filters empty strings using DataFrame operations
   - Normalizes using `lower()` and `regexp_replace()` functions
   - Filters empty results after cleaning
4. **Word Counting**: Uses `groupBy().agg(count())` for aggregation
5. **Sorting**: Results sorted using `orderBy(desc(), asc())`
6. **Output**: Saves using DataFrame `write.text()` operations
7. **Bonus**: Includes SQL query alternative (commented) and console output with statistics

## 🔧 Development

### Code Structure

- **Package**: `com.morillo.spark`
- **RDD API Class**: `WordCount`
- **DataFrame API Class**: `WordCountDataFrame`
- **Entry Points**: `main(args: Array[String])` in both classes

### Key Dependencies

```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-core_2.13</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-sql_2.13</artifactId>
    <version>4.0.1</version>
    <scope>provided</scope>
</dependency>
```

## 📝 Usage Examples

```bash
# Build first (always required)
mvn clean package

# Basic word count (RDD API)
./run-wordcount.sh westeros.txt got_results

# Basic word count (DataFrame API)
./run-wordcount-dataframe.sh westeros.txt got_results_df

# Process large text file (RDD API)
./run-wordcount.sh /data/books/complete_series.txt /results/word_analysis

# Process large text file (DataFrame API)
./run-wordcount-dataframe.sh /data/books/complete_series.txt /results/word_analysis_df

# Count words in log files (DataFrame API with better performance for complex queries)
./run-wordcount-dataframe.sh /var/log/application.log /analytics/log_words
```

## 🔄 RDD vs DataFrame API Comparison

| Feature | RDD API | DataFrame API |
|---------|---------|---------------|
| **Performance** | Good for simple operations | Better for complex operations with Catalyst optimizer |
| **Code Style** | Functional programming style | SQL-like operations |
| **Type Safety** | Compile-time type safety | Runtime schema validation |
| **Optimization** | Manual optimization needed | Automatic query optimization |
| **Learning Curve** | Steeper for beginners | Easier for SQL users |
| **Best For** | Complex transformations, legacy code | Analytics, SQL users, performance-critical apps |

## 🐛 Troubleshooting

### Java Version Issues
```bash
# Ensure Java 17 is being used
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./run-wordcount.sh input.txt output
```

### Output Directory Exists
The script automatically removes existing output directories since Spark requires non-existent output paths.

### Build Failures
```bash
# Clean and rebuild
mvn clean
mvn compile
mvn package
```

## 📜 License

This project demonstrates Apache Spark capabilities using Game of Thrones character data for educational purposes.

## 🤝 Contributing

Feel free to submit issues and pull requests to improve this Spark application example.
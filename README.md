# Game of Thrones Word Count - Apache Spark Application

A small Apache Spark application that counts words in text files, using Game of Thrones character data from the HBO series as sample input. The same job is implemented twice, with the classic RDD API and with the DataFrame API, and both produce identical output.

[![CI](https://github.com/morillo/spark-wordcount/actions/workflows/ci.yml/badge.svg)](https://github.com/morillo/spark-wordcount/actions/workflows/ci.yml)

## Features

- **Spark 4.1.3** with Scala 2.13 and **Java 17**
- **Two implementations**: RDD API (`WordCount`) and DataFrame API (`WordCountDataFrame`), sharing one word-cleaning rule (`WordCleaner`)
- **Command-line arguments** for input path and output directory
- **Run scripts** that pick the right JDK and Spark distribution
- **Word processing** with case normalisation and punctuation removal
- **Frequency-sorted results** (most common words first)
- **Strict build**: warnings are errors, scalafmt formatting, ScalaTest suites, CI on every pull request

## Prerequisites

- **OpenJDK 17** (Homebrew `openjdk@17`). Spark 4.x supports Java 17 or 21 only; the build refuses other JDKs with a clear message.
- **Maven 3.x** (Homebrew)
- **A pinned Apache Spark distribution** matching `spark.version` in `pom.xml`. The run scripts default to `/usr/local/spark-versions/spark-4.1.3` and honour `SPARK_HOME` if set. Homebrew's `apache-spark` tracks the latest release, which may be ahead of the version this project is built against, so it is not used.

Install the pinned distribution once:

```bash
curl -o /tmp/spark.tgz https://dlcdn.apache.org/spark/spark-4.1.3/spark-4.1.3-bin-hadoop3.tgz
sudo mkdir -p /usr/local/spark-versions
sudo tar xzf /tmp/spark.tgz -C /usr/local/spark-versions
sudo mv /usr/local/spark-versions/spark-4.1.3-bin-hadoop3 /usr/local/spark-versions/spark-4.1.3
```

Verify:

```bash
/usr/libexec/java_home -v 17
mvn --version
/usr/local/spark-versions/spark-4.1.3/bin/spark-submit --version
```

When bumping Spark, change `spark.version` and `scala.version` in `pom.xml` (use the Scala version shipped in the distribution's `jars/` folder) and the `SPARK_HOME` default in both run scripts together.

## Project Structure

```
.
├── README.md
├── pom.xml                                    # Maven build (strict compiler, scalafmt, ScalaTest)
├── .scalafmt.conf                             # Formatting rules
├── .pre-commit-config.yaml                    # Gitleaks + spotless:check before each commit
├── .github/workflows/ci.yml                   # CI: mvn verify, Gitleaks, dependency review
├── .github/dependabot.yml                     # Weekly dependency and action updates
├── src/main/scala/com/morillo/spark/
│   ├── WordCleaner.scala                      # Shared word normalisation
│   ├── WordCount.scala                        # RDD API implementation
│   └── WordCountDataFrame.scala               # DataFrame API implementation
├── src/test/scala/com/morillo/spark/          # ScalaTest suites
├── run-wordcount.sh                           # RDD API execution script
├── run-wordcount-dataframe.sh                 # DataFrame API execution script
├── westeros.txt                               # Sample Game of Thrones data
└── target/                                    # Build outputs (generated)
```

## Building the Project

Maven must run on JDK 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean package
```

This creates `target/spark-wordcount-1.0.0.jar`. The jar contains only this project's classes; Spark and Scala are `provided` and come from the Spark distribution at run time, so the jar is a few kilobytes.

**Note**: the run scripts do not build the project. Run the Maven build first.

## Running the Application

Both scripts set `JAVA_HOME` to JDK 17 and `SPARK_HOME` to the pinned distribution, check that the jar exists, and remove an existing output directory before submitting (Spark requires a non-existent output path).

### Option 1: RDD API Implementation

```bash
./run-wordcount.sh westeros.txt sevenkingdoms
./run-wordcount.sh /path/to/input.txt /path/to/output_directory
```

### Option 2: DataFrame API Implementation

```bash
./run-wordcount-dataframe.sh westeros.txt sevenkingdoms-dataframe
./run-wordcount-dataframe.sh /path/to/input.txt /path/to/output_directory
```

### Option 3: Direct spark-submit

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export SPARK_HOME=/usr/local/spark-versions/spark-4.1.3

# RDD API
"$SPARK_HOME/bin/spark-submit" \
  --class com.morillo.spark.WordCount \
  --master "local[*]" \
  --deploy-mode client \
  --driver-memory 2g \
  --executor-memory 1g \
  target/spark-wordcount-1.0.0.jar \
  westeros.txt \
  sevenkingdoms

# DataFrame API
"$SPARK_HOME/bin/spark-submit" \
  --class com.morillo.spark.WordCountDataFrame \
  --master "local[*]" \
  --deploy-mode client \
  --driver-memory 2g \
  --executor-memory 1g \
  target/spark-wordcount-1.0.0.jar \
  westeros.txt \
  sevenkingdoms-dataframe
```

### From IntelliJ IDEA

The shared run configurations `WordCount (RDD API)` and `WordCountDataFrame (DataFrame API)` run the apps on the Spark libraries Maven downloads. They enable "Add dependencies with 'provided' scope to classpath" and pass the `--add-opens` JVM flags Spark 4 needs on JDK 17. They write to `sevenkingdoms-ide/` and `sevenkingdoms-dataframe-ide/`.

## Sample Output

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

The RDD version sorts by count only, so words with equal counts can appear in any order. The DataFrame version sorts by count, then alphabetically. The set of lines is identical.

```bash
cat sevenkingdoms/part-*
```

## Testing

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Run all tests
mvn test

# Run a specific suite
mvn test -Dsuites=com.morillo.spark.WordCountTest
```

`WordCleanerTest` covers the normalisation rule. `WordCountTest` starts a local SparkSession and checks that both implementations return the same counts, the expected ordering, and the same `(word,count)` output lines.

## Code Quality and CI

The build is strict, and CI enforces it on every pull request and push to `master`.

- **Compiler**: scalac runs with `-Xlint -Wdead-code -Wvalue-discard -Werror`, so any warning fails the build.
- **Formatting**: [scalafmt](https://scalameta.org/scalafmt/) through the Spotless Maven plugin; rules live in `.scalafmt.conf`.

  ```bash
  mvn spotless:apply   # format the code
  mvn spotless:check   # fail on unformatted code (also part of `mvn verify`)
  ```

- **Secrets**: [Gitleaks](https://github.com/gitleaks/gitleaks) scans the full history in CI and the staged changes in the pre-commit hook.
- **Dependencies**: Dependabot opens weekly update PRs for Maven dependencies and GitHub Actions (`.github/dependabot.yml`). Spark and Scala minor/major bumps are excluded on purpose because they must move together with the pinned Spark distribution. On pull requests, `dependency-review-action` flags newly added dependencies with known CVEs.
- **CI** (`.github/workflows/ci.yml`): `mvn -B -ntp verify` on JDK 17 (compile, tests, format check), plus the Gitleaks and dependency-review jobs. Actions are pinned to commit SHAs and the workflow token is read-only.

Run the same full check locally before pushing:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn verify
```

### Pre-commit hooks

```bash
uv tool install pre-commit   # once per machine
pre-commit install           # once per clone
pre-commit run --all-files   # run every hook by hand
```

The hooks run Gitleaks and `mvn spotless:check` before each commit. Maven needs JDK 17; the hook finds it with `/usr/libexec/java_home -v 17` on macOS and falls back to `JAVA_HOME` elsewhere.

## Application Logic

Both implementations validate that exactly two arguments are given, create a SparkSession, and stop it in a `finally` block. The word rule lives in `WordCleaner`: split on whitespace, lowercase, strip every character that is not a letter or digit, drop empty results.

### RDD API (`WordCount.countWords`)

`flatMap` on whitespace, `map` through `WordCleaner.clean`, `filter` empties, `reduceByKey` to count, `sortBy` count descending, `saveAsTextFile`.

### DataFrame API (`WordCountDataFrame.countWords`)

`explode(split(...))`, `regexp_replace(lower(...))`, `filter`, `groupBy().agg(count())`, `orderBy(desc("count"), asc("clean_word"))`. `formatForOutput` turns the rows into the same `(word,count)` text the RDD version writes, and `main` also prints the top 20 words and the number of unique words.

## RDD vs DataFrame API Comparison

| Feature | RDD API | DataFrame API |
|---------|---------|---------------|
| **Performance** | Good for simple operations | Better for complex operations with Catalyst optimizer |
| **Code Style** | Functional programming style | SQL-like operations |
| **Type Safety** | Compile-time type safety | Runtime schema validation |
| **Optimization** | Manual optimization needed | Automatic query optimization |
| **Learning Curve** | Steeper for beginners | Easier for SQL users |
| **Best For** | Complex transformations, legacy code | Analytics, SQL users, performance-critical apps |

## Troubleshooting

### Wrong JDK

The build fails early with `Spark 4.x supports Java 17 or 21 only` when Maven runs on another JDK:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### spark-submit not found

The run scripts print `spark-submit not found at ...`. Install the pinned distribution (see Prerequisites) or point `SPARK_HOME` at an existing Spark 4.1.x install.

### Output Directory Exists

The scripts remove an existing output directory before submitting. With direct `spark-submit`, delete it yourself first.

## License

This project demonstrates Apache Spark capabilities using Game of Thrones character data for educational purposes.

## Contributing

Open a pull request; CI must pass (build, tests, format check, secret scan).

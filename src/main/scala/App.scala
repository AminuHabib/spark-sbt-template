import org.apache.spark.sql.SparkSession

object LogAnalyzer {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Log Analyzer")
      .config("spark.master", "local")
      .getOrCreate()

    val result = DstiJob.run(spark)
    spark.stop

    println(s"result of job is : $result")

  }
}
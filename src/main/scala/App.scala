import org.apache.spark.sql.SparkSession

object App {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("AccessLog Analyzer")
      .config("spark.master", "local")
      .getOrCreate()

    val result = AccessJob.run(spark)
    spark.stop

    println(s"The result of the job is : $result")

  }
}
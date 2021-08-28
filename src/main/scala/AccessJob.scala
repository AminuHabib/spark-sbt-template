import org.apache.spark.sql.{ Dataset, SparkSession, Encoders }
import org.apache.spark.sql.functions.{ to_timestamp, regexp_extract, col }
import scala.util.matching.Regex

case class AccessLog(
  ip: String,
  ident: String,
  user: String,
  datetime: String,
  request: String,
  status: String,
  size: String,
  referer: String,
  userAgent: String,
  unk: String)

object AccessJob {

  def run(spark: SparkSession): String = {
    val path = "src/data/access.log.gz"
    val outputPath = "results"

    createReport(spark: SparkSession, path: String, outputPath: String)
    return "successful"
  }

  def toAccessLog(params: List[String]) = AccessLog(
    params(0),
    params(1),
    params(2),
    params(3),
    params(4),
    params(5),
    params(6),
    params(7),
    params(8),
    params(9))

  def createReport(
    spark: SparkSession,
    gzPath: String,
    outputPath: String): Unit = {
    val logs = spark.read.text(gzPath)
    val RegEx =
      """^(?<ip>[0-9.]+) (?<identd>[^ ]) (?<user>[^ ]) \[(?<datetime>[^\]]+)\] \"(?<request>[^\"]*)\" (?<status>[^ ]*) (?<size>[^ ]*) \"(?<referer>[^\"]*)\" \"(?<useragent>[^\"]*)\" \"(?<unk>[^\"]*)\"""".r

    val logString = logs.map(_.getString(0))(Encoders.STRING)

    val dsParsed = logString.flatMap(x => RegEx.unapplySeq(x))(
      Encoders.product[List[String]])

    val ds = dsParsed.map(toAccessLog _)(Encoders.product[AccessLog])
    val dsWithTime = ds.withColumn(
      "datetime",
      to_timestamp(ds("datetime"), "dd/MMM/yyyy:HH:mm:ss X"))

    //dsWithTime.printSchema
    dsWithTime.cache
    dsWithTime.createOrReplaceTempView("AccessLogTable")

    //  "==== (BEGIN) Compute the list of number of access per IP address for each IP address ==="
    spark
      .sql(
        "select cast(datetime as date) as date, map(ip, count(*)) as IpAddressCount from AccessLogTable where cast(datetime as date) in (select date from (select count(*) as count, cast(datetime as date) as date from AccessLogTable group by date having count(*) > 20000 )) group by date, ip")
      .coalesce(1)
      .write
      .mode("Overwrite")
      .json(s"$outputPath/AccessCountByIP")

    // "==== (BEGIN) Compute the list of number of access by URI for each URI ==="
    spark
      .sql(
        "select cast(datetime as date) as date, map(split(request,\" \")[1], count(*)) as uriCount from AccessLogTable where cast(datetime as date) in (select date from (select count(*) as count, cast(datetime as date) as date from AccessLogTable group by date having count(*) > 20000 )) group by date, split(request,\" \")[1]")
      .coalesce(1)
      .write
      .mode("Overwrite")
      .json(s"$outputPath/AccessCountByUri")
  }
}
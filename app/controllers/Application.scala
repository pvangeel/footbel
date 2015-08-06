package controllers

import java.io.{File, InputStream}
import java.util.zip.ZipFile

import models.{Match, Ranking}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json
import play.api.mvc._
import util.WSUtil

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  private def zippedFileToCsvLines = {
    zippedFile: File =>
      val zipFile: ZipFile = new ZipFile(zippedFile)
      zipFile.entries.find(_.getName.toLowerCase.endsWith(".csv")).map { entry =>
        val stream: InputStream = zipFile.getInputStream(entry)
        val filereader = Source.fromInputStream(stream, "iso-8859-1")
        filereader.getLines
      } getOrElse Iterator.empty
  }

  def importRankings = Action.async {
    val begin = System.currentTimeMillis
    val url = "http://static.belgianfootball.be/project/publiek/download/natcltdownP.zip"

    WSUtil.urlToTempFile(url).map(zippedFileToCsvLines).map {
      lines =>
        val rankings = lines.drop(1).map(_.split(";")).map {
          line =>
            Ranking(
              div = line(0),
              pos = line(1).toLong,
              team = line(2),
              matches = line(3).toLong,
              wins = line(4).toLong,
              losses = line(5).toLong,
              draws = line(6).toLong,
              goalsPlus = line(7).toLong,
              goalsMin = line(8).toLong,
              points = line(9).toLong,
              per = line(10).toLong
            )
        }
        Ranking.createBatch(rankings.toSeq)
        Ok(Json.toJson(System.currentTimeMillis - begin))
    }
  }

  def importMatches = Action.async {
    val begin = System.currentTimeMillis
    val url = "http://static.belgianfootball.be/project/publiek/download/natresdownP.zip"
    WSUtil.urlToTempFile(url).map(zippedFileToCsvLines).map {
      lines =>
        val matches = lines.drop(1).map(_.split(";")).map {
          line =>
            def safeStringToLong(str: String): Option[Long] = {
              import scala.util.control.Exception._
              catching(classOf[NumberFormatException]) opt str.toInt
            }
            Match(
              div = line(0),
              dateTime = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").parseDateTime(s"${line(1)} ${line(2)}"),
              home = line(3),
              away = line(4),
              rh = safeStringToLong(line(5)),
              ra = safeStringToLong(line(6)),
              status = line(7),
              md = line(8).toLong,
              regnumberhome = line(9).toLong,
              regnumberaway = line(10).toLong
          )
        }
        Match.createBatch(matches.toSeq)

        Ok(Json.toJson(System.currentTimeMillis - begin))
    }
  }


  def rankings = Action {
    implicit request =>
      Ok(Json.toJson(Ranking.findAll()))
  }

  def matches(div: String = "1") = Action {
    implicit request =>
      import models.Match._
      Ok(Json.toJson(Match.find(div = div)))
  }

}

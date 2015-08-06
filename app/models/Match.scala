package models

import anorm._
import anorm.SqlParser._
import org.joda.time.DateTime
import play.api.db.DB
import play.api.libs.json.Json
import play.api.Play.current
import anorm.JodaParameterMetaData._

case class Match(
                  div: String,
                  dateTime: DateTime,
                  home: String,
                  away: String,
                  rh: Option[Long],
                  ra: Option[Long],
                  status: String,
                  md: Long,
                  regnumberhome: Long,
                  regnumberaway: Long,
                  id: Option[Long] = None
                  )

object Match {

  implicit val matchReads = Json.reads[Match]
  implicit val matchWrites = Json.writes[Match]

  val parser = str("div") ~
      get[DateTime]("dateTime") ~
      str("home") ~
      str("away") ~
      get[Option[Long]]("rh") ~
      get[Option[Long]]("ra") ~
      str("status") ~
      long("md") ~
      long("regnumberhome") ~
      long("regnumberaway") ~
      get[Option[Long]]("id") map {
    case div ~ dateTime ~ home ~ away ~ rh ~ ra ~ status ~ md ~ regnumberhome ~ regnumberaway ~ id =>
      Match(div, dateTime, home, away, rh, ra, status, md, regnumberhome, regnumberaway, id)
  }


  def createBatch(matches: Seq[Match]) = DB.withConnection {
    implicit connection =>
      SQL("delete from matches").execute
      val begin = System.currentTimeMillis
      val parameters: Seq[Seq[NamedParameter]] = matches.map {
        mtch =>
          Seq(
            NamedParameter("div", mtch.div),
            NamedParameter("dateTime", mtch.dateTime),
            NamedParameter("home", mtch.home),
            NamedParameter("away", mtch.away),
            NamedParameter("rh", mtch.rh),
            NamedParameter("ra", mtch.ra),
            NamedParameter("status", mtch.status),
            NamedParameter("md", mtch.md),
            NamedParameter("regnumberhome", mtch.regnumberhome),
            NamedParameter("regnumberaway", mtch.regnumberaway)
          )
      }

      val count: Array[Int] = BatchSql("insert into matches(div, dateTime, home, away, rh, ra, status, md, regnumberhome, regnumberaway) values ({div}, {dateTime}, {home}, {away}, {rh}, {ra}, {status}, {md}, {regnumberhome}, {regnumberaway})", parameters).execute
      println(s"done importing matches. Took: ${System.currentTimeMillis - begin}ms")
  }

  def findAll() = DB.withConnection {
    implicit connection =>
      SQL("select * from matches").as(parser *)
  }

  def find(div: String) = DB.withConnection {
    implicit connection =>
      SQL("select * from matches where div = {div}")
        .on('div -> div)
        .as(parser *)
  }

}



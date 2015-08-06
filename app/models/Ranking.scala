package models

import com.sun.org.apache.xpath.internal.operations.Div
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.libs.json.Json

case class Ranking(div: String,
                   pos: Long,
                   team: String,
                   matches: Long,
                   wins: Long,
                   losses: Long,
                   draws: Long,
                   goalsPlus: Long,
                   goalsMin: Long,
                   points: Long,
                   per: Long,
                   id: Option[Long] = None)


object Ranking {

  val parser = str("div") ~
    long("pos") ~
    str("team") ~
    long("matches") ~
    long("wins") ~
    long("losses") ~
    long("draws") ~
    long("goalsPlus") ~
    long("goalsMin") ~
    long("points") ~
    long("per") ~
    get[Option[Long]]("id") map {
    case div ~ pos ~ team ~ matches ~ wins ~ losses ~ draws ~ goalsPlus ~ goalsMin ~ points ~ per ~ id =>
      Ranking(
        div,
        pos,
        team,
        matches,
        wins,
        losses,
        draws,
        goalsPlus,
        goalsMin,
        points,
        per,
        id)
  }

  implicit val rankingReads = Json.reads[Ranking]
  implicit val rankingWrites = Json.writes[Ranking]

  def createBatch(rankings: Seq[Ranking]) = DB.withConnection {
    implicit connection =>
      //      SQL("insert into rankings(div, pos, team, matches, wins, losses, draws, goalsPlus, goalsMin, points, per) values")
      SQL("delete from rankings").execute
      val parameters: Seq[Seq[NamedParameter]] = rankings.map {
        ranking =>
          Seq(
            NamedParameter("div", ranking.div),
            NamedParameter("pos", ranking.pos),
            NamedParameter("team", ranking.team),
            NamedParameter("matches", ranking.matches),
            NamedParameter("wins", ranking.wins),
            NamedParameter("losses", ranking.losses),
            NamedParameter("draws", ranking.draws),
            NamedParameter("goalsPlus", ranking.goalsPlus),
            NamedParameter("goalsMin", ranking.goalsMin),
            NamedParameter("points", ranking.points),
            NamedParameter("per", ranking.per)
          )
      }

      val count: Array[Int] = BatchSql("insert into rankings(div, pos, team, matches, wins, losses, draws, goalsPlus, goalsMin, points, per) values ({div}, {pos}, {team}, {matches}, {wins}, {losses}, {draws}, {goalsPlus}, {goalsMin}, {points}, {per})", parameters).execute
  }

  def findAll() = DB.withConnection {
    implicit connection =>
      SQL("select * from rankings").as(parser *)
  }

  def find(div: String) = DB.withConnection {
    implicit connection =>
      SQL("select * from rankings where div = {div}")
        .on('div -> div)
        .as(parser *)
  }


}















package models

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

  val parser = str("team") map { case team => team }

  implicit val rankingWrites = Json.reads[Ranking]

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


}

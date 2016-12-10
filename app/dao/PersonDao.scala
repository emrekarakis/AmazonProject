package dao

import java.sql.Connection
import anorm._
import play.api.Logger
import play.api.Play.current
import play.api.db.DB

/**
 * Created by emrekarakis on 26/01/16.
 */

object PersonDao {

  val logger: Logger = Logger("PersonDaoLogger")


  def insert(name: String )( implicit connection: Connection): Option[Long] = {
    val id: Option[Long] = SQL("insert into Person(name) values ({name})")
      .on( 'name -> name ).executeInsert()
    id
  }

  def update(status: Int, id: Long): Unit = {
     DB.withConnection { implicit c: Connection =>
       SQL("update Person set status={status} where id={id}")
         .on('status -> status, 'id -> id)
         .executeUpdate()
     }
  }
}

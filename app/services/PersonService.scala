package services

import dao.PersonDao
import play.api.Logger

import scala.util.{Failure, Success, Try}

/**
 * Created by emrekarakis on 24/02/16.
 */
object PersonService {
  val logger = Logger("PersonServiceLogger")

  def update(status: Int, id: Long): Try[Unit]= { //try döndüreyim
   try{
     PersonDao.update(status, id)
     Success(Unit)
   } catch {
     case e: Exception => {
       logger.error("Update operation could not be performed properly",e)
       Failure(e)
     }
   }
  }
}

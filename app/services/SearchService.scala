package services

import play.api.Play.current
import java.sql.Connection
import controllers.Application._
import dao.PersonDao
import play.api.Logger
import play.api.db.DB
import play.api.libs.json.{Json, JsObject}
import play.api.mvc.Result
import scala.util.{Try}

/**
 * Created by emrekarakis on 18/02/16.
 */

object SearchService {

  val logger: Logger = Logger("SearchServiceLogger")

  def insertPersonAndStartSearch(name: String): Result = {
    try {

      DB.withTransaction { implicit c: Connection =>
        val idAsOpt: Option[Long] = PersonDao.insert(name)

        idAsOpt match {
          case Some(id: Long) => {  //transactional oldugu için exception fırlattık ve sqse mesaj gönderme işleminde hata oldugu zaman geri alındı
            val jsonValue: JsObject = Json.obj("id" -> id, "name" -> name)
            val resultAsTry: Try[Unit] = SqsService.sendMessageToQueue(ConfigurationService.queueUrl,jsonValue.toString)
            resultAsTry.get        /* if (resultAsTry.isFailure) {throw resultAsTry.failed.get }*/
                                   //get normalde başarılı durumda içindeki tip ne ise onu döndürür fakat failure
                                   // durumda exception fırlatıyor trancactional oldugu için failure durumda exception atmamız gerekli
            logger.info("Search request was received successfully")
            Ok(views.html.index("Search request was received successfully"))
          }
          case None => {
            logger.error("Id could not be extracted from Database successfully")
            Ok(views.html.index("Search request could not be received successfully"))
          }
        }
      }
    } catch {
      case e: Exception => {
        logger.error("An error occurred while saving the data to database and Amazon SQS as transactional:", e)
        Ok(views.html.index("A problem occurred while transmitting the search request"))
      }
    }
  }
}

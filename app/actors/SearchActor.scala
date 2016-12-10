package actors

import akka.actor._
import context.ExecutionContexts
import models.Models.{SearchResult, SearchMessage}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import services.{PersonService, GoogleService}
import scala.concurrent.Future
import scala.util.{Try}

/**
 * Created by emrekarakis on 09/02/16.
 */

class SearchActor extends Actor with ActorLogging {

  val logger = Logger("SearchActorLogger")


  def receive = {

    case searchMessage: SearchMessage => {
      try {
        val json: JsValue = Json.parse(searchMessage.messageBody)
        val nameAsOpt: Option[String] = (json \ "name").asOpt[String]
        val idAsOpt: Option[Long] = (json \ "id").asOpt[Long]
        (nameAsOpt, idAsOpt) match {
          case (Some(name: String), Some(id: Long)) => {
            val searchResultFuture: Future[JsValue] = GoogleService.search(name)

            searchResultFuture.map { (value: JsValue) =>
              logger.info("Google Search Process is successful")
              val tryOfResult: Try[Unit] = PersonService.update(1, id)      //status is updated as 1 after doing search!!!!
              tryOfResult.map { (result: Unit) =>
                logger.info("Parameters(id and status) was successfully updated in the database")
                val recordActor: ActorRef = context.actorOf(Props( new RecordActor))
                val recordData: SearchResult = SearchResult(id, name, value, searchMessage.receiptHandle)
                recordActor ! recordData  //updated data are sent to Amazon S3
                context.stop(self)               //the search actor commits a suicide in success condition of sending messages to Amazon S3
              }
            }(ExecutionContexts.genericOps)

          }
          case _ => {
            logger.error("An error occurred while parsing Json Object.Json Object does not contain subentities called id or name")
          }
        }
      } catch {
        case e: Exception => {
          logger.error("An error occurred while doing search for name",e)
        }
      }
    }
    case _ => {
      logger.info("An unknown message was received by Search Actor")
      context.stop(self) //the search actor commits a suicide when an unknown message comes to this actor
    }
  }
}



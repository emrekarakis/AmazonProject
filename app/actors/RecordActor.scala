package actors

import java.io.ByteArrayInputStream
import akka.actor.{PoisonPill, Actor}
import context.ExecutionContexts
import models.Models.SearchResult
import play.api.Logger
import play.api.libs.json.JsValue
import services.{SqsService, ConfigurationService, S3Service}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Created by emrekarakis on 10/02/16.
 */

class RecordActor extends Actor {

  val logger = Logger("RecordActorLogger")

  def receive = {
    case recordMessage: SearchResult => {
      try {
        val id: Long = recordMessage.id
        val name: String = recordMessage.name
        val searchResult: JsValue = recordMessage.resultJson
        val inputStream: ByteArrayInputStream = new ByteArrayInputStream(searchResult.toString().getBytes())
        val result: Try[Unit] = S3Service.storeDataToAmazonS3(id + "-" + name, inputStream)
        result match {
          case Success(v: Unit) => {
            logger.info("Messages are saved to Amazon S3 ")
            val deleteResult: Future[Unit] = SqsService.deleteMessageFromQueue(ConfigurationService.queueUrl,recordMessage.receiptHandle)

            deleteResult.onComplete {
              case Success(v) => {
                logger.info("Message was successfully deleted from SQS")
                self ! PoisonPill
              }
              case Failure(e: Throwable) => {
                logger.error("An error occcurred while deleting a message from SQS. Message=>", e)
                self ! PoisonPill
              }
            }(ExecutionContexts.genericOps)
          }
          case Failure(e) => {
            logger.error("An error occurred while storing data to AmazonS3.Message could not be saved to Amazon S3", e)
            self ! PoisonPill
          }
        }
      } catch {
         case e: Exception => {
           logger.error("An error occurred while getting some parameters like (id,name,searchResult)", e)
        }
      }
    }
    case _ => {
      logger.info("An unknown message was received by Record Actor")
    }
  }
}


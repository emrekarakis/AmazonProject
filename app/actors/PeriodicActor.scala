package actors

import akka.actor._
import com.amazonaws.services.sqs.model.Message
import context.ExecutionContexts
import models.Models.{RetrieveMessages, SearchMessage}
import play.api.Logger
import services.{ConfigurationService, SqsService}
import scala.concurrent.Future

/**
 * Created by emrekarakis on 03/02/16.
 */
class PeriodicActor extends Actor {

  val logger = Logger("PeriodicActorLogger")


  def receive = {

    case RetrieveMessages => {
      try {
        val futureOfListOfMessages: Future[List[Message]] = SqsService.receiveMessageFromQueue(ConfigurationService.queueUrl)
        futureOfListOfMessages.map { listOfMessage: List[Message] =>   //farklı bir try catch bloguna alınmalıydı recover olmasaydı
          for(message: Message <- listOfMessage) {
            val searchActor: ActorRef = context.actorOf(Props(new SearchActor))
            val searchMessage: SearchMessage = SearchMessage(message.getBody, message.getReceiptHandle)
            searchActor ! searchMessage
          }
           self ! RetrieveMessages
        }(ExecutionContexts.genericOps).recover {
          case e: Exception => {
            logger.error("An error occurred while processing search operation!!",e)
            self ! RetrieveMessages
          }
        }(ExecutionContexts.genericOps)
      } catch {
        case e: Exception => {
          logger.error("An error occurred while receiving message from Amazon SQS",e)
          self ! RetrieveMessages
        }
      }
    }
    case _ => {
      logger.error("An unknown message was received by Periodic Actor")
    }
  }
}

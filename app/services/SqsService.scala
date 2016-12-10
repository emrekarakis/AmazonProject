package services

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.amazonaws.services.sqs.model._
import play.api.Logger
import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
 * Created by emrekarakis on 26/01/16.
 */

object SqsService {
  val logger: Logger = Logger("SqsServiceLogger")
  lazy val sqs: AmazonSQSAsyncClient = initialize()


  def initialize(): AmazonSQSAsyncClient = {
    try {
      val credentials: AWSCredentials = new ProfileCredentialsProvider().getCredentials()
      val sqs: AmazonSQSAsyncClient = new AmazonSQSAsyncClient(credentials)
      sqs.setRegion(Region.getRegion(Regions.fromName(ConfigurationService.region)))
      logger.info("Getting credentials and setting regions are completed successfully")
      sqs
    } catch {
      case e: Exception => {
        logger.error("An error occcurred while getting credentials and setting region")
        throw e
      }
    }
  }

  def getQueueUrl(queueName: String): String = {
    try {
      val queueUrlRequest: GetQueueUrlRequest = new GetQueueUrlRequest(queueName)
      val queueUrlResult: GetQueueUrlResult = sqs.getQueueUrl(queueUrlRequest)
      val queueUrl: String = queueUrlResult.getQueueUrl
      logger.info("QueueUrl was taken successfully")
      queueUrl
    } catch {
      case e: Exception => {
        logger.error("An error occurred while getting queueUrl", e)
        ""
      }
    }
  }

  def sendMessageToQueue(queueUrl: String, message: String): Try[Unit] = {
    try {
      logger.debug(s"Queue url=> ${queueUrl}, Message=> ${message}")
      val result: SendMessageResult = sqs.sendMessage(ConfigurationService.queueUrl, message)
      logger.info("Message sent to SQS successfully")
      Success(Unit)
    } catch {
      case e: Exception => {
        Failure(e)
      }
    }
  }

  def receiveMessageFromQueue(queueUrl: String): Future[List[Message]] = {
    val promise: Promise[List[Message]] = Promise[List[Message]]()
    val receiveMessageRequest: ReceiveMessageRequest = new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(10)
    sqs.receiveMessageAsync(receiveMessageRequest, new AsyncHandler[ReceiveMessageRequest, ReceiveMessageResult] {
      override def onError(e: Exception): Unit = {
        promise.failure(e)
      }
      override def onSuccess(request: ReceiveMessageRequest, result: ReceiveMessageResult): Unit = {
        promise.success(result.getMessages.asScala.toList)
      }
    })
    promise.future
  }

  def deleteMessageFromQueue(queueUrl: String, receiptHandle: String): Future[Unit] = {
    val promise: Promise[Unit] = Promise[Unit]()
    val deleteMessageRequest: DeleteMessageRequest = new DeleteMessageRequest(queueUrl, receiptHandle)
    sqs.deleteMessageAsync(deleteMessageRequest, new AsyncHandler[DeleteMessageRequest, Void] {
      override def onError(e: Exception): Unit = {
        promise.failure(e)
      }
      override def onSuccess(request: DeleteMessageRequest, result: Void): Unit = {
        promise.success(Unit)
      }
    })
    promise.future
  }
}
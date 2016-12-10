package global
import scala.concurrent.duration._
import actors.PeriodicActor
import akka.actor._
import context.ExecutionContexts
import models.Models.{RetrieveMessages}
import play.api.mvc.{RequestHeader, Result, _}
import play.api.{Application, GlobalSettings, Logger}
import scala.concurrent.Future


/**
 * Created by emrekarakis on 16/02/16.
 */

object Global extends GlobalSettings {

  val logger: Logger = Logger("GlobalLogger")

  override def onStart(app: Application) = {

    logger.info("Application has started ")
    val system: ActorSystem = ActorSystem("AmazonProject")
    val periodicActor: ActorRef = system.actorOf(Props(new PeriodicActor), name = "retriever")

    //periodicActor ! RetrieveMessages
    system.scheduler.scheduleOnce(1000 milliseconds, periodicActor, RetrieveMessages)(ExecutionContexts.criticalOps)
    //system.scheduler.schedule(1000 milliseconds, 1000 milliseconds, periodicActor, RetrieveMessages)(ExecutionContexts.criticalOps)
     //her bir saniyede bir periodic actore mesaj atıyorsun her bir saniyede bir mesaj atmak yerine işi bitince atsın
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {

    logger.error("Unhandled exception occurred =>", ex)
    Future.successful(Results.Ok("Unknown error occurred"))

  }
}



package controllers

import models.Models
import play.api.Logger
import Models.{ UserData}
import play.api.mvc._
import services.{SearchService}


object Application extends Controller {

  val logger: Logger = Logger("ApplicationLogger")


  def index = Action { request=>
    Ok(views.html.index(""))
  }

  def search = Action { request =>
    val messageAsOpt: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded

    messageAsOpt match {
      case Some(map: Map[String, Seq[String]]) => {
        val firstNameAsOpt: Option[Seq[String]] = map.get("firstName")
        val lastNameAsOpt: Option[Seq[String]] = map.get("lastName")

        (firstNameAsOpt, lastNameAsOpt) match {

          case (Some(sequenceOfFirstName: Seq[String]), Some(sequenceOfLastName: Seq[String]))
            if (sequenceOfFirstName.nonEmpty && sequenceOfFirstName.head.nonEmpty && sequenceOfLastName.nonEmpty && sequenceOfLastName.head.nonEmpty) => {
            val userData: UserData = UserData(sequenceOfFirstName.head,sequenceOfLastName.head)
            SearchService.insertPersonAndStartSearch(userData.firstName + " " + userData.lastName)
          }
          case _ => {
            logger.error("FirstName and LastName fields can not be left empty")
            Ok(views.html.index("FirstName and LastName fields can not be left empty"))
          }
        }
      }
      case None => {
        logger.error("You've entered invalid parameter.")
        Ok(views.html.index("You've entered invalid parameter."))
      }
    }
  }
}

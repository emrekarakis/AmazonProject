package services
import java.net.URLEncoder
import context.ExecutionContexts
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.{JsValue}
import play.api.libs.ws.{WS, WSResponse}
import scala.concurrent.Future

/**
 * Created by emrekarakis on 11/01/16.
 */

object GoogleService {

   val logger: Logger = Logger("GoogleServiceLogger")

  lazy val key: String = URLEncoder.encode(ConfigurationService.apiKey, "UTF-8")
  lazy val customSearchID: String = URLEncoder.encode(ConfigurationService.customSearchEngineID, "UTF-8")
  lazy val alt: String = URLEncoder.encode(ConfigurationService.altValue, "UTF-8")

  def search(nameToSearch: String): Future[JsValue] = {

    val searchResult: Future[JsValue] =
      try {
        val query: String = URLEncoder.encode(nameToSearch, "UTF-8")
        val organizedSearchUrl: String = s"""${ConfigurationService.urlCustomSearch}key=${key}&cx=${customSearchID}&q=${query}&alt=${alt}"""
        val searchResponseFromGoogleApi: Future[WSResponse] = WS.url(organizedSearchUrl).withFollowRedirects(true).get

        val futureResult: Future[JsValue] = searchResponseFromGoogleApi.map { (result: WSResponse) =>
          val jsonValue: JsValue = result.json
          val resultMode: Int = result.status / 100
          val returnValue: JsValue = resultMode match {
            case 2 => {
              logger.info("JsonObject was transmitted by Google successfully")
              jsonValue
            }
            case _ => {
              val errorMessagesAsOpt: Option[JsValue] = (jsonValue \ "error").asOpt[JsValue]
              errorMessagesAsOpt match {
                case Some(json: JsValue) => {
                  val errorValue: Option[String] = (json \ "message").asOpt[String]
                  errorValue match {
                    case Some(errorMessage: String) => {
                      logger.error(s"An error has occured while getting json object(search result) from Google." +
                        s"Error message: ${errorMessage} Status Text: ${result.statusText} Status Code: ${result.status}")
                    }
                    case None => {
                      logger.error(s"An unknown error has occured while getting json object(search result)from Google." +
                        s"Status Text: ${result.statusText} Status Code: ${result.status}")
                    }
                  }

                }
                case None => {
                  logger.error(s"An error has occurred while searching from Google.Google'error message could not be found")
                }
              }
              throw new RuntimeException("Search operation could not be completed!!")
            }
          }
          returnValue
        }(ExecutionContexts.slowIoOps).recover {
          case e: Exception => {
            logger.error("An error occurred while search results are handled", e)
            throw e
          }
        }(ExecutionContexts.slowIoOps)
        futureResult
      } catch {
        case e: Exception => {
          logger.error("An exception caught while search operation is performed", e)
          Future.failed(e)
        }
      }
    searchResult
  }
}




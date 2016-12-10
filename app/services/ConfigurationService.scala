package services

import play.api.Play
import play.api.Play.current

/**
 * Created by emrekarakis on 02/02/16.
 */
object ConfigurationService {

  lazy val queueName: String = getString("queueName")
  lazy val bucketName: String = getString("bucketName")
  lazy val customSearchEngineID: String = getString("customSearchEngineId")
  lazy val apiKey: String = getString("apiKey")
  lazy val urlCustomSearch: String = getString("urlCustomSearch")
  lazy val altValue: String = getString("alt")
  lazy val region: String = getString("region")
  lazy val queueUrl: String = SqsService.getQueueUrl(queueName)



  def getString(key: String): String = {
    val readValueAsOpt: Option[String] = Play.application.configuration.getString(key)
    val readValueFromConfiguration: String = readValueAsOpt match {
      case Some(readValue: String) => {
        readValue
      }
      case None => {
        new RuntimeException(s" could not get ${key} from configuration file")
        ""
      }
    }
    readValueFromConfiguration
  }

  def getStringAsOpt(key: String): Option[String] = {
    Play.application.configuration.getString(key)
  }
}
package services

import java.io.InputStream
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}
import com.amazonaws.services.s3.model._
import play.api.Logger
import scala.util.{Try, Failure, Success}


/**
 * Created by emrekarakis on 26/01/16.
 */

object S3Service {

  val logger: Logger = Logger("S3ServiceLogger")
  lazy val s3Client: AmazonS3 = initialize()

  def initialize(): AmazonS3Client = {

    try {
      val s3Client: AmazonS3Client = new AmazonS3Client(new ProfileCredentialsProvider())
      logger.info("The process of getting credentials and creating s3Client object completed successfully")
      s3Client
    } catch {
      case e: Exception => {
        logger.error("An error occurred while getting credentials and creating s3Client object")
        throw e
      }
    }
  }


  def storeDataToAmazonS3(key: String, inputStream: InputStream): Try[Unit] = {

    try {
      val lenght: Int = inputStream.available()
      val objectMetadata: ObjectMetadata = new ObjectMetadata()
      objectMetadata.setContentLength(lenght)
      val request: PutObjectRequest = new PutObjectRequest(ConfigurationService.bucketName, key, inputStream, objectMetadata)
      s3Client.putObject(request)
      logger.info("Request was transmitted to Amazon S3 successfully")
      Success(Unit)

    } catch {
      case e: Exception =>{
        Failure(e)
      }
    }
  }
}


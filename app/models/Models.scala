package models

import play.api.libs.json.JsValue


/**
 * Created by emrekarakis on 03/02/16.
 */

object Models {

 case class UserData(firstName: String, lastName: String)
 case class RetrieveMessages()
 case class SearchMessage(messageBody: String, receiptHandle: String)
 case class SearchResult(id: Long, name: String, resultJson: JsValue, receiptHandle:String)

}

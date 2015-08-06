package util

import java.io.{FileOutputStream, File}

import play.api.libs.iteratee.Iteratee
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object WSUtil {

  def urlToTempFile(url: String) = WS.url(url).getStream().flatMap {
    case (headers, stream) =>

      val file: File = File.createTempFile("prefix", "tmp")
      val outputStream = new FileOutputStream(file)

      // The iteratee that writes to the output stream
      val iteratee: Iteratee[Array[Byte], Unit] = Iteratee.foreach[Array[Byte]] { bytes =>
        outputStream.write(bytes)
      }
      // Feed the body into the iteratee
      (stream |>>> iteratee).andThen {
        case result =>
          // Close the output stream whether there was an error or not
          outputStream.close()
          // Get the result or rethrow the error
          result.get
      }.map(_ => file)


  }

}

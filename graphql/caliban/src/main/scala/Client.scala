import com.github.plokhotnyuk.jsoniter_scala.core.*
import jdk.internal.net.http.ResponseSubscribers
import zio.*

import java.net.*
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.util.function.Function

trait Client {
  def get[A](url: URI)(using JsonValueCodec[A]): Task[A]
}

object Client {
  private final class Live(client: HttpClient) extends Client {

    private final class ResponseDecoder[A](using JsonValueCodec[A]) extends HttpResponse.BodyHandler[A] {
      override def apply(responseInfo: HttpResponse.ResponseInfo): HttpResponse.BodySubscriber[A] = {
        new ResponseSubscribers.ByteArraySubscriber[A](readFromArray[A](_))
      }
    }

    def get[A](uri: URI)(using JsonValueCodec[A]): Task[A] = {
      val req = HttpRequest.newBuilder(uri).GET().build()
      val handler = new ResponseDecoder[A]
      ZIO.attempt(client.send(req, handler).body())
    }
  }

  private val httpClient: TaskLayer[HttpClient] = ZLayer.scoped(
    ZIO.fromAutoCloseable(
      ZIO
        .executor.map { e =>
          HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 3000)))
            .executor(e.asJava)
            .build()
        }
    )
  )

  val live: TaskLayer[Client] = ZLayer.make[Client](httpClient, ZLayer.derive[Live])
}

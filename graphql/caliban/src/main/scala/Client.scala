import com.github.plokhotnyuk.jsoniter_scala.core.*
import zio.*

import java.net.*
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

trait Client {
  def get[A](url: URI)(using JsonValueCodec[A]): Task[A]
}

object Client {
  private final class Live(client: HttpClient) extends Client {

    def get[A](uri: URI)(using JsonValueCodec[A]): Task[A] = {
      val req     = HttpRequest.newBuilder(uri).GET().build()
      val handler = BodyHandlers.ofByteArray()
      ZIO.attempt(readFromArray(client.send(req, handler).body()))
    }
  }

  private val httpClient: TaskLayer[HttpClient] = ZLayer.scoped(
    ZIO.fromAutoCloseable(
      ZIO.executor.map { e =>
        HttpClient
          .newBuilder()
          .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 3000)))
          .executor(e.asJava)
          .build()
      }
    )
  )

  val live: TaskLayer[Client] = ZLayer.make[Client](httpClient, ZLayer.derive[Live])
}

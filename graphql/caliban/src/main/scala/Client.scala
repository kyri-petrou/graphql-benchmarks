import com.github.plokhotnyuk.jsoniter_scala.core.*
import io.helidon.http.HeaderNames
import io.helidon.webclient.api.*
import zio.*

import java.net.URI

trait Client {
  def get[A](url: URI)(using JsonValueCodec[A]): Task[A]
}

object Client {
  private final class Live(client: WebClient) extends Client {
    def get[A](uri: URI)(using JsonValueCodec[A]): Task[A] = {
      val req = client.get().path(uri.getPath).header(HeaderNames.HOST, uri.getHost)
      ZIO.attempt {
        val resp = req.request()
        readFromStream(resp.entity().inputStream())
      }
    }
  }

  private val httpClient: TaskLayer[WebClient] = ZLayer.scoped {
    val acq = ZIO.executor.map { e =>
      WebClient
        .builder()
        .baseUri(s"http://localhost:3000")
        .connectionCacheSize(200)
        .keepAlive(true)
        .executor(e.asExecutionContextExecutorService)
        .build()
    }
    ZIO.acquireRelease(acq)(c => ZIO.succeed(c.closeResource()))
  }

  val live: TaskLayer[Client] = ZLayer.make[Client](httpClient, ZLayer.derive[Live])
}

import com.github.plokhotnyuk.jsoniter_scala.core.*
import org.apache.hc.client5.http.async.methods.*
import org.apache.hc.client5.http.impl.async.{CloseableHttpAsyncClient, HttpAsyncClients}
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.core5.concurrent.FutureCallback
import org.apache.hc.core5.http.{HttpHost, NameValuePair}
import zio.*

import java.net.*

trait Client {
  def get[A](url: URI, params: Chunk[(String, String)] = Chunk.empty)(using JsonValueCodec[A]): Task[A]
}

object Client {
  private final class Live(client: CloseableHttpAsyncClient) extends Client {

    private final class RequestCallback[A](cb: ZIO[Any, Throwable, A] => Unit)(using JsonValueCodec[A], Trace)
        extends FutureCallback[SimpleHttpResponse] {
      def completed(result: SimpleHttpResponse): Unit = cb(ZIO.attempt(readFromArray[A](result.getBodyBytes)))
      def failed(ex: Exception): Unit                 = cb(ZIO.fail(ex))
      def cancelled(): Unit                           = cb(ZIO.fail(new InterruptedException("cancelled")))
    }

    def get[A](uri: URI, params: Chunk[(String, String)])(using JsonValueCodec[A]): Task[A] = {
      val req = createRequest(uri, params)
      ZIO.async[Any, Throwable, A] { cb =>
        client.execute(
          SimpleRequestProducer.create(req),
          SimpleResponseConsumer.create(),
          new RequestCallback(cb)
        )
      }
    }

    private def createRequest(uri: URI, params: Chunk[(String, String)]) = {
      val builder = SimpleRequestBuilder.get().setUri(uri)
      params.foreach(builder.addParameter(_, _))
      builder.build()
    }
  }

  private val httpClient: TaskLayer[CloseableHttpAsyncClient] = ZLayer.scoped(
    ZIO.fromAutoCloseable(
      ZIO
        .succeed {
          HttpAsyncClients
            .custom()
            .setProxy(new HttpHost("http", "127.0.0.1", 3000))
            .setConnectionManager({
              PoolingAsyncClientConnectionManagerBuilder
                .create()
                .setMaxConnTotal(2000)
                .setMaxConnPerRoute(200)
                .build()
            })
            .build()
        }
        .tap(c => ZIO.succeed(c.start()))
    )
  )

  val live: TaskLayer[Client] = ZLayer.make[Client](httpClient, ZLayer.derive[Live])
}

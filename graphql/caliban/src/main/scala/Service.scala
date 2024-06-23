import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import zio.query.*
import zio.{RLayer, Task, ZIO, ZLayer}

import java.net.URI

trait Service {
  val posts: Task[List[Post]]
  def user(id: Int): TaskQuery[User]
}

object Service {
  val layer: RLayer[Client, Service] = ZLayer.derive[Live]

  private class Live(client: Client) extends Service { self =>
    private val BaseUri  = URI.create("http://jsonplaceholder.typicode.com")
    private val PostsUri = BaseUri.resolve("/posts")
    private val UsersUri = BaseUri.resolve("/users")

    val posts: Task[List[Post]] = {
      client.get[List[Post]](PostsUri)
    }

    def user(id: Int): TaskQuery[User] =
      UsersDataSource.get(id)

    private object UsersDataSource {
      def get(id: Int): TaskQuery[User] = ZQuery.fromRequestUncached(Req(id))(usersDS)

      private case class Req(id: Int) extends Request[Throwable, User]

      private val usersDS = DataSource.fromFunctionZIO("UsersDataSource") { (req: Req) =>
        client.get[User](UsersUri.resolve(req.id.toString))
      }

      private given JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))
    }

    private given JsonValueCodec[List[Post]] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))

    private given JsonValueCodec[Service] with {
      def decodeValue(in: JsonReader, default: Service): Service = default
      def encodeValue(x: Service, out: JsonWriter): Unit         = ()
      inline def nullValue: Service                              = self
    }
  }
}

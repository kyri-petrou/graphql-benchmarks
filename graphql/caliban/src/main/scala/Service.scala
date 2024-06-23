import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import zio.query.*
import zio.{Chunk, Fiber, RLayer, Task, ZIO, ZLayer}

import java.net.URI
import java.util.concurrent.ConcurrentHashMap

trait Service {
  val posts: Task[List[Post]]
  def user(id: Int): TaskQuery[User]
}

object Service {
  val layer: RLayer[Client, Service] = ZLayer.derive[Live]

  private class Live(client: Client) extends Service {
    self =>

    private inline val BaseUri = "http://jsonplaceholder.typicode.com"
    private val PostsUri       = URI.create(BaseUri + "/posts")

    val posts: Task[List[Post]] =
      client
        .get[List[PostDao]](PostsUri)
        .map { bytes =>
          val posts = readFromArray[List[PostDao]](bytes)
          posts.map(dao => Post(dao.userId, dao.id, dao.title, dao.body, self.user(dao.userId)))
        }

    def user(id: Int): TaskQuery[User] =
      UsersDataSource.get(id)

    private object UsersDataSource {
      def get(id: Int): TaskQuery[User] = ZQuery.fromRequest(Req(id))(usersDS)

      private case class Req(id: Int) extends Request[Throwable, User]

      private val uris = new ConcurrentHashMap[Int, URI]()

      private val usersDS = DataSource.fromFunctionBatchedZIO("UsersDataSource") { (reqs: Chunk[Req]) =>
        ZIO
          .foreach(reqs) { req =>
            val uri = uris.computeIfAbsent(req.id, id => URI.create(BaseUri + "/users/" + id))
            client.get[User](uri).fork
          }
          .flatMap { Fiber.collectAll(_).await.unexit }
          .map(_.map(readFromArray[User](_)))

      }

      private given JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))
    }

    private case class PostDao(
        userId: Int,
        id: Int,
        title: String,
        body: String
    )

    private given JsonValueCodec[List[PostDao]] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))
  }
}

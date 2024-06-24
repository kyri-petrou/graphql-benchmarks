import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import zio.query.*
import zio.{Chunk, RIO, RLayer, Task, ZIO, ZLayer}

import java.net.URI
import scala.collection.mutable

trait Service {
  val posts: Task[List[Post]]

  def user(id: Int): TaskQuery[User]
}

object Service {
  val posts: RIO[Service, List[Post]] = ZIO.serviceWithZIO(_.posts)

  def user(id: Int): RQuery[Service, User] = ZQuery.serviceWithQuery(_.user(id))

  val layer: RLayer[Client, Service] = ZLayer.derive[Live]

  private class Live(client: Client) extends Service {
    private inline val BaseUrl = "http://jsonplaceholder.typicode.com"
    private val PostsUri       = URI.create(BaseUrl + "/posts")
    private val UsersUri       = URI.create(BaseUrl + "/users")

    val posts: Task[List[Post]] =
      client.get[List[Post]](PostsUri)

    def user(id: Int): TaskQuery[User] =
      UsersDataSource.get(id)

    private object UsersDataSource {
      def get(id: Int): TaskQuery[User] = usersDS.query(Req(id.toString))

      private case class Req(id: String) extends Request[Throwable, User] {
        def toQueryParam: (String, String) = ("id", id)
      }

      private val usersDS = DataSource.fromFunctionBatchedZIO("UsersDataSource") { (reqs: Chunk[Req]) =>
        ZIO.foreach(reqs) { req =>
          client.get[List[User]](UsersUri, Chunk.single(req.toQueryParam)).map(_.head)
        }
      }

      private given JsonValueCodec[List[User]] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))
    }

    private given JsonValueCodec[List[Post]] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))
  }
}

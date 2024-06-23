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

  private class Live(client: Client) extends Service {
    self =>

    private inline val BaseUri = "http://jsonplaceholder.typicode.com"
    private val PostsUri = URI.create(BaseUri + "/posts")

    val posts: Task[List[Post]] =
      client.get[List[Post]](PostsUri)

    def user(id: Int): TaskQuery[User] =
      UsersDataSource.get(id)

    private object UsersDataSource {
      def get(id: Int): TaskQuery[User] = ZQuery.fromRequest(Req(id))(usersDS)

      private case class Req(id: Int) extends Request[Throwable, User]

      private val usersDS = DataSource.fromFunctionZIO("UsersDataSource") { (req: Req) =>
        val uri = URI.create(BaseUri + "/users/" + req.id)
        client.get[User](uri)
      }

      private given JsonValueCodec[User] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))
    }

    private given JsonValueCodec[List[Post]] = JsonCodecMaker.make(CodecMakerConfig.withDecodingOnly(true))
  }
}

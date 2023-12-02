import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import zio.query.*
import zio.{RIO, RLayer, Task, ZIO, ZLayer}

import java.net.URI

trait Service {
  val posts: Task[List[Post.Dto]]
  def user(id: Int): TaskQuery[User]
}

object Service {
  val posts: RIO[Service, List[Post.Dto]]  = ZIO.serviceWithZIO(_.posts)
  def user(id: Int): RQuery[Service, User] = ZQuery.serviceWithQuery(_.user(id))

  val layer: RLayer[Client, Service] = ZLayer.derive[Live]

  private class Live(client: Client) extends Service {
    private inline val BaseUrl = "http://jsonplaceholder.typicode.com"

    val posts: Task[List[Post.Dto]] = {
      val uri = URI.create(BaseUrl + "/posts")
      client.get(uri)
    }

    def user(id: Int): TaskQuery[User] =
      ZQuery.fromRequest(GetUser(id))(userDS)

    private val userDS = DataSource.fromFunctionZIO("UsersDs") { (req: GetUser) =>
      client.get[User](URI.create(BaseUrl + "/users/" + req.id))
    }

    private case class GetUser(id: Int) extends Request[Throwable, User]

    private given JsonValueCodec[User]           = JsonCodecMaker.make
    private given JsonValueCodec[List[Post.Dto]] = JsonCodecMaker.make
  }
}

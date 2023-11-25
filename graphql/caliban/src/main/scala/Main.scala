import caliban.*
import caliban.quick.*
import zio.ZIOAppDefault

object Main extends ZIOAppDefault {
  val _ = System.setProperty("jdk.httpclient.connectionPoolSize", "200")

  private val api = graphQL(RootResolver(Query(Service.posts)))

  def run =
    api
      .runServer(8000, apiPath = "/graphql")
      .provide(Service.layer, Client.live)
}

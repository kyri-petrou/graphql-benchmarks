import caliban.*
import caliban.quick.*
import zio.*
import zio.http.{Client as _, *}
import zio.http.netty.{ChannelType, NettyConfig}

object Main extends ZIOAppDefault {
  private val api = graphQL(RootResolver(Query(Service.posts)))

  def run =
    api
      .toApp("/graphql")
      .flatMap(Server.serve)
      .provide(
        Service.layer,
        Client.live,
        Server.customized,
        ZLayer.succeed(Server.Config.default.port(8000)),
        ZLayer.succeed(NettyConfig.default.channelType(ChannelType.URING))
      )
}

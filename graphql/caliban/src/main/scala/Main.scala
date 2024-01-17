import caliban.*
import caliban.execution.QueryExecution
import caliban.quick.*
import zio.*
import zio.http.Server
import zio.http.netty.{ChannelType, NettyConfig}

object Main extends ZIOAppDefault {
  private val api                                      = graphQL(RootResolver(Query(Service.posts)))
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers

  def run =
    api
      .toApp(apiPath = "/graphql")
      .flatMap(Server.serve)
      .provide(
        Server.customized,
        ZLayer.succeed(Server.Config.default.port(8000)),
        ZLayer.succeed(
          NettyConfig.defaultWithFastShutdown
            .maxThreads(java.lang.Runtime.getRuntime.availableProcessors())
            .leakDetection(NettyConfig.LeakDetectionLevel.DISABLED)
            .channelType(ChannelType.URING)
        ),
        Service.layer,
        Client.live,
        ZLayer.scoped(Configurator.setQueryExecution(QueryExecution.Batched))
      )
}

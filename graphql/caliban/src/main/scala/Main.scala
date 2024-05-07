import caliban.*
import caliban.execution.QueryExecution
import zio.*
import zio.http.netty.{ChannelType, NettyConfig}
import zio.http.{Client as _, *}

object Main extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    ZLayer.make[Any](Runtime.removeDefaultLoggers, Runtime.disableFlags(RuntimeFlag.FiberRoots))

  private val api = graphQL(RootResolver(Query(Service.posts)))

  private val nProcessors = java.lang.Runtime.getRuntime.availableProcessors()

  def run =
    Server
      .serve(QuickAdapter(api.interpreterUnsafe).toApp(apiPath = "/graphql"))
      .provideSomeEnvironment[Service & Server](_.prune)
      .provide(
        Server.customized,
        ZLayer.succeed(Server.Config.default.port(8000)),
        ZLayer.succeed(NettyConfig.default.maxThreads(nProcessors).channelType(ChannelType.URING)),
        Service.layer,
        Client.live,
        ZLayer.scoped(Configurator.setQueryExecution(QueryExecution.Batched))
      )
}

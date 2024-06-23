import caliban.*
import caliban.execution.QueryExecution
import caliban.quick.*
import zio.*

object Main extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers ++ Runtime.disableFlags(RuntimeFlag.FiberRoots)

  private val api = ZIO.serviceWith[Service](svc => graphQL(RootResolver(Query(svc.posts))))
  def run =
    api
      .flatMap(_.runServer(8000, apiPath = "/graphql"))
      .provide(Service.layer, Client.live, ZLayer.scoped(Configurator.setQueryExecution(QueryExecution.Batched)))
}

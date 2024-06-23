import caliban.*
import caliban.execution.QueryExecution
import caliban.quick.*
import zio.*
import zio.RuntimeFlag.EagerShiftBack
import kyo.KyoSchedulerZIOAppDefault

object Main extends KyoSchedulerZIOAppDefault {

  override val bootstrap: ZLayer[Any, Any, Any] =
    ZLayer.make[Any](
      Runtime.removeDefaultLoggers,
      Runtime.disableFlags(RuntimeFlag.FiberRoots),
      Runtime.enableFlags(EagerShiftBack)
    )

  private val api = ZIO.serviceWith[Service](svc => graphQL(RootResolver(Query(svc.posts))))
  def run =
    api
      .flatMap(_.runServer(8000, apiPath = "/graphql"))
      .provide(Service.layer, Client.live, ZLayer.scoped(Configurator.setQueryExecution(QueryExecution.Batched)))

}

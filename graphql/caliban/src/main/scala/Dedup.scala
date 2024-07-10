import caliban.{CalibanError, GraphQLRequest, GraphQLResponse}
import caliban.wrappers.Wrapper.OverallWrapper
import com.github.benmanes.caffeine.cache.Caffeine
import zio.*

object Dedup {
  def wrapper(): OverallWrapper[Any] = new Dedup
}

final class Dedup extends OverallWrapper[Any] {
  private given Unsafe = Unsafe.unsafe(identity)

  private val cache = Caffeine
    .newBuilder()
    .expireAfterWrite(1.milli)
    .build[GraphQLRequest, Promise[Nothing, GraphQLResponse[CalibanError]]]
    .asMap()

  def wrap[R1 <: Any](
      f: GraphQLRequest => ZIO[R1, Nothing, GraphQLResponse[CalibanError]]
  ): GraphQLRequest => ZIO[R1, Nothing, GraphQLResponse[CalibanError]] = { req =>
    cache.get(req) match {
      case null =>
        val fresh = Promise.unsafe.make[Nothing, GraphQLResponse[CalibanError]](FiberId.None)
        cache.putIfAbsent(req, fresh) match {
          case null      => f(req).onExit(fresh.done(_))
          case fromCache => fromCache.await
        }
      case promise => promise.await
    }
  }
}

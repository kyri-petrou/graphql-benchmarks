import caliban.*
import caliban.schema.Schema.*
import caliban.schema.Annotations.{GQLExcluded, GQLField}
import caliban.schema.{Schema, SchemaDerivation}
import zio.*
import zio.query.{RQuery, TaskQuery}

case class Query(
    posts: Task[List[Post]]
)

object Query {
  given (using Service): Schema[Any, Query] = Schema.SemiAuto.derived
}

case class User(
    id: Int,
    name: String,
    username: String,
    email: String,
    phone: Option[String],
    website: Option[String]
) derives Schema.SemiAuto

case class Post(
    userId: Int,
    id: Int,
    title: String,
    body: String
)

object Post {
  given (using svc: Service): Schema[Any, Post] =
    Schema.customObj("Post")(
      field("userId")(_.userId),
      field("id")(_.id),
      field("title")(_.title),
      field("body")(_.body),
      field("user")(p => svc.user(p.userId))
    )
}

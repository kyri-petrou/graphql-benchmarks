import caliban.*
import caliban.schema.Schema.*
import caliban.schema.Annotations.{GQLExcluded, GQLField}
import caliban.schema.{Schema, SchemaDerivation}
import zio.*
import zio.query.RQuery

object ServiceSchema extends SchemaDerivation[Service]

case class Query(
    posts: Task[List[Post]]
) derives ServiceSchema.SemiAuto

case class User(
    id: Int,
    name: String,
    username: String,
    email: String,
    phone: Option[String],
    website: Option[String]
) derives ServiceSchema.SemiAuto

case class Post(
    userId: Int,
    id: Int,
    title: String,
    body: String,
    @GQLExcluded service: Service
) derives ServiceSchema.SemiAuto {
  @GQLField def user: RQuery[Service, User] = service.user(userId)
}

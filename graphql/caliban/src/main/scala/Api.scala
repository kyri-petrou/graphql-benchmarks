import caliban.*
import caliban.schema.Schema.*
import caliban.schema.{Schema, SchemaDerivation}
import zio.*
import zio.query.{RQuery, ZQuery}

import scala.compiletime.summonInline

object ServiceSchema extends SchemaDerivation[Service]

case class Query(
    posts: RIO[Service, List[Post.Dto]]
) derives ServiceSchema.SemiAuto

case class User(
    id: Int,
    name: String,
    username: String,
    email: String,
    phone: Option[String],
    website: Option[String],
) derives ServiceSchema.SemiAuto

case class Post(
    id: Int,
    userId: Int,
    title: String,
    body: String,
    user: RQuery[Service, User],
) derives ServiceSchema.SemiAuto

object Post {
  case class Dto(id: Int, userId: Int, title: String, body: String)

  object Dto {
    given Schema[Service, Post.Dto] = ServiceSchema[Post].contramap { post =>
      Post(post.id, post.userId, post.title, post.body, ZQuery.suspend(Service.user(post.userId)))
    }
  }
}

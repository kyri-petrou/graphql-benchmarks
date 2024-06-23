import caliban.*
import caliban.schema.Schema
import caliban.schema.Schema.*
import zio.*
import zio.query.TaskQuery

case class Query(
    posts: Task[List[Post]]
) derives Schema.SemiAuto

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
    body: String,
    user: TaskQuery[User]
) derives Schema.SemiAuto

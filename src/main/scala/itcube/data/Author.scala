package itcube.data

import zio.schema._

case class Author(name: String, country: Option[String])

object Author {
  implicit val authorSchema: Schema[Author] = DeriveSchema.gen[Author]
}

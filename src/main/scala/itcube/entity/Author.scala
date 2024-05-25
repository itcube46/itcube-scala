package itcube.entity

import zio.schema._

import java.util.UUID

/** Сущность "Автор". */
case class Author(id: Option[UUID],
                  name: String,
                  country: Option[String])

object Author {
  // Автоматический вывод схемы класса данных автора
  implicit val authorSchema: Schema[Author] = DeriveSchema.gen[Author]
}

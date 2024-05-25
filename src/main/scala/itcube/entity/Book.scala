package itcube.entity

import zio.schema._

import java.util.UUID

/** Сущность "Книга". */
case class Book(id: Option[UUID],
                title: String,
                isbn: Option[String],
                year: Option[Int],
                image: Option[String],
                annotation: Option[String],
                publisher: Option[Publisher],
                author: Option[Author])

object Book {
  // Автоматический вывод схемы класса данных книги
  implicit val bookSchema: Schema[Book] = DeriveSchema.gen[Book]
}

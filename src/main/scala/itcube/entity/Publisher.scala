package itcube.entity

import zio.schema._

import java.util.UUID

/** Сущность "Издатель". */
case class Publisher(id: Option[UUID],
                     name: String,
                     country: String)

object Publisher {
  // Автоматический вывод схемы класса данных издателя
  implicit val publisherSchema: Schema[Publisher] = DeriveSchema.gen[Publisher]
}

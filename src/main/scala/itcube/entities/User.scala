package itcube.entities

import zio.schema._

import java.util.UUID

/** Сущность "Пользователь". */
case class User(id: Option[UUID],
                name: String,
                email: String,
                password: String)

object User {
  // Автоматический вывод схемы пользователя
  implicit val userSchema: Schema[User] = DeriveSchema.gen[User]
}

package itcube.repositories.user

import itcube.entities.User
import zio._

/** Репозиторий пользователей. */
trait UserRepository {
  /** Получить всех пользователей. */
  def all: Task[List[User]]

  /** Получить пользователя по ID. */
  def findById(id: String): Task[Option[User]]

  /** Получить пользователя по email. */
  def findByEmail(email: String): Task[Option[User]]

  /** Получить пользователя по имени. */
  def findByName(name: String): Task[Option[User]]

  /** Создать пользователя. */
  def create(user: User): Task[Option[User]]

  /** Изменить пользователя. */
  def update(user: User): Task[Option[User]]

  /** Удалить пользователя. */
  def delete(id: String): Task[Unit]
}

object UserRepository {
  /** Сервис для получения всех пользователей. */
  def all: ZIO[UserRepository, Throwable, List[User]] =
    ZIO.serviceWithZIO[UserRepository](_.all)

  /** Сервис для получения пользователя по ID. */
  def findById(id: String): ZIO[UserRepository, Throwable, Option[User]] =
    ZIO.serviceWithZIO[UserRepository](_.findById(id))

  /** Сервис для получения пользователя по email. */
  def findByEmail(email: String): ZIO[UserRepository, Throwable, Option[User]] =
    ZIO.serviceWithZIO[UserRepository](_.findByEmail(email))

  /** Сервис для получения пользователя по имени. */
  def findByName(name: String): ZIO[UserRepository, Throwable, Option[User]] =
    ZIO.serviceWithZIO[UserRepository](_.findByName(name))

  /** Сервис для создания пользователя. */
  def register(user: User): ZIO[UserRepository, Throwable, Option[User]] =
    ZIO.serviceWithZIO[UserRepository](_.create(user))

  /** Сервис для изменения пользователя. */
  def update(user: User): ZIO[UserRepository, Throwable, Option[User]] =
    ZIO.serviceWithZIO[UserRepository](_.update(user))

  /** Сервис для удаления пользователя. */
  def delete(id: String): ZIO[UserRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[UserRepository](_.delete(id))
}

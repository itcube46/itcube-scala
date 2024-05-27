package itcube.repositories.author

import itcube.entities.Author
import zio._

/** Репозиторий авторов. */
trait AuthorRepository {
  /** Получить всех авторов. */
  def all: Task[List[Author]]

  /** Получить автора по ID. */
  def findById(id: String): Task[Option[Author]]

  /** Получить автора по имени. */
  def findByName(name: String): Task[Option[Author]]

  /** Создать автора. */
  def create(author: Author): Task[Option[Author]]

  /** Изменить автора. */
  def update(author: Author): Task[Option[Author]]

  /** Удалить автора. */
  def delete(id: String): Task[Unit]
}

object AuthorRepository {
  /** Сервис для получения всех авторов. */
  def all: ZIO[AuthorRepository, Throwable, List[Author]] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.all)

  /** Сервис для получения автора по ID. */
  def findById(id: String): ZIO[AuthorRepository, Throwable, Option[Author]] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.findById(id))

  /** Сервис для получения автора по имени. */
  def findByName(name: String): ZIO[AuthorRepository, Throwable, Option[Author]] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.findByName(name))

  /** Сервис для создания автора. */
  def create(author: Author): ZIO[AuthorRepository, Throwable, Option[Author]] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.create(author))

  /** Сервис для изменения автора. */
  def update(author: Author): ZIO[AuthorRepository, Throwable, Option[Author]] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.update(author))

  /** Сервис для удаления автора. */
  def delete(id: String): ZIO[AuthorRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.delete(id))
}

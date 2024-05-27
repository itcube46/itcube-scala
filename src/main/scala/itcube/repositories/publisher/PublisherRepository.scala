package itcube.repositories.publisher

import itcube.entities.Publisher
import zio._

/** Репозиторий издателей. */
trait PublisherRepository {
  /** Получить всех издателей. */
  def all: Task[List[Publisher]]

  /** Получить издателя по ID. */
  def findById(id: String): Task[Option[Publisher]]

  /** Получить издателя по названию. */
  def findByName(name: String): Task[Option[Publisher]]

  /** Создать издателя. */
  def create(publisher: Publisher): Task[Option[Publisher]]

  /** Изменить издателя. */
  def update(publisher: Publisher): Task[Option[Publisher]]

  /** Удалить издателя. */
  def delete(id: String): Task[Unit]
}

object PublisherRepository {
  /** Сервис для получения всех издателей. */
  def all: ZIO[PublisherRepository, Throwable, List[Publisher]] =
    ZIO.serviceWithZIO[PublisherRepository](_.all)

  /** Сервис для получения издателя по ID. */
  def findById(id: String): ZIO[PublisherRepository, Throwable, Option[Publisher]] =
    ZIO.serviceWithZIO[PublisherRepository](_.findById(id))

  /** Сервис для получения автора по имени. */
  def findByName(name: String): ZIO[PublisherRepository, Throwable, Option[Publisher]] =
    ZIO.serviceWithZIO[PublisherRepository](_.findByName(name))

  /** Сервис для создания издателя. */
  def create(publisher: Publisher): ZIO[PublisherRepository, Throwable, Option[Publisher]] =
    ZIO.serviceWithZIO[PublisherRepository](_.create(publisher))

  /** Сервис для изменения издателя. */
  def update(publisher: Publisher): ZIO[PublisherRepository, Throwable, Option[Publisher]] =
    ZIO.serviceWithZIO[PublisherRepository](_.update(publisher))

  /** Сервис для удаления издателя. */
  def delete(id: String): ZIO[PublisherRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[PublisherRepository](_.delete(id))
}

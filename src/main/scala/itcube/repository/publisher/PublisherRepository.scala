package itcube.repository.publisher

import itcube.data.Publisher
import zio._

trait PublisherRepository {
  def all: Task[List[Publisher]]

  def findById(id: String): Task[Option[Publisher]]

  def create(publisher: Publisher): Task[String]

  //def update(publisher: Publisher): Task[Publisher]

  //def delete(id: String): Task[Unit]
}

object PublisherRepository {
  def all: ZIO[PublisherRepository, Throwable, List[Publisher]] =
    ZIO.serviceWithZIO[PublisherRepository](repo => repo.all)

  def findById(id: String): ZIO[PublisherRepository, Throwable, Option[Publisher]] =
    ZIO.serviceWithZIO[PublisherRepository](_.findById(id))

  def create(publisher: Publisher): ZIO[PublisherRepository, Throwable, String] =
    ZIO.serviceWithZIO[PublisherRepository](_.create(publisher))
}

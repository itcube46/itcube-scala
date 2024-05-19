package itcube.repository.author

import itcube.data.Author
import zio.{Task, ZIO}

import java.util.UUID

trait AuthorRepository {
  def all: Task[List[Author]]

  def findById(id: String): Task[Option[Author]]

  def create(author: Author): Task[String]

  def update(id: UUID, author: Author): Task[Option[Author]]

  def delete(id: String): Task[Unit]
}

object AuthorRepository {
  def all: ZIO[AuthorRepository, Throwable, List[Author]] =
    ZIO.serviceWithZIO[AuthorRepository](_.all)

  def findById(id: String): ZIO[AuthorRepository, Throwable, Option[Author]] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.findById(id))

  def create(author: Author): ZIO[AuthorRepository, Throwable, String] =
    ZIO.serviceWithZIO[AuthorRepository](repo => repo.create(author))
}

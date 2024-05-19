package itcube.repository.author

import io.getquill.{EntityQuery, Escape, PostgresZioJdbcContext, Quoted}
import itcube.data.Author
import itcube.repository.db.PostgresDataSource
import zio.{Random, Task, ULayer, ZLayer}

import java.util.UUID
import javax.sql.DataSource

case class Authors(id: UUID, name: String, country: Option[String])

case class PGAuthorRepository(ds: DataSource) extends AuthorRepository {
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  private val authorSchema: Quoted[EntityQuery[Authors]] = quote {
    query[Authors]
  }

  override def all: Task[List[Author]] = {
    ctx
      .run(authorSchema
        .map(a => Author(a.name, a.country)))
      .provide(dsLayer)
  }

  override def findById(id: String): Task[Option[Author]] = {
    ctx
      .run(authorSchema
        .filter(a => a.id == lift(UUID.fromString(id)))
        .map(a => Author(a.name, a.country)))
      .map(_.headOption)
      .provide(dsLayer)
  }

  override def create(author: Author): Task[String] = {
    {
      for {
        id <- Random.nextUUID
        _ <- ctx
          .run(authorSchema
            .insertValue(lift(Authors(id, author.name, author.country))))
      } yield id.toString
    }.provide(dsLayer)
  }

  def update(id: UUID, author: Author): Task[Option[Author]] = {
    ctx
      .run(authorSchema
        .updateValue(lift(Authors(id, author.name, author.country)))
        .returning(a => Author(a.name, a.country)))
      .option
      .provide(dsLayer)
  }

  def delete(id: String): Task[Unit] = {
    ctx
      .run(authorSchema
        .filter(a => a.id == lift(UUID.fromString(id)))
        .delete)
      .unit
      .provide(dsLayer)
  }
}

object PGAuthorRepository {
  def layer: ZLayer[Any, Throwable, PGAuthorRepository] =
    PostgresDataSource.layer >>> ZLayer.fromFunction(ds => PGAuthorRepository(ds))
}

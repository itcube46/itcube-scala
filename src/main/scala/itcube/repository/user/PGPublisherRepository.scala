package itcube.repository.user

import io.getquill.{EntityQuery, Escape, PostgresZioJdbcContext, Quoted}
import itcube.data.Publisher
import itcube.repository.db.PostgresDataSource
import zio._

import java.util.UUID
import javax.sql.DataSource

case class Publishers(id: UUID, name: String, country: String)

case class PGPublisherRepository(ds: DataSource) extends PublisherRepository {
  private val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  private val publisherSchema: Quoted[EntityQuery[Publishers]] = quote {
    query[Publishers]
  }

  override def all: Task[List[Publisher]] = {
    ctx
      .run(publisherSchema
        .map(pt => Publisher(pt.name, pt.country)))
      .provide(dsLayer)
  }

  override def findById(id: String): Task[Option[Publisher]] = {
    ctx
      .run(publisherSchema
        .filter(pt => pt.id == lift(UUID.fromString(id)))
        .map(pt => Publisher(pt.name, pt.country)))
      .map(_.headOption)
      .provide(dsLayer)
  }

  override def create(publisher: Publisher): Task[String] = {
    {
      for {
        id <- Random.nextUUID
        _ <- ctx
          .run(publisherSchema
            .insertValue(lift(Publishers(id, publisher.name, publisher.country))))
      } yield id.toString
    }.provide(dsLayer)
  }
}

object PGPublisherRepository {
  def layer: ZLayer[Any, Throwable, PGPublisherRepository] =
    PostgresDataSource.layer >>> ZLayer.fromFunction(PGPublisherRepository(_))
}

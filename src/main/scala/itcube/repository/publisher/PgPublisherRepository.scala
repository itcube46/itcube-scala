package itcube.repository.publisher

import io.getquill._
import itcube.entity.Publisher
import itcube.repository.PostgresDataSource
import zio._

import java.util.UUID
import javax.sql.DataSource

/** Класс данных табличного представления издателей. */
case class Publishers(id: UUID,
                      name: String,
                      country: String)

/** Репозиторий издателей для СУБД PostgreSQL. */
case class PgPublisherRepository(ds: DataSource) extends PublisherRepository {
  private val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  /** Схема Quill для запросов. */
  private val publishersSchema: Quoted[EntityQuery[Publishers]] = quote {
    query[Publishers]
  }

  /** Получить всех издателей. */
  override def all: Task[List[Publisher]] = {
    ctx
      .run(publishersSchema
        .map(row => Publisher(Some(row.id), row.name, row.country)))
      .provide(dsLayer)
  }

  /** Получить издателя по ID. */
  override def findById(id: String): Task[Option[Publisher]] = {
    val uuid = UUID.fromString(id)
    ctx
      .run(publishersSchema
        .filter(row => row.id == lift(uuid))
        .map(row => Publisher(Some(row.id), row.name, row.country)))
      .map(_.headOption)
      .provide(dsLayer)
  }

  /** Получить издателя по названию. */
  override def findByName(name: String): Task[Option[Publisher]] = {
    ctx
      .run(publishersSchema
        .filter(row => row.name == lift(name))
        .map(row => Publisher(Some(row.id), row.name, row.country)))
      .map(_.headOption)
      .provide(dsLayer)
  }

  /** Создать издателя. */
  override def create(publisher: Publisher): Task[Option[Publisher]] = {
    val created =
      for {
        id <- Random.nextUUID
        result <- ctx
          .run(publishersSchema
            .insertValue(lift(Publishers(id, publisher.name, publisher.country)))
            .returning(row => Publisher(Some(row.id), row.name, row.country)))
      } yield result

    created.option.provide(dsLayer)
  }

  /** Изменить издателя. */
  override def update(publisher: Publisher): Task[Option[Publisher]] = {
    ctx
      .run(publishersSchema
        .updateValue(lift(Publishers(publisher.id.get, publisher.name, publisher.country)))
        .returning(row => Publisher(Some(row.id), row.name, row.country)))
      .option
      .provide(dsLayer)
  }

  /** Удалить издателя. */
  override def delete(id: String): Task[Unit] = {
    val uuid = UUID.fromString(id)
    ctx
      .run(publishersSchema
        .filter(row => row.id == lift(uuid))
        .delete)
      .unit
      .provide(dsLayer)
  }
}

object PgPublisherRepository {
  /** Слой репозитория издателей. */
  val live: ZLayer[Any, Throwable, PgPublisherRepository] =
    PostgresDataSource.live >>> ZLayer.fromFunction(PgPublisherRepository(_))
}

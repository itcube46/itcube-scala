package itcube.repositories.publisher

import io.getquill._
import itcube.entities.Publisher
import itcube.repositories.PostgresDataSource
import zio._

import java.util.UUID
import javax.sql.DataSource

/** Класс данных табличного представления издателей. */
final case class Publishers(id: UUID,
                            name: String,
                            country: String)

/** Репозиторий издателей для СУБД PostgreSQL. */
case class PgPublisherRepository(ds: DataSource) extends PublisherRepository {
  private val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  /** Преобразование табличного представления данных в сущность. */
  private def toPublisher: Publishers => Publisher = {
    row =>
      Publisher(
        Some(row.id),
        row.name,
        row.country
      )
  }

  /** Получить всех издателей. */
  override def all: Task[List[Publisher]] = {
    run {
      quote {
        query[Publishers]
      }
    }
      .map(_.map(toPublisher))
      .provide(dsLayer)
  }

  /** Получить издателя по ID. */
  override def findById(id: String): Task[Option[Publisher]] = {
    val uuid = UUID.fromString(id)
    run {
      quote {
        query[Publishers].filter(_.id == lift(uuid))
      }
    }
      .map(_.headOption)
      .map(_.map(toPublisher))
      .provide(dsLayer)
  }

  /** Получить издателя по названию. */
  override def findByName(name: String): Task[Option[Publisher]] = {
    run {
      quote {
        query[Publishers].filter(_.name == lift(name))
      }
    }
      .map(_.headOption)
      .map(_.map(toPublisher))
      .provide(dsLayer)
  }

  /** Создать издателя. */
  override def create(publisher: Publisher): Task[Option[Publisher]] = {
    val created = transaction {
      for {
        id <- Random.nextUUID
        result <- run {
          quote {
            query[Publishers]
              .insertValue(lift(Publishers(id, publisher.name, publisher.country)))
              .returning(r => r)
          }
        }
      } yield result
    }
    created.option.map(_.map(toPublisher)).provide(dsLayer)
  }

  /** Изменить издателя. */
  override def update(publisher: Publisher): Task[Option[Publisher]] = {
    transaction {
      run {
        quote {
          query[Publishers]
            .filter(_.id == lift(publisher.id.get))
            .updateValue(lift(Publishers(publisher.id.get, publisher.name, publisher.country)))
            .returning(r => r)
        }
      }
    }
      .option
      .map(_.map(toPublisher))
      .provide(dsLayer)
  }

  /** Удалить издателя. */
  override def delete(id: String): Task[Unit] = {
    transaction {
      val uuid = UUID.fromString(id)
      run {
        quote {
          query[Publishers]
            .filter(_.id == lift(uuid))
            .delete
        }
      }
    }
      .unit
      .provide(dsLayer)
  }
}

object PgPublisherRepository {
  /** Слой репозитория издателей. */
  val live: ZLayer[Any, Throwable, PgPublisherRepository] =
    PostgresDataSource.live >>> ZLayer.fromFunction(PgPublisherRepository(_))
}

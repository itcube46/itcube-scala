package itcube.repositories.author

import io.getquill._
import itcube.entities.Author
import itcube.repositories.PostgresDataSource
import itcube.repositories.book.BooksAuthors
import zio._

import java.util.UUID
import javax.sql.DataSource

/** Класс данных табличного представления авторов. */
final case class Authors(id: UUID,
                         name: String,
                         country: Option[String])

/** Репозиторий авторов для СУБД PostgreSQL. */
case class PgAuthorRepository(ds: DataSource) extends AuthorRepository {
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  /** Преобразование табличного представления данных в сущность. */
  private def toAuthor: Authors => Author = {
    row =>
      Author(
        Some(row.id),
        row.name,
        row.country
      )
  }

  /** Получить всех авторов. */
  override def all: Task[List[Author]] = {
    run {
      quote {
        query[Authors]
      }
    }
      .map(_.map(toAuthor))
      .provide(dsLayer)
  }

  /** Получить автора по ID. */
  override def findById(id: String): Task[Option[Author]] = {
    val uuid = UUID.fromString(id)
    run {
      quote {
        query[Authors].filter(_.id == lift(uuid))
      }
    }
      .map(_.headOption)
      .map(_.map(toAuthor))
      .provide(dsLayer)
  }

  /** Получить автора по имени. */
  override def findByName(name: String): Task[Option[Author]] = {
    run {
      quote {
        query[Authors].filter(_.name == lift(name))
      }
    }
      .map(_.headOption)
      .map(_.map(toAuthor))
      .provide(dsLayer)
  }

  /** Создать автора. */
  override def create(author: Author): Task[Option[Author]] = {
    val created = transaction {
      for {
        id <- Random.nextUUID
        result <- run {
          quote {
            query[Authors]
              .insertValue(lift(Authors(id, author.name, author.country)))
              .returning(r => r)
          }
        }
      } yield result
    }
    created.option.map(_.map(toAuthor)).provide(dsLayer)
  }

  /** Изменить автора. */
  override def update(author: Author): Task[Option[Author]] = {
    transaction {
      run {
        quote {
          query[Authors]
            .filter(_.id == lift(author.id.get))
            .updateValue(lift(Authors(author.id.get, author.name, author.country)))
            .returning(r => r)
        }
      }
    }
      .option
      .map(_.map(toAuthor))
      .provide(dsLayer)
  }

  /** Удалить автора. */
  override def delete(id: String): Task[Unit] = {
    transaction {
      val uuid = UUID.fromString(id)
      for {
        _ <- run {
          quote {
            query[BooksAuthors]
              .filter(_.authorId == lift(uuid))
              .delete
          }
        }
        _ <- run {
          quote {
            query[Authors]
              .filter(_.id == lift(uuid))
              .delete
          }
        }
      } yield ()
    }.provide(dsLayer)
  }
}

object PgAuthorRepository {
  /** Слой репозитория авторов. */
  val live: ZLayer[Any, Throwable, PgAuthorRepository] =
    PostgresDataSource.live >>> ZLayer.fromFunction(ds => PgAuthorRepository(ds))
}

package itcube.repository.author

import io.getquill._
import itcube.entity.Author
import itcube.repository.PostgresDataSource
import zio._

import java.util.UUID
import javax.sql.DataSource

/** Класс данных табличного представления авторов. */
case class Authors(id: UUID,
                   name: String,
                   country: Option[String])

/** Репозиторий авторов для СУБД PostgreSQL. */
case class PgAuthorRepository(ds: DataSource) extends AuthorRepository {
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  /** Схема Quill для запросов. */
  private val authorsSchema: Quoted[EntityQuery[Authors]] = quote {
    query[Authors]
  }

  /** Получить всех авторов. */
  override def all: Task[List[Author]] = {
    ctx
      .run(authorsSchema
        .map(row => Author(Some(row.id), row.name, row.country)))
      .provide(dsLayer)
  }

  /** Получить автора по ID. */
  override def findById(id: String): Task[Option[Author]] = {
    val uuid = UUID.fromString(id)
    ctx
      .run(authorsSchema
        .filter(row => row.id == lift(uuid))
        .map(row => Author(Some(row.id), row.name, row.country)))
      .map(_.headOption)
      .provide(dsLayer)
  }

  /** Получить автора по имени. */
  override def findByName(name: String): Task[Option[Author]] = {
    ctx
      .run(authorsSchema
        .filter(row => row.name == lift(name))
        .map(row => Author(Some(row.id), row.name, row.country)))
      .map(_.headOption)
      .provide(dsLayer)
  }

  /** Создать автора. */
  override def create(author: Author): Task[Option[Author]] = {
    val created =
      for {
        id <- Random.nextUUID
        result <- ctx
          .run(authorsSchema
            .insertValue(lift(Authors(id, author.name, author.country)))
            .returning(row => Author(Some(row.id), row.name, row.country)))
      } yield result

    created.option.provide(dsLayer)
  }

  /** Изменить автора. */
  override def update(author: Author): Task[Option[Author]] = {
    ctx
      .run(authorsSchema
        .updateValue(lift(Authors(author.id.get, author.name, author.country)))
        .returning(row => Author(Some(row.id), row.name, row.country)))
      .option
      .provide(dsLayer)
  }

  /** Удалить автора. */
  override def delete(id: String): Task[Unit] = {
    val uuid = UUID.fromString(id)
    ctx
      .run(authorsSchema
        .filter(row => row.id == lift(uuid))
        .delete)
      .unit
      .provide(dsLayer)
  }
}

object PgAuthorRepository {
  /** Слой репозитория авторов. */
  val live: ZLayer[Any, Throwable, PgAuthorRepository] =
    PostgresDataSource.live >>> ZLayer.fromFunction(ds => PgAuthorRepository(ds))
}

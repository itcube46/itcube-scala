package itcube.repositories.user

import io.getquill._
import itcube.entities.User
import itcube.repositories.PostgresDataSource
import zio._

import java.util.UUID
import javax.sql.DataSource

/** Класс данных табличного представления пользователей. */
final case class Users(id: UUID,
                       name: String,
                       email: String,
                       password: String)

case class PgUserRepository(ds: DataSource) extends UserRepository {
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  /** Преобразование табличного представления данных в сущность. */
  private def toUser: Users => User = {
    row =>
      User(
        Some(row.id),
        row.name,
        row.email,
        row.password
      )
  }

  /** Получить всех пользователей. */
  override def all: Task[List[User]] = {
    run {
      quote {
        query[Users]
      }
    }
      .map(_.map(toUser))
      .provide(dsLayer)
  }

  /** Получить пользователя по ID. */
  override def findById(id: String): Task[Option[User]] = {
    val uuid = UUID.fromString(id)
    run {
      quote {
        query[Users].filter(_.id == lift(uuid))
      }
    }
      .map(_.headOption)
      .map(_.map(toUser))
      .provide(dsLayer)
  }

  /** Получить пользователя по email. */
  override def findByEmail(email: String): Task[Option[User]] = {
    run {
      quote {
        query[Users].filter(_.email == lift(email))
      }
    }
      .map(_.headOption)
      .map(_.map(toUser))
      .provide(dsLayer)
  }

  /** Получить пользователя по имени. */
  override def findByName(name: String): Task[Option[User]] = {
    run {
      quote {
        query[Users].filter(_.name == lift(name))
      }
    }
      .map(_.headOption)
      .map(_.map(toUser))
      .provide(dsLayer)
  }

  /** Создать пользователя. */
  override def create(user: User): Task[Option[User]] = {
    val created = transaction {
      for {
        id <- Random.nextUUID
        result <- run {
          quote {
            query[Users]
              .insertValue(lift(Users(
                id,
                user.name,
                user.email,
                user.password
              )))
              .returning(r => r)
          }
        }
      } yield result
    }
    created.option.map(_.map(toUser)).provide(dsLayer)
  }

  /** Изменить пользователя. */
  override def update(user: User): Task[Option[User]] = {
    val updated = transaction {
      run {
        quote {
          query[Users]
            .filter(_.id == lift(user.id.get))
            .updateValue(lift(Users(
              user.id.get,
              user.name,
              user.email,
              user.password
            )))
            .returning(r => r)
        }
      }
    }
    updated.option.map(_.map(toUser)).provide(dsLayer)
  }

  /** Удалить пользователя. */
  override def delete(id: String): Task[Unit] = {
    transaction {
      val uuid = UUID.fromString(id)
      run {
        quote {
          query[Users]
            .filter(_.id == lift(uuid))
            .delete
        }
      }
    }
      .unit
      .provide(dsLayer)
  }
}

object PgUserRepository {
  /** Слой репозитория пользователей. */
  val live: ZLayer[Any, Throwable, PgUserRepository] =
    PostgresDataSource.live >>> ZLayer.fromFunction(PgUserRepository(_))
}

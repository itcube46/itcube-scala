package itcube.repositories.book

import io.getquill._
import itcube.entities.{Author, Book, Publisher}
import itcube.repositories.PostgresDataSource
import itcube.repositories.author.Authors
import itcube.repositories.publisher.Publishers
import zio._

import java.util.UUID
import javax.sql.DataSource

/** Класс данных табличного представления книг. */
final case class Books(id: UUID,
                       title: String,
                       isbn: Option[String],
                       year: Option[Int],
                       image: Option[String],
                       annotation: Option[String],
                       publisherId: Option[UUID])

/** Класс данных табличного представления отношения книги-авторы. */
final case class BooksAuthors(bookId: UUID,
                              authorId: UUID)

/** Класс данных табличного представления отношения пользователи-книги. */
final case class UsersBooks(id: UUID,
                            userId: UUID,
                            bookId: UUID,
                            progress: Float,
                            rating: Integer,
                            comment: String)

case class PgBookRepository(ds: DataSource) extends BookRepository {
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  /** Преобразование табличного представления данных в сущность. */
  private def toBook: ((Books, Option[Publishers], Option[Authors])) => Book = {
    case (bookRow, publisherRow, authorRow) =>
      Book(
        Some(bookRow.id),
        bookRow.title,
        bookRow.isbn,
        bookRow.year,
        bookRow.image,
        bookRow.annotation,
        publisherRow.map(v => Publisher(Some(v.id), v.name, v.country)),
        authorRow.map(v => Author(Some(v.id), v.name, v.country))
      )
  }

  /** Получить все книги. */
  override def all: Task[List[Book]] = {
    run {
      quote {
        for {
          books <- query[Books]
          publishers <- query[Publishers].leftJoin(p => books.publisherId.contains(p.id))
          booksAuthors <- query[BooksAuthors].leftJoin(_.bookId == books.id)
          authors <- query[Authors].leftJoin(a => booksAuthors.exists(_.authorId == a.id))
        } yield (books, publishers, authors)
      }
    }
      .map(_.map(toBook))
      .provide(dsLayer)
  }

  /** Получить книгу по ID. */
  override def findById(id: String): Task[Option[Book]] = {
    val uuid = UUID.fromString(id)
    run {
      quote {
        for {
          books <- query[Books].filter(_.id == lift(uuid))
          publishers <- query[Publishers].leftJoin(p => books.publisherId.contains(p.id))
          booksAuthors <- query[BooksAuthors].leftJoin(ba => books.id == ba.bookId)
          authors <- query[Authors].leftJoin(a => booksAuthors.exists(_.authorId == a.id))
        } yield (books, publishers, authors)
      }
    }
      .map(_.headOption)
      .map(_.map(toBook))
      .provide(dsLayer)
  }

  /** Получить книгу по названию. */
  override def findByTitle(title: String): Task[Option[Book]] = {
    run {
      quote {
        for {
          books <- query[Books].filter(_.title == lift(title))
          publishers <- query[Publishers].leftJoin(p => books.publisherId.contains(p.id))
          booksAuthors <- query[BooksAuthors].leftJoin(ba => books.id == ba.bookId)
          authors <- query[Authors].leftJoin(a => booksAuthors.exists(_.authorId == a.id))
        } yield (books, publishers, authors)
      }
    }
      .map(_.headOption)
      .map(_.map(toBook))
      .provide(dsLayer)
  }

  /** Создать книгу. */
  override def create(book: Book): Task[Option[Book]] = {
    val created = transaction {
      for {
        publishers <- createBookPublisher(book.publisher)
        books <- for {
          id <- Random.nextUUID
          result <- run {
            quote {
              query[Books]
                .insertValue(lift(Books(
                  id,
                  book.title,
                  book.isbn,
                  book.year,
                  book.image,
                  book.annotation,
                  publishers.map(_.id)
                )))
                .returning(r => r)
            }
          }
        } yield result
        authors <- createBookAuthor(book.author, books.id)
      } yield (books, publishers, authors)
    }
    created.option.map(_.map(toBook)).provide(dsLayer)
  }

  /** Создать издателя книги, если он представлен и его нет в БД. */
  private def createBookPublisher(publisherOpt: Option[Publisher]): Task[Option[Publishers]] = {
    if (publisherOpt.isDefined) {
      val publisher = publisherOpt.get
      if (publisher.id.isEmpty) {
        val created =
          for {
            id <- Random.nextUUID
            publishers <- run {
              quote {
                query[Publishers]
                  .insertValue(lift(Publishers(id, publisher.name, publisher.country)))
                  .returning(r => r)
              }
            }
          } yield publishers
        created.option.provide(dsLayer)
      } else {
        val uuid = publisher.id.get
        run {
          quote {
            query[Publishers].filter(_.id == lift(uuid))
          }
        }
          .map(_.headOption)
          .provide(dsLayer)
      }
    } else {
      ZIO.none
    }
  }

  /** Создать автора, если он представлен и его нет в БД. */
  private def createBookAuthor(authorOpt: Option[Author], bookId: UUID): Task[Option[Authors]] = {
    if (authorOpt.isDefined) {
      val author = authorOpt.get
      if (author.id.isEmpty) {
        val authorRow =
          for {
            id <- Random.nextUUID
            authors <- run {
              quote {
                query[Authors]
                  .insertValue(lift(Authors(id, author.name, author.country)))
                  .returning(r => r)
              }
            }
            _ <- run {
              quote {
                query[BooksAuthors]
                  .insertValue(lift(BooksAuthors(bookId, authors.id)))
              }
            }
          } yield authors
        authorRow.option.provide(dsLayer)
      } else {
        val uuid = author.id.get
        val authorRow = for {
          authors <- run {
            quote {
              query[Authors].filter(_.id == lift(uuid))
            }
          }
          _ <- run {
            quote {
              query[BooksAuthors]
                .insertValue(lift(BooksAuthors(bookId, uuid)))
            }
          }
        } yield authors
        authorRow.map(_.headOption).provide(dsLayer)
      }
    } else {
      ZIO.none
    }
  }

  /** Изменить книгу. */
  override def update(book: Book): Task[Option[Book]] = {
    val updated = transaction {
      for {
        publishers <- createBookPublisher(book.publisher)
        books <- run {
          quote {
            query[Books]
              .filter(_.id == lift(book.id.get))
              .updateValue(lift(Books(
                book.id.get,
                book.title,
                book.isbn,
                book.year,
                book.image,
                book.annotation,
                publishers.map(_.id)
              )))
              .returning(r => r)
          }
        }
        authors <- updateBookAuthor(book.author, books.id)
      } yield (books, publishers, authors)
    }
    updated.option.map(_.map(toBook)).provide(dsLayer)
  }

  /** Обновить автора книги. */
  private def updateBookAuthor(authorOpt: Option[Author], bookId: UUID): Task[Option[Authors]] = {
    if (authorOpt.isDefined) {
      val author = authorOpt.get
      if (author.id.isEmpty) {
        val authorRow = for {
          id <- Random.nextUUID
          authors <- run {
            quote {
              query[Authors]
                .insertValue(lift(Authors(id, author.name, author.country)))
                .returning(r => r)
            }
          }
          _ <- run {
            quote {
              query[BooksAuthors]
                .insertValue(lift(BooksAuthors(bookId, authors.id)))
            }
          }
        } yield authors
        authorRow.option.provide(dsLayer)
      } else {
        val uuid = author.id.get
        val authorRow = for {
          authors <- run {
            quote {
              query[Authors].filter(_.id == lift(uuid))
            }
          }
        } yield authors
        authorRow.map(_.headOption).provide(dsLayer)
      }
    } else {
      ZIO.none
    }
  }

  /** Удалить книгу. */
  override def delete(id: String): Task[Unit] = {
    transaction {
      val uuid = UUID.fromString(id)
      for {
        _ <- run {
          quote {
            query[BooksAuthors]
              .filter(_.bookId == lift(uuid))
              .delete
          }
        }
        _ <- run {
          quote {
            query[Books]
              .filter(_.id == lift(uuid))
              .delete
          }
        }
      } yield ()
    }.provide(dsLayer)
  }

  /** Добавляет комментарий пользователя к книге. */
  override def comment(text: String, userId: String, bookId: String): Task[Unit] = {
    val result = transaction {
      for {
        userBookUUID <- getUserBook(userId, bookId)
        userBook <- setUserComment(text, userBookUUID, userId, bookId)
      } yield userBook
    }
    result.unit.provide(dsLayer)
  }

  private def getUserBook(userId: String, bookId: String): Task[Option[UUID]] = {
    val userUuid = UUID.fromString(userId)
    val bookUuid = UUID.fromString(bookId)
    run {
      quote {
        query[UsersBooks]
          .filter(ub => ub.userId == lift(userUuid) && ub.bookId == lift(bookUuid))
          .map(ub => ub.id)
      }
    }
      .map(_.headOption)
      .provide(dsLayer)
  }

  private def setUserComment(text: String,
                             userBookId: Option[UUID],
                             userId: String,
                             bookId: String): Task[Option[UsersBooks]] = {
    if (userBookId.isDefined) {
      run {
        quote {
          query[UsersBooks]
            .filter(_.id == lift(userBookId.get))
            .update(_.comment -> lift(text))
            .returning(r => r)
        }
      }
        .option
        .provide(dsLayer)
    } else {
      val userUuid = UUID.fromString(userId)
      val bookUuid = UUID.fromString(bookId)
      val created = for {
        id <- Random.nextUUID
        userBook <- run {
          quote {
            query[UsersBooks]
              .insertValue(lift(UsersBooks(id, userUuid, bookUuid, 0, 0, text)))
              .returning(r => r)
          }
        }
      } yield userBook
      created.option.provide(dsLayer)
    }
  }
}

object PgBookRepository {
  /** Слой репозитория книг. */
  val live: ZLayer[Any, Throwable, PgBookRepository] =
    PostgresDataSource.live >>> ZLayer.fromFunction(ds => PgBookRepository(ds))
}

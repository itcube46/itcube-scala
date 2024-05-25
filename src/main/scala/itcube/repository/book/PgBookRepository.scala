package itcube.repository.book

import io.getquill._
import itcube.entity.{Author, Book, Publisher}
import itcube.repository.PostgresDataSource
import itcube.repository.author.Authors
import itcube.repository.publisher.Publishers
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

/** Класс данных табличного представления отношений книги-авторы. */
case class BooksAuthors(bookId: UUID,
                        authorId: UUID)

case class PgBookRepository(ds: DataSource) extends BookRepository {
  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx._

  private val dsLayer: ULayer[DataSource] = ZLayer.succeed(ds)

  private def toBook: ((Books, Option[Publishers], Option[BooksAuthors], Option[Authors])) => Book = {
    case (book, publisher, _, author) =>
      Book(
        Some(book.id),
        book.title,
        book.isbn,
        book.year,
        book.image,
        book.annotation,
        publisher.map(v => Publisher(Some(v.id), v.name, v.country)),
        author.map(v => Author(Some(v.id), v.name, v.country))
      )
  }

  private def toBooksRow(id: UUID, book: Book): Books = {
    Books(
      id,
      book.title,
      book.isbn,
      book.year,
      book.image,
      book.annotation,
      book.publisher.flatMap(v => v.id)
    )
  }

  private def toBooksRow(book: Book): Books = {
    Books(
      book.id.get,
      book.title,
      book.isbn,
      book.year,
      book.image,
      book.annotation,
      book.publisher.flatMap(v => v.id)
    )
  }

  /** Получить все книги. */
  override def all: Task[List[Book]] = {
    run {
      quote {
        for {
          books <- query[Books]
          publishers <- query[Publishers].leftJoin(p => books.publisherId.contains(p.id))
          booksAuthors <- query[BooksAuthors].leftJoin(ba => books.id == ba.bookId)
          authors <- query[Authors].leftJoin(a => booksAuthors.exists(_.authorId == a.id))
        } yield (books, publishers, booksAuthors, authors)
      }
    }
      .map(r => r.map(toBook))
      .provide(dsLayer)
  }


  /** Получить книгу по ID. */
  override def findById(id: String): Task[Option[Book]] = {
    val uuid = UUID.fromString(id)
    run {
      quote {
        for {
          books <- query[Books].filter(b => b.id == lift(uuid))
          publishers <- query[Publishers].leftJoin(p => books.publisherId.contains(p.id))
          booksAuthors <- query[BooksAuthors].leftJoin(ba => books.id == ba.bookId)
          authors <- query[Authors].leftJoin(a => booksAuthors.exists(_.authorId == a.id))
        } yield (books, publishers, booksAuthors, authors)
      }
    }
      .map(_.headOption)
      .map(r => r.map(toBook))
      .provide(dsLayer)
  }

  /** Получить книгу по названию. */
  override def findByTitle(title: String): Task[Option[Book]] = {
    run {
      quote {
        for {
          books <- query[Books].filter(b => b.title == lift(title))
          publishers <- query[Publishers].leftJoin(p => books.publisherId.contains(p.id))
          booksAuthors <- query[BooksAuthors].leftJoin(ba => books.id == ba.bookId)
          authors <- query[Authors].leftJoin(a => booksAuthors.exists(_.authorId == a.id))
        } yield (books, publishers, booksAuthors, authors)
      }
    }
      .map(_.headOption)
      .map(r => r.map(toBook))
      .provide(dsLayer)
  }

  /** Создать книгу. */
  override def create(book: Book): Task[Option[Book]] = ???

  /** Изменить книгу. */
  override def update(book: Book): Task[Option[Book]] = ???

  /** Удалить книгу. */
  override def delete(id: String): Task[Unit] = {
    val uuid = UUID.fromString(id)
    ctx.run(
        quote {
          query[Books]
            .filter(b => b.id == lift(uuid))
            .delete
        })
      .unit
      .provide(dsLayer)
  }
}

object PgBookRepository {
  /** Слой репозитория книг. */
  val live: ZLayer[Any, Throwable, PgBookRepository] =
    PostgresDataSource.live >>> ZLayer.fromFunction(ds => PgBookRepository(ds))
}

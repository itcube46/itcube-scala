package itcube.repositories.book

import itcube.entities.Book
import zio._

/** Репозиторий книг. */
trait BookRepository {
  /** Получить все книги. */
  def all: Task[List[Book]]

  /** Получить книгу по ID. */
  def findById(id: String): Task[Option[Book]]

  /** Получить книгу по названию. */
  def findByTitle(title: String): Task[Option[Book]]

  /** Создать книгу. */
  def create(book: Book): Task[Option[Book]]

  /** Изменить книгу. */
  def update(book: Book): Task[Option[Book]]

  /** Удалить книгу. */
  def delete(id: String): Task[Unit]

  /** Добавляет комментарий к книге от пользователя. */
  def comment(text: String, userId: String, bookId: String): Task[Unit]
}

object BookRepository {
  /** Сервис для получения всех книг. */
  def all: ZIO[BookRepository, Throwable, List[Book]] =
    ZIO.serviceWithZIO[BookRepository](_.all)

  /** Сервис для получения книги по ID. */
  def findById(id: String): ZIO[BookRepository, Throwable, Option[Book]] =
    ZIO.serviceWithZIO[BookRepository](_.findById(id))

  /** Сервис для получения книги по названию. */
  def findByTitle(title: String): ZIO[BookRepository, Throwable, Option[Book]] =
    ZIO.serviceWithZIO[BookRepository](_.findByTitle(title))

  /** Сервис для создания книги. */
  def create(book: Book): ZIO[BookRepository, Throwable, Option[Book]] =
    ZIO.serviceWithZIO[BookRepository](_.create(book))

  /** Сервис для изменения книги. */
  def update(book: Book): ZIO[BookRepository, Throwable, Option[Book]] =
    ZIO.serviceWithZIO[BookRepository](_.update(book))

  /** Сервис для удаления книги. */
  def delete(id: String): ZIO[BookRepository, Throwable, Unit] =
    ZIO.serviceWithZIO[BookRepository](_.delete(id))
}

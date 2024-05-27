package itcube.rest.api

import itcube.entities.Book
import itcube.repositories.book.BookRepository
import zio._
import zio.http._
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

/** API книг. */
object BookRoutes {
  def apply(): Routes[BookRepository, Response] = {
    Routes(
      // GET /books
      // GET /books?title=:title
      Method.GET / "books" -> handler {
        (request: Request) => {
          if (request.url.queryParams.isEmpty) {
            BookRepository
              .all
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                books => Response(body = Body.from(books))
              )
          } else {
            val titles: Chunk[String] = request.url.queryParams("title")
            if (titles.nonEmpty) {
              val title = titles(0)
              BookRepository
                .findByTitle(title)
                .mapBoth(
                  error => Response.internalServerError(error.getMessage),
                  {
                    case Some(book) =>
                      Response(body = Body.from(book))
                    case None =>
                      Response.notFound(s"Book $title not found!")
                  }
                )
            } else {
              ZIO.fail(Response.badRequest("No name query param"))
            }
          }
        }
      },

      // GET /books/:id
      Method.GET / "books" / string("id") -> handler {
        (id: String, _: Request) => {
          BookRepository
            .findById(id)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              {
                case Some(book) =>
                  Response(body = Body.from(book))
                case None =>
                  Response.notFound(s"Book $id not found!")
              }
            )
        }
      },

      // POST /books
      Method.POST / "books" -> handler {
        (request: Request) =>
          for {
            book <- request.body.to[Book].orElseFail(Response.badRequest)
            response <- BookRepository
              .create(book)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                {
                  case Some(book) =>
                    Response(body = Body.from(book))
                  case None =>
                    Response.notFound(s"Book not created!")
                }
              )
          } yield response
      },

      // PATCH /books
      Method.PATCH / "books" -> handler {
        (request: Request) =>
          for {
            book <- request.body.to[Book].orElseFail(Response.badRequest)
            response <- BookRepository
              .update(book)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                {
                  case Some(book) =>
                    Response(body = Body.from(book))
                  case None =>
                    Response.notFound(s"Book ${book.id} not updated!")
                }
              )
          } yield response
      },

      // DELETE /books/:id
      Method.DELETE / "books" / string("id") -> handler {
        (id: String, _: Request) => {
          BookRepository
            .delete(id)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              _ => Response.ok
            )
        }
      }
    )
  }
}

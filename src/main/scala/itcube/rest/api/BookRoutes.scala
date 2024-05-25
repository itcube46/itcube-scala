package itcube.rest.api

import itcube.entity.Book
import itcube.repository.book.BookRepository
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
            val titles: Chunk[String] = request.url.queryParams("name")
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

      // TODO: имплементировать вставку в БД
      // POST /books
      // curl -i -X POST -H 'Content-Type: application/json' -d '{"title":"test","publisher":{"name":"test","country":"Russia"},"author":{"name":"Roman","country":"Russia"}}' http://127.0.0.1:8080/books
      Method.POST / "books" -> handler {
        (request: Request) =>
          for {
            author <- request.body.to[Book].orElseFail(Response.badRequest)
            response <- BookRepository
              .create(author)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                book => Response(body = Body.from(book))
              )
          } yield response
      }
    )
  }
}

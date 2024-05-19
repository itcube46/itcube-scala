package itcube.rest.api

import itcube.data.Author
import itcube.repository.author.AuthorRepository
import zio.http._
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

/** API авторов. */
object AuthorRoutes {
  def apply(): Routes[AuthorRepository, Response] = {
    Routes(
      Method.GET / "authors" -> handler {
        AuthorRepository
          .all
          .mapBoth(
            error => Response.internalServerError(error.getMessage),
            authors => Response(body = Body.from(authors))
          )
      },

      Method.POST / "authors" -> handler {
        (request: Request) =>
          for {
            p <- request.body.to[Author].orElseFail(Response.badRequest)
            r <- AuthorRepository
              .create(p)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                publisher => Response(body = Body.from(publisher))
              )
          } yield r
      },

      Method.GET / "authors" / string("id") -> handler(
        (id: String, _: Request) =>
          AuthorRepository
            .findById(id)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              {
                case Some(author) =>
                  Response(body = Body.from(author))
                case None =>
                  Response.notFound(s"Author $id not found!")
              }
            )
      )
    )
  }
}

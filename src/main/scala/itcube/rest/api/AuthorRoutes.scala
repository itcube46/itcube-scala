package itcube.rest.api

import itcube.entities.Author
import itcube.repositories.author.AuthorRepository
import zio._
import zio.http._
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

/** API авторов. */
object AuthorRoutes {
  def apply(): Routes[AuthorRepository, Response] = {
    Routes(
      // GET /authors
      // GET /authors?name=:name
      Method.GET / "authors" -> handler {
        (request: Request) => {
          if (request.url.queryParams.isEmpty) {
            AuthorRepository
              .all
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                authors => Response(body = Body.from(authors))
              )
          } else {
            val names: Chunk[String] = request.url.queryParams("name")
            if (names.nonEmpty) {
              val name = names(0)
              AuthorRepository
                .findByName(name)
                .mapBoth(
                  error => Response.internalServerError(error.getMessage),
                  {
                    case Some(author) =>
                      Response(body = Body.from(author))
                    case None =>
                      Response.notFound(s"Author $name not found!")
                  }
                )
            } else {
              ZIO.fail(Response.badRequest("No name query param"))
            }
          }
        }
      },

      // GET /authors/:id
      Method.GET / "authors" / string("id") -> handler {
        (id: String, _: Request) => {
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
        }
      },

      // POST /authors
      Method.POST / "authors" -> handler {
        (request: Request) =>
          for {
            author <- request.body.to[Author].orElseFail(Response.badRequest)
            response <- AuthorRepository
              .create(author)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                {
                  case Some(author) =>
                    Response(body = Body.from(author))
                  case None =>
                    Response.notFound(s"Author not created!")
                }
              )
          } yield response
      },

      // PATCH /authors
      Method.PATCH / "authors" -> handler {
        (request: Request) =>
          for {
            author <- request.body.to[Author].orElseFail(Response.badRequest)
            response <- AuthorRepository
              .update(author)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                {
                  case Some(author) =>
                    Response(body = Body.from(author))
                  case None =>
                    Response.notFound(s"Author ${author.id} not updated!")
                }
              )
          } yield response
      },

      // DELETE /authors/:id
      Method.DELETE / "authors" / string("id") -> handler {
        (id: String, _: Request) => {
          AuthorRepository
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

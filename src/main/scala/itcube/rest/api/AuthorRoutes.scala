package itcube.rest.api

import itcube.entity.Author
import itcube.repository.author.AuthorRepository
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
      // curl -i -X POST -H 'Content-Type: application/json' -d '{"name":"Roman","country":"Russia"}' http://127.0.0.1:8080/authors
      Method.POST / "authors" -> handler {
        (request: Request) =>
          for {
            author <- request.body.to[Author].orElseFail(Response.badRequest)
            response <- AuthorRepository
              .create(author)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                author => Response(body = Body.from(author))
              )
          } yield response
      },

      // PATCH /authors
      // curl -i -X PATCH -H 'Content-Type: application/json' -d '{"id":"bc03f935-439b-4165-ba3b-5dc8143d66ce","name":"Roma","country":"Russia"}' http://127.0.0.1:8080/authors
      Method.PATCH / "authors" -> handler {
        (request: Request) =>
          for {
            author <- request.body.to[Author].orElseFail(Response.badRequest)
            response <- AuthorRepository
              .update(author)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                author => Response(body = Body.from(author))
              )
          } yield response
      },

      // DELETE /authors/:id
      // curl -i -X DELETE "http://127.0.0.1:8080/authors/d87f3a26-b6c5-429d-9cfc-f7f76f7611cb"
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

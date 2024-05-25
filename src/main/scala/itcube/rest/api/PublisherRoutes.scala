package itcube.rest.api

import itcube.entity.Publisher
import itcube.repository.publisher.PublisherRepository
import zio._
import zio.http._
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

/** API издателей. */
object PublisherRoutes {
  def apply(): Routes[PublisherRepository, Response] = {
    Routes(
      // GET /publishers
      // GET /publishers?name=:name
      Method.GET / "publishers" -> handler {
        (request: Request) => {
          if (request.url.queryParams.isEmpty) {
            PublisherRepository
              .all
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                publishers => Response(body = Body.from(publishers))
              )
          } else {
            val names: Chunk[String] = request.url.queryParams("name")
            if (names.nonEmpty) {
              val name = names(0)
              PublisherRepository
                .findByName(name)
                .mapBoth(
                  error => Response.internalServerError(error.getMessage),
                  {
                    case Some(publisher) =>
                      Response(body = Body.from(publisher))
                    case None =>
                      Response.notFound(s"Publisher $name not found!")
                  }
                )
            } else {
              ZIO.fail(Response.badRequest("No name query param"))
            }
          }
        }
      },

      // GET /publishers/:id
      Method.GET / "publishers" / string("id") -> handler {
        (id: String, _: Request) => {
          PublisherRepository
            .findById(id)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              {
                case Some(publisher) =>
                  Response(body = Body.from(publisher))
                case None =>
                  Response.notFound(s"Publisher $id not found!")
              }
            )
        }
      },

      // POST /publishers
      // curl -i -X POST -H 'Content-Type: application/json' -d '{"name":"BHV","country":"Russia"}' http://127.0.0.1:8080/publishers
      Method.POST / "publishers" -> handler {
        (request: Request) =>
          for {
            publisher <- request.body.to[Publisher].orElseFail(Response.badRequest)
            response <- PublisherRepository
              .create(publisher)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                publisher => Response(body = Body.from(publisher))
              )
          } yield response
      },

      // PATCH /publishers
      // curl -i -X PATCH -H 'Content-Type: application/json' -d '{"id":"bc03f935-439b-4165-ba3b-5dc8143d66ce","name":"BHV","country":"Russia"}' http://127.0.0.1:8080/publishers
      Method.PATCH / "publishers" -> handler {
        (request: Request) =>
          for {
            publisher <- request.body.to[Publisher].orElseFail(Response.badRequest)
            response <- PublisherRepository
              .update(publisher)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                author => Response(body = Body.from(author))
              )
          } yield response
      },

      // DELETE /publishers/:id
      // curl -i -X DELETE "http://127.0.0.1:8080/publishers/d87f3a26-b6c5-429d-9cfc-f7f76f7611cb"
      Method.DELETE / "publishers" / string("id") -> handler(
        (id: String, _: Request) => {
          PublisherRepository
            .delete(id)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              _ => Response.ok
            )
        }
      )
    )
  }
}

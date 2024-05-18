package itcube.rest.api

import itcube.data.Publisher
import itcube.repository.user.PublisherRepository
import zio.http._
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

object PublisherRoutes {

  def apply(): Routes[PublisherRepository, Response] = {
    Routes(
      // GET /publishers
      Method.GET / "publishers" -> handler {
        PublisherRepository
          .all
          .mapBoth(
            error => Response.internalServerError(error.getMessage),
            publishers => Response(body = Body.from(publishers))
          )
      },

      // POST /publishers -d '{"name": "AST", "country": "RU"}'
      // curl -i -H 'Content-Type: application/json' -d '{"name":"AST","country":"Russia"}' http://127.0.0.1:8080/publishers
      Method.POST / "publishers" -> handler {
        (request: Request) =>
          for {
            p <- request.body.to[Publisher].orElseFail(Response.badRequest)
            r <- PublisherRepository
              .create(p)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                publisher => Response(body = Body.from(publisher))
              )
          } yield r
      },

      // GET /publishers/:id
      Method.GET / "publishers" / string("id") -> handler(
        (id: String, _: Request) =>
          PublisherRepository
            .findById(id)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              {
                case Some(user) =>
                  Response(body = Body.from(user))
                case None =>
                  Response.notFound(s"Publisher $id not found!")
              }
            )
      )
    )
  }

}

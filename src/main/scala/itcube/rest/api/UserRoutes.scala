package itcube.rest.api

import itcube.entities.User
import itcube.repositories.user.UserRepository
import zio._
import zio.http._
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

/** API пользователей. */
object UserRoutes {
  def apply(): Routes[UserRepository, Response] = {
    Routes(
      // GET /users
      // GET /users?name=:name
      Method.GET / "users" -> handler {
        (request: Request) => {
          if (request.url.queryParams.isEmpty) {
            UserRepository
              .all
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                users => Response(body = Body.from(users))
              )
          } else {
            val names: Chunk[String] = request.url.queryParams("name")
            if (names.nonEmpty) {
              val name = names(0)
              UserRepository
                .findByName(name)
                .mapBoth(
                  error => Response.internalServerError(error.getMessage),
                  {
                    case Some(user) =>
                      Response(body = Body.from(user))
                    case None =>
                      Response.notFound(s"User $name not found!")
                  }
                )
            } else {
              ZIO.fail(Response.badRequest("No name query param"))
            }
          }
        }
      },

      // GET /users/:id
      Method.GET / "users" / string("id") -> handler {
        (id: String, _: Request) => {
          UserRepository
            .findById(id)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              {
                case Some(user) =>
                  Response(body = Body.from(user))
                case None =>
                  Response.notFound(s"User $id not found!")
              }
            )
        }
      },

      // Авторизация пользователя
      // TODO: представленная реализация является наивной и небезопасной!
      // GET /login/:email/:password
      Method.GET / "login" / string("email") / string("password") -> handler {
        (email: String, password: String, _: Request) => {
          UserRepository
            .findByEmail(email)
            .mapBoth(
              error => Response.internalServerError(error.getMessage),
              {
                case Some(user) =>
                  if (user.password == password) {
                    Response(body = Body.from(user))
                  } else {
                    Response.unauthorized(s"User $email unauthorized!")
                  }
                case None =>
                  Response.notFound(s"User $email not found!")
              }
            )
        }
      },

      // Регистрация пользователя
      // POST /users
      Method.POST / "users" -> handler {
        (request: Request) =>
          for {
            user <- request.body.to[User].orElseFail(Response.badRequest)
            response <- UserRepository
              .register(user)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                {
                  case Some(user) =>
                    Response(body = Body.from(user))
                  case None =>
                    Response.notFound(s"User not registered!")
                }
              )
          } yield response
      },

      // PATCH /users
      Method.PATCH / "users" -> handler {
        (request: Request) =>
          for {
            user <- request.body.to[User].orElseFail(Response.badRequest)
            response <- UserRepository
              .update(user)
              .mapBoth(
                error => Response.internalServerError(error.getMessage),
                {
                  case Some(user) =>
                    Response(body = Body.from(user))
                  case None =>
                    Response.notFound(s"User ${user.id} not updated!")
                }
              )
          } yield response
      },

      // DELETE /users/:id
      Method.DELETE / "users" / string("id") -> handler {
        (id: String, _: Request) => {
          UserRepository
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

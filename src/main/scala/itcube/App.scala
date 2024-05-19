package itcube

import itcube.config.HttpServerConfig
import itcube.repository.author.PGAuthorRepository
import itcube.repository.publisher.PGPublisherRepository
import itcube.rest.api.{AuthorRoutes, PublisherRoutes}
import zio._
import zio.config.typesafe.FromConfigSourceTypesafe
import zio.http.Middleware.{CorsConfig, cors}
import zio.http._
import zio.http.netty.NettyConfig

object App extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = {
    Runtime.setConfigProvider(
      ConfigProvider.fromResourcePath()
    )
  }

  private val serverConfig: ZLayer[Any, Config.Error, Server.Config] = {
    ZLayer
      .fromZIO(
        ZIO.config[HttpServerConfig](HttpServerConfig.config).map {
          c => Server.Config.default.binding(c.host, c.port)
        }
      )
  }

  private val nettyConfig: ZLayer[Any, Config.Error, NettyConfig] = {
    ZLayer
      .fromZIO(
        ZIO.config[HttpServerConfig](HttpServerConfig.config).map { c =>
          NettyConfig.default.maxThreads(c.nThreads)
        }
      )
  }

  private val corsConfig: CorsConfig = CorsConfig()

  private val routes = PublisherRoutes() ++ AuthorRoutes()

  def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    (Server
      .install(routes.toHttpApp @@ cors(corsConfig))
      .flatMap(
        port => Console.printLine(s"Started server on port: $port")
      ) *> ZIO.never)
      .provide(
        //nettyConfig,
        serverConfig,
        PGPublisherRepository.layer,
        PGAuthorRepository.layer,
        Server.live
      )
  }

}

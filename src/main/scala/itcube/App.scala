package itcube

import itcube.config.HttpServerConfig
import itcube.repositories.author.PgAuthorRepository
import itcube.repositories.book.PgBookRepository
import itcube.repositories.publisher.PgPublisherRepository
import itcube.repositories.user.PgUserRepository
import itcube.rest.api.{AuthorRoutes, BookRoutes, PublisherRoutes, UserRoutes}
import zio._
import zio.config.typesafe.FromConfigSourceTypesafe
import zio.http.Middleware.{CorsConfig, cors}
import zio.http._
import zio.http.netty.NettyConfig

/** Главный класс - точка входа в программу. */
object App extends ZIOAppDefault {

  // Переопределённый слой начальной загрузки
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = {
    Runtime.setConfigProvider(
      ConfigProvider.fromResourcePath()
    )
  }

  // Слой конфигурации сервера
  private val serverConfig: ZLayer[Any, Config.Error, Server.Config] = {
    ZLayer.fromZIO(
      ZIO.config[HttpServerConfig](HttpServerConfig.config).map {
        c => Server.Config.default.binding(c.host, c.port)
      }
    )
  }

  // Слой конфигурации Netty
  private val nettyConfig: ZLayer[Any, Config.Error, NettyConfig] = {
    ZLayer.fromZIO(
      ZIO.config[HttpServerConfig](HttpServerConfig.config).map { c =>
        NettyConfig.default.maxThreads(c.nThreads)
      }
    )
  }

  // Конфигурация CORS
  private val corsConfig: CorsConfig = CorsConfig() // по умолчанию

  // Конкатенация "маршрутов"
  private val routes = PublisherRoutes() ++ AuthorRoutes() ++ BookRoutes() ++ UserRoutes()

  // Конвертация "маршрутов" в [[HttpApp]] и применение конфигурации CORS
  private val httpApp = routes.toHttpApp @@ cors(corsConfig)

  /** Запуск ZIO-приложения. */
  def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    (Server
      .install(httpApp)
      .flatMap(
        port => Console.printLine(s"Started server on port: $port")
      ) *> ZIO.never)
      .provide(
        PgUserRepository.live,
        PgPublisherRepository.live,
        PgAuthorRepository.live,
        PgBookRepository.live,
        //nettyConfig,
        serverConfig,
        Server.live
      )
  }

}

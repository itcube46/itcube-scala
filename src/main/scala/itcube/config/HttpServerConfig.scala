package itcube.config

import zio.Config
import zio.config.magnolia.deriveConfig

/** Data type конфигурации HTTP-сервера. */
case class HttpServerConfig(host: String, port: Int, nThreads: Int)

object HttpServerConfig {
  // Автоматический вывод конфигурации HTTP-сервера с помощью Magnolia
  val config: Config[HttpServerConfig] =
    deriveConfig[HttpServerConfig].nested("HttpServerConfig")
}

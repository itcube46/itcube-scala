package itcube.repository

import io.getquill.jdbczio.Quill
import zio.ZLayer

import javax.sql.DataSource

/** Получение DataSource PostgreSQL. */
object PostgresDataSource {
  def live: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("PgDataSource").orDie
}

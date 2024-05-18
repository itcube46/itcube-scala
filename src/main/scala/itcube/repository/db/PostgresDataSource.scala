package itcube.repository.db

import io.getquill.jdbczio.Quill
import zio.ZLayer

import javax.sql.DataSource

/** Получение DataSource PostgreSQL. */
object PostgresDataSource {
  def layer: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("PGDataSource").orDie
}

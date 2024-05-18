package itcube.data

import zio.schema._

case class Publisher(name: String, country: String)

object Publisher {
  implicit val publisherSchema: Schema[Publisher] = DeriveSchema.gen[Publisher]
  //implicit val publisherEncoder: JsonEncoder[Publisher] = DeriveJsonEncoder.gen[Publisher]
  //implicit val publisherDecoder: JsonDecoder[Publisher] = DeriveJsonDecoder.gen[Publisher]
}

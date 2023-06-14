/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package persistence
package databaseComponent
package mongoImpl

import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.BsonType
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.{ CodecProvider, CodecRegistry }
import org.bson.codecs.configuration.CodecRegistries.{fromRegistries, fromProviders}

import util.data.User


case class MongoUser(_id: Int, name: String, passHash: String)

object MongoUser:

    def apply(name: String, hash: String): MongoUser = MongoUser(new ObjectId().hashCode(), name, hash)

    private class MongoUserCodec(registry: CodecRegistry) extends Codec[MongoUser]:

        override def decode(reader: org.bson.BsonReader, decoderContext: DecoderContext): MongoUser = {
            reader.readStartDocument()
            var id = -1
            var name = ""
            var passHash = ""
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                reader.readName() match
                    case "_id" => id = reader.readInt32()
                    case "name" => name = reader.readString()
                    case "passHash" => passHash = reader.readString()
                    case _ => reader.skipValue()
            }
            reader.readEndDocument()
            MongoUser(id, name, passHash)
        }

        override def encode(writer: org.bson.BsonWriter, value: MongoUser, encoderContext: EncoderContext): Unit = {
            writer.writeStartDocument()
            writer.writeInt32("_id", value._id)
            writer.writeString("name", value.name)
            writer.writeString("passHash", value.passHash)
            writer.writeEndDocument()
        }

        override def getEncoderClass: Class[MongoUser] = classOf[MongoUser]

    private val mongoUserCodecProvider = new CodecProvider {
           @SuppressWarnings(Array("unchecked"))
           def get[C](clazz: Class[C], codecRegistry: CodecRegistry): Codec[C] = {
              if (classOf[MongoUser].isAssignableFrom(clazz)) {
                (new MongoUserCodec(codecRegistry)).asInstanceOf[Codec[C]]
              } else {
                null
              }
           }
         }

    val codecRegistry = fromRegistries(fromProviders(mongoUserCodecProvider), DEFAULT_CODEC_REGISTRY)

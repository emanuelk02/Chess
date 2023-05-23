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

import java.sql.Date
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.{BsonType, BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.{ CodecProvider, CodecRegistry }
import org.bson.codecs.configuration.CodecRegistries.{fromCodecs, fromRegistries, fromProviders}

import util.data.FenParser._
import util.data.GameSession


case class MongoSession(_id: Int, user_id: Int, session: GameSession)

object MongoSession:

    def apply(user_id: Int, sess: GameSession): MongoSession = MongoSession(new ObjectId().hashCode().abs, user_id, sess)

    private class GameSessionCodec extends Codec[GameSession]:

        override def decode(reader: BsonReader, decoderContext: DecoderContext): GameSession = {
            reader.readStartDocument()
            var name: String = null
            var date: Date = null
            var fen: String = null
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                reader.readName() match
                    case "name" => name = reader.readString()
                    case "date" => date = Date(reader.readDateTime())
                    case "fen" => fen = reader.readString()
                    case _ => reader.skipValue()
            }
            reader.readEndDocument()
            new GameSession(name, date, fen)
        }

        override def encode(writer: BsonWriter, value: GameSession, encoderContext: EncoderContext): Unit = {
            writer.writeStartDocument()
            writer.writeString("name", value.displayName)
            writer.writeDateTime("date", value.date.getTime())
            writer.writeString("fen", value.toFen)
            writer.writeEndDocument()
        }

        override def getEncoderClass: Class[GameSession] = classOf[GameSession]

    private class MongoSessionCodec(registry: CodecRegistry) extends Codec[MongoSession]:

        private val sessionCodec = registry.get(classOf[GameSession])

        override def decode(reader: BsonReader, decoderContext: DecoderContext): MongoSession = {
            reader.readStartDocument()
            var id: Int = -1
            var user_id: Int = -1
            var session: GameSession = null
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                reader.readName() match
                    case "_id" => id = reader.readInt32()
                    case "user_id" => user_id = reader.readInt32()
                    case "session" => session = sessionCodec.decode(reader, decoderContext)
                    case _ => reader.skipValue()
            }
            reader.readEndDocument()
            MongoSession(id, user_id, session)
        }

        override def encode(writer: BsonWriter, value: MongoSession, encoderContext: EncoderContext): Unit = {
            writer.writeStartDocument()
            writer.writeInt32("_id", value._id)
            writer.writeInt32("user_id", value.user_id)
            writer.writeName("session")
            sessionCodec.encode(writer, value.session, encoderContext)
            writer.writeEndDocument()
        }

        override def getEncoderClass: Class[MongoSession] = classOf[MongoSession]

    private val mongoSessionCodecProvider = new CodecProvider {
           @SuppressWarnings(Array("unchecked"))
           def get[C](clazz: Class[C], codecRegistry: CodecRegistry): Codec[C] = {
              if (classOf[MongoSession].isAssignableFrom(clazz)) {
                (new MongoSessionCodec(codecRegistry)).asInstanceOf[Codec[C]]
              } else {
                null
              }
           }
         }

    val codecRegistry = fromRegistries(
        MongoUser.codecRegistry,
        fromCodecs(new GameSessionCodec),
        fromProviders(mongoSessionCodecProvider),
        DEFAULT_CODEC_REGISTRY
    )

/**
 * Implementation of [[DataEventEmitter]] that is specialized for input streams.
 *
 * @inheritdoc
 */

package event

import java.io.InputStream

import scala.io.Source
import scala.io.BufferedSource
import scala.collection.mutable.Buffer
import scala.util.control.Breaks._

class StreamEventEmitter(stream: InputStream, chunkSize: Int = 4096, defaultMax: Int = 10)
        extends DataEventEmitter[Array[Byte]](defaultMax) {
    private var buffer = Buffer[Byte]()
    private var lastByteEof = false
    protected override def executeClose() = {
        instant[DataEventEmitter.CloseEvent](DataEventEmitter.close, ())
        stream.close()
    }

    protected override def poll(): Unit = {
        val avil = stream.available()
        for(x <- 0 to avil) {
            val read = stream.read()
            if(read == -1) lastByteEof = true
            else buffer += read.asInstanceOf[Byte]
        }
        if(buffer.length > chunkSize) {
            val first = buffer.slice(0, chunkSize)
            emit[DataEventEmitter.DataEvent](DataEventEmitter.data, first.toArray)
            buffer = buffer.slice(chunkSize, buffer.length)
        } else if(lastByteEof) {
            emit[DataEventEmitter.DataEvent](DataEventEmitter.data, buffer.toArray)
            buffer.clear()
            close()
        }
    }
}
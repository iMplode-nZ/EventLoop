/**
 * Implementation of [[DataEventEmitter]] that is specialized for input streams. This is a version that uses multiple threads because the normal edition breaks with some badly designed streams.
 *
 * @inheritdoc
 */

package event

import java.io.InputStream

import scala.io.Source
import scala.io.BufferedSource
import scala.collection.mutable.Buffer
import scala.util.control.Breaks._

class ThreadedStreamEventEmitter(stream: InputStream, chunkSize: Int = 4096, defaultMax: Int = 10)
        extends DataEventEmitter[Array[Byte]](defaultMax) {
    new Thread(() => {
        while(!isExited) {
            val read = stream.read()
            if(read == -1) lastByteEof = true
            else buffer.synchronized {
                buffer += read.asInstanceOf[Byte]
            }
        }
        stream.close()
    }).start()

    private var buffer = Buffer[Byte]()
    private var lastByteEof = false
    private var isExited = false
    protected override def executeClose() = {
        isExited = true
        instant[DataEventEmitter.CloseEvent](DataEventEmitter.close, ())
    }

    protected override def poll(): Unit = buffer.synchronized {
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
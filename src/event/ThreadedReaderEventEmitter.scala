/**
 * Implementation of [[DataEventEmitter]] that is specialized for readers. This is a version that uses multiple threads because the normal edition breaks with some badly designed streams.
 *
 * @inheritdoc
 */

package event

import java.io.Reader

import scala.collection.mutable.Buffer

class ThreadedReaderEventEmitter(reader: Reader, chunkSize: Int = 4096, defaultMax: Int = 10, closeStream: Boolean = true)
        extends DataEventEmitter[Array[Byte]](defaultMax) {
    Runtime.getRuntime.addShutdownHook(Thread(forceClose _))
    new Thread(() => {
        while(!isExited) {
            val read = reader.read()
            if(read == -1) lastByteEof = true
            else buffer.synchronized {
                buffer += read.asInstanceOf[Byte]
            }
        }
        if(closeStream) reader.close()
    }).start()

    private var buffer = Buffer[Byte]()
    private var lastByteEof = false
    private var isExited = false
    protected override def executeClose() = {
        isExited = true
        super.executeClose()
    }

    protected override def poll(): Unit = buffer.synchronized {
        if(buffer.length >= chunkSize) {
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
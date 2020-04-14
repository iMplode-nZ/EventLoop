/**
 * Implementation of [[DataEventEmitter]] that is specialized for readers.
 *
 * @inheritdoc
 */

package event

import java.io.Reader

import scala.collection.mutable.Buffer

class ReaderEventEmitter(reader: Reader, chunkSize: Int = 4096, defaultMax: Int = 10)
        extends DataEventEmitter[Array[Char]](defaultMax) {
    private var buffer = Buffer[Char]()
    private var lastByteEof = false
    protected override def executeClose() = {
        reader.close()
        super.executeClose()
    }

    protected override def poll(): Unit = {
        while(reader.ready()) {
            val read = reader.read()
            if(read == -1) lastByteEof = true
            else buffer += read.asInstanceOf[Char]
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
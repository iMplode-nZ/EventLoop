package event

private type R[T, X <: DataEventEmitter.DataEventType] = X match {
    case DataEventEmitter.DataEvent => T
    case DataEventEmitter.CloseEvent => Unit
}
/**
 * Emits data and close events.
 *
 * Data events are of type [[Event[T]].
 *
 * Close events are of type [[Unit]].
 * @tparam [T] the type of the data
 */
abstract class DataEventEmitter[T](defaultMax: Int = 10)
        extends BaseEventEmitter[DataEventEmitter.DataEventType, DataEventEmitter[T],
            [X <: DataEventEmitter.DataEventType] =>> R[T, X]](defaultMax) {
    protected override def executeClose(): Unit = {
        instant[DataEventEmitter.CloseEvent](DataEventEmitter.close, ())
    }
}
/**
 * Type container to reduce name collisions
 */
object DataEventEmitter {
    /**
     * Base name type for [[DataEventEmitter]].
     */
    sealed trait DataEventType extends EventType
    /**
     * Type container for [[data]].
     */
    sealed class DataEvent extends DataEventType
    /**
     * Type container for [[close]].
     */
    sealed class CloseEvent extends DataEventType
    /**
     * Called on data.
     */
    val data = new DataEvent
    /**
     * Called on closing of the data stream.
     */
    val close = new CloseEvent
}
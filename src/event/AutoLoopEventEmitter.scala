package event
import eventloop.Loop._
/**
 * Automatically adds itself to the event loop when created.
 *
 * @inheritdoc
 */
trait AutoLoopEventEmitter[S <: EventType, T <: AutoLoopEventEmitter[S, T, R], R[_ <: S]]
        extends EventEmitter[S, T, R] { this: T =>
    loop.addEventPoller(this)
}
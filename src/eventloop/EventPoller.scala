package eventloop
import scala.collection.TraversableOnce
/**
 * An event poller. Will be called every event loop.
 *
 * Supports getting closed, and getting events.
 *
 * Methods protected to avoid name collisions.
 */
trait EventPoller {
    /**
     * Returns the events to execute.
     *
     * @return The events to execute, recommended to be a list or an array.
     */
    protected def events(): TraversableOnce[Event]
    /**
     * Whether the event poller is closing.
     *
     * Should be off after `EventPoller.close` has been called.
     *
     * @return Whether the event poller is closing.
     */
    protected def closing(): Boolean
    /**
     * Method to close it from outside.
     */
    def close(): Unit
    /**
     * Method to close the event poller that is called by the event loop.
     */
    protected def executeClose(): Unit
}
/**
 * Object to access protected methods.
 */
object EventPoller {
    /**
     * The result of callling `events` on an event poller.
     *
     * See `EventPoller.events`
     *
     * @param x The event poller to call `events` on.
     * @return The exact value returned by `x.events()`
     */
    def events(x: EventPoller): TraversableOnce[Event] = x.events()
    /**
     * The result of callling `closing` on an event poller.
     *
     * See `EventPoller.closing`
     *
     * @param x The event poller to call `closing` on.
     * @return The exact value returned by `x.closing()`
     */
    def closing(x: EventPoller): Boolean = x.closing()
    /**
     * The result of callling `close` on an event poller.
     *
     * See `EventPoller.close`
     *
     * @param x The event poller to call `close` on.
     * @return The exact value returned by `x.close`()`
     */
    def executeClose(x: EventPoller): Unit = x.executeClose()
}
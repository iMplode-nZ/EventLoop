package event
import eventloop.EventPoller
import scala.collection.Set
/**
 * Supertrait of all event key types.
 */
trait EventType
/**
 * Type of an event function that may only be executed once.
 *
 * @tparam [V] Type of the function
 */
case class EventFunction[V](function: EventListener[V], once: Boolean)
/**
 * Emits events.
 *
 * @tparam [S] Base type of event keys
 * @tparam [T] This
 * @tparam [R] Mapping between event keys and types of events
 */
trait EventEmitter[S <: EventType, T <: EventEmitter[S, T, R], R[_ <: S]] extends EventPoller { this: T =>
    /**
     * Sets a listener to go off for every event for a key
     *
     * @tparam [K] The type of the name
     * @param name The key for the event
     * @param listener The listener to be called
     */
    def on[K <: S](name: K, listener: EventListener[R[K]]): T
    /**
     * Sets a listener to go off for the first event for a key
     *
     * @tparam [K] The type of the name
     * @param name The key for the event
     * @param listener The listener to be called
     */
    def once[K <: S](name: K, listener: EventListener[R[K]]): T
    /**
     * Makes a listener be executed before other listeners.
     *
     * See `EventEmitter.on`
     *
     * @tparam [K] The type of the name
     * @param name The key for the event
     * @param listener The listener to be called
     */
    def prependOn[K <: S](name: K, listener: EventListener[R[K]]): T
    /**
     * Makes a listener be executed before other listeners for one event.
     *
     * See `EventEmitter.on`.
     *
     * @tparam [K] The type of the name
     * @param name The key for the event
     * @param listener The listener to be called
     */
    def prependOnce[K <: S](name: K, listener: EventListener[R[K]]): T
    /**
     * Removes all listeners.
     */
    def offAll(): T
    /**
     * Removes all listeners for a certain event.
     *
     * @tparam [K] The type of the name
     * @param name The key for the event
     */
    def offAll[K <: S](name: K): T
    /**
     * Removes a listener.
     *
     * @tparam [K] The type of the event name
     * @param name The key for the event
     * @param listener The listener to remove
     */
    def off[K <: S](name: K, listener: EventListener[R[K]]): T
    /**
     * The raw listeners for a key.
     *
     * Returns a list, or some traversable object of [[EventFunction]]s.
     *
     * @tparam [K] The type of the event name
     * @param name The key for the event
     */
    def raw[K <: S](name: K): TraversableOnce[EventFunction[R[K]]]
    /**
     * Amount of listeners for a key.
     *
     * See `EventEmitter.raw`.
     *
     * @tparam [K] The type of the event name
     * @param name The key for the event
     */
    def count[K <: S](name: K): Int
    /**
     * Gets the max amount of listeners.
     */
    def max: Int
    /**
     * Sets the max amount of listeners.
     */
    def max(x: Int): T
    /**
     * All possible event names.
     */
    def names: Set[S]
    /**
     * Emits an event.
     *
     * **Warning:** *To use this properly, the generic type must be specified.*
     *
     * @tparam [K] The type of the event name
     * @param name The key for the event
     * @param event The data of the event
     */
    protected def emit[K <: S](name: K, event: R[K]): Boolean
    /**
     * Emits an event, bypassing the event loop.
     *
     * See `EventEmitter.emit`.
     *
     * @tparam [K] The type of the event name
     * @param name The key for the event
     * @param event The data of the event
     */
    protected def instant[K <: S](name: K, event: R[K]): Boolean
}
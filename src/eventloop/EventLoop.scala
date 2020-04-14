package eventloop

/**
 * An event loop.
 *
 * @tparam [T] The type of an identifier for timeouts.
 * @tparam [R] The type of an identifier for intervals.
 * @tparam [S] The type of an identifier for immediates.
 */
trait EventLoop[T, R, S] {
    /**
     * Starts the event loop.
     */
    def start(): Unit
    /**
     * Regesters a timeout to be executed after some time.
     *
     * @param timeout The function to be executed
     * @param time The time in milliseconds later to execute it
     * @return The identifier for the timeout
     */
    def setTimeout(timeout: () => Unit, time: Long): T
    /**
     * Regesters an interval to be repeatedly executed.
     *
     * @param interval The function to be executed
     * @param time The time in milliseconds between each execution
     * @return The identifier for the interval
     */
    def setInterval(interval: () => Unit, time: Long): R
    /**
     * Regesters an immediate to be executed on the next pass of the event loop.
     *
     * @param immediate The function to be executed
     * @return The identifier for the immediate
     */
    def setImmediate(immediate: () => Unit): S
    /**
     * Clears a timeout.
     *
     * @param timeout The timeout to be cleared
     * @return If the timeout exists
     */
    def clearTimeout(timeout: T): Boolean
    /**
     * Clears an interval.
     *
     * @param timeout The interval to be cleared
     * @return If the interval exists
     */
    def clearInterval(interval: R): Boolean
    /**
     * Clears an immediate.
     *
     * @param timeout The immediate to be cleared
     * @return If the immediate exists
     */
    def clearImmediate(immediate: S): Boolean
    /**
     * Adds an event poller.
     *
     * Every loop of the event loop, the poller's events will be executed.
     *
     * See [[EventPoller]]
     *
     * @param poller The poller to be added on to the event loop
     */
    def addEventPoller(poller: EventPoller): Unit
    /**
     * Adds an event.
     *
     * The event will be executed the next iteration of the event loop.
     *
     * See [[Event]]
     *
     * @param event The event to be executed
     */
    def addEvent(event: Event): Unit
}
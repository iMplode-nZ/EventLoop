package event

/**
 * Events passed to [[EventListener]].
 *
 * @tparam [T] The type of the data
 */
trait Event[T] {
    /**
     * The data of the event.
     */
    def data: T
    /**
     * No future listeners will receive this event.
     *
     * Returns this.
     */
    def halt(): Event[T]

    override def toString: String = s"Event(${data})"
}
package eventloop

/**
 * An event function.
 *
 * Can be executed using function invocation syntax.
 */
trait Event {
    /**
     * The method to be run.
     */
    def apply(): Unit
}
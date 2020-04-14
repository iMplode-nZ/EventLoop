package eventloop

/**
 * This object stores information about a basic event loop.
 */

object Loop {
    private var eventloop: EventLoop[_, _, _] = new DefaultEventLoop(10)
    lazy val loop = eventloop
    def loop_=(a: EventLoop[_, _, _]): Unit = {
        eventloop = a
    }
}
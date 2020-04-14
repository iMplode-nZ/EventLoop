package eventloop

import scala.collection.mutable.Set
import scala.collection.mutable.Queue

case class TimeoutFunction(time: Long, function: () => Unit)
case class IntervalFunction(start: Long, delay: Long, var current: Long, function: () => Unit)
case class ImmediateFunction (function: () => Unit)

/**
 * The default event loop. This follows the node.js event loop mechanism, which is explained at https://nodejs.org/uk/docs/guides/event-loop-timers-and-nexttick/.
 *
 * Single threaded.
 *
 * @inheritdoc
 */
class DefaultEventLoop(max: Long) extends EventLoop[TimeoutFunction, IntervalFunction, ImmediateFunction] {
    private val timeouts: Set[TimeoutFunction] = Set()
    private val intervals: Set[IntervalFunction] = Set()
    private val immediates: Set[ImmediateFunction] = Set()
    private val poll: Queue[Event] = new Queue()
    private val pollers: Set[EventPoller] = Set()

    private def ready(time: Long): Boolean = System.currentTimeMillis() > time

    private def getTime(interval: IntervalFunction): Long = interval.start + interval.delay * interval.current

    private def readyInterval(interval: IntervalFunction): Boolean = ready(getTime(interval))

    private def remaining(): Boolean = timeouts.nonEmpty || intervals.nonEmpty || immediates.nonEmpty || poll.nonEmpty || pollers.nonEmpty

    def start(): Unit = {
        while(remaining()) {
            //Timer
            val filteredTimeouts = timeouts.filter((timeout) => ready(timeout.time))
            filteredTimeouts.foreach((timeout) => timeout.function())
            timeouts --= filteredTimeouts
            val mask = intervals.filter((interval) => readyInterval(interval))
            intervals --= mask
            intervals ++= mask.map((interval) => {
                interval.function()
                interval.current += 1
                interval
            })
            //Poll
            var break = false
            var current = 0
            while(current < max && !break) {
                if(!remaining()) break = true
                while(poll.isEmpty && !break) {
                    if(immediates.nonEmpty) break = true
                    if(pollers.isEmpty) break = true
                    if(!break) {
                        pollers.foreach((poller) => poll ++= EventPoller.events(poller))
                    }
                    if(pollers.exists((poller) => EventPoller.closing(poller))) break = true
                    if(intervals.exists((interval) => readyInterval(interval))
                        || timeouts.exists((timeout) => ready(timeout.time))) break = true
                }
                while(poll.length > 0 && !break) {
                    poll.dequeue()()
                    current = current + 1
                }
            }
            //Check
            immediates.foreach((immediate) => immediate.function())
            immediates.clear()
            //Close callbacks
            pollers --= pollers.filter((poller) => if(EventPoller.closing(poller)) {
                    EventPoller.close(poller)
                    true
                } else false)
        }
    }
    def setTimeout(timeout: () => Unit, time: Long): TimeoutFunction = {
        val func = TimeoutFunction(time + System.currentTimeMillis(), timeout)
        timeouts += func
        func
    }
    def setInterval(interval: () => Unit, time: Long): IntervalFunction = {
        val func = IntervalFunction(System.currentTimeMillis(), time, 1, interval)
        intervals += func
        func
    }
    def setImmediate(immediate: () => Unit): ImmediateFunction = {
        val func = ImmediateFunction(immediate)
        immediates += func
        func
    }
    def clearTimeout(timeout: TimeoutFunction): Boolean = {
        if(timeouts.contains(timeout)) {
            timeouts -= timeout
            true
        } else false
    }
    def clearInterval(interval: IntervalFunction): Boolean = {
        if(intervals.contains(interval)) {
            intervals -= interval
            true
        } else false
    }
    def clearImmediate(immediate: ImmediateFunction): Boolean = {
        if(immediates.contains(immediate)) {
            immediates -= immediate
            true
        } else false
    }
    def addEventPoller(poller: EventPoller): Unit = pollers += poller
    def addEvent(event: Event): Unit = poll += event
}
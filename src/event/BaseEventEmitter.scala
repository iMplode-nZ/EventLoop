package event
import util.TypedMap
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.Set

enum State {
    case Open, Closing, Closed
}
/**
 * Default implementation of [[EventEmitter]].
 *
 * @inheritdoc
 */
trait BaseEventEmitter[S <: EventType, T <: BaseEventEmitter[S, T, R], R[_ <: S]](defaultMax: Int = 10)
        extends AutoLoopEventEmitter[S, T, R] { this: T =>
    private var maxListeners = defaultMax
    protected var state = State.Open

    private val current = new ArrayBuffer[eventloop.Event]()
    private val listeners = new TypedMap[S, [X <: S] =>> ArrayBuffer[EventFunction[R[X]]]]()
    private def append[K <: S](name: K, function: EventFunction[R[K]]): Unit = listeners.get(name) match {
        case Some(x) => {
            if(x.length > maxListeners) overMax()
            x += function
        }
        case None => {
            if(maxListeners == 0) overMax()
            val addon = new ArrayBuffer[EventFunction[R[K]]]()
            listeners += (name -> addon)
            addon += function
        }
    }
    private def prepend[K <: S](name: K, function: EventFunction[R[K]]): Unit = listeners.get(name) match {
        case Some(x) => {
            if(x.length > maxListeners) overMax()
            x.prepend(function)
        }
        case None => {
            if(maxListeners == 0) overMax()
            val addon = new ArrayBuffer[EventFunction[R[K]]]()
            listeners += (name -> addon)
            addon += function
        }
    }
    def on[K <: S](name: K, listener: EventListener[R[K]]): T = {
        append(name, EventFunction[R[K]](listener, false))
        this
    }
    def once[K <: S](name: K, listener: EventListener[R[K]]): T = {
        append(name, EventFunction[R[K]](listener, true))
        this
    }
    def prependOn[K <: S](name: K, listener: EventListener[R[K]]): T = {
        prepend(name, EventFunction[R[K]](listener, false))
        this
    }
    def prependOnce[K <: S](name: K, listener: EventListener[R[K]]): T = {
        prepend(name, EventFunction[R[K]](listener, true))
        this
    }
    def offAll(): T = {
        listeners.clear()
        this
    }
    def offAll[K <: S](name: K): T = {
        listeners.get(name).foreach((x) => x.clear())
        this
    }
    def off[K <: S](name: K, listener: EventListener[R[K]]): T = {
        listeners.get(name).foreach((x) => {
            x.filter((test) => test.function ne listener)
            if(x.isEmpty) listeners -= name
        })
        this
    }
    def raw[K <: S](name: K): TraversableOnce[EventFunction[R[K]]] = listeners.get(name) match {
        case Some(x) => x.toArray[EventFunction[R[K]]]
        case None => Array[EventFunction[R[K]]]()
    }
    def count[K <: S](name: K): Int = {
        listeners.get(name) match {
            case Some(x) => x.length
            case None => 0
        }
    }
    def max: Int = maxListeners
    def max(x: Int): T = {
        maxListeners = x
        this
    }
    def names: Set[S] = listeners.keySet
    private def run[K <: S](name: K, list: ArrayBuffer[EventFunction[R[K]]], event: R[K]): Unit = {
        var stopped = false
        val ev = new Event[R[K]] {
            override val data = event
            override def halt(): Event[R[K]] = {
                stopped = true
                this
            }
        }
        listeners += name -> list.filter((a) => {
            if(stopped) true
            else {
                a.function(ev)
                !a.once
            }
        })
    }
    protected def emit[K <: S](name: K, event: R[K]): Boolean = listeners.get(name) match {
        case Some(x) => {
            current += (() => run(name, x, event))
            true
        }
        case None => false
    }
    protected def instant[K <: S](name: K, event: R[K]): Boolean = listeners.get(name) match {
        case Some(x) => {
            run(name, x, event)
            true
        }
        case None => false
    }

    protected def events(): TraversableOnce[eventloop.Event] = if(state != State.Open) Array[eventloop.Event]() else {
        poll()
        val arr = current.toArray
        current.clear
        arr
    }
    protected def poll(): Unit = ()
    protected def closing(): Boolean = state == State.Closing
    protected def close(): Unit = state match {
        case State.Open => state = State.Closing
        case State.Closing => {
            executeClose()
            state = State.Closed
        }
        case State.Closed => executeAlreadyClosed()
    }
    protected def forceClose(): Unit = state match {
        case State.Open | State.Closing => {
            executeClose()
            state = State.Closed
        }
        case State.Closed => executeAlreadyClosed()
    }
    protected def executeClose(): Unit
    protected def executeAlreadyClosed(): Unit = ()
    protected def overMax(): Unit = throw new IndexOutOfBoundsException("Too many listeners")
}
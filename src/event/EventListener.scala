package event

trait EventListener[T] {
    def apply(value: Event[T]): Unit
}
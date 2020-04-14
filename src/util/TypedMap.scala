package util

import scala.collection.immutable.Map
import scala.collection.Set
/**
 * A map with separate types for keys.
 *
 * @tparam [S] The type which all keys are a subtype of
 * @tparam [R] The map between key types and value types
 */
class TypedMap[S, R[_ <: S]](var map: Map[S, Any] = Map.empty[S, Any]) {
    /**
     * Gets a type as a value.
     *
     * @tparam [K] The type of the key
     * @param k The key
     * @return `Some[V]` if the value exists, otherwise `None`
     */
    def get[K <: S](k: K) : Option[R[K]] = map.get(k).asInstanceOf[Option[R[K]]]

    /**
     * Adds an element to the map.
     *
     * @tparam [K] The type of the key
     * @param kv The key and the value
     * @return `this`
     */
    def +=[K <: S](kv: (K, R[K])): TypedMap[S, R] = {
        map += kv
        this
    }
    /**
     * Removes an element from the map.
     *
     * @tparam [K] The type of the key
     * @param k The key
     * @return `this`
     */
    def -=[K <: S](k: K): TypedMap[S, R] = {
        map -= k
        this
    }

    /**
     * Creates a map with an element appended.
     *
     * @tparam [K] The type of the key
     * @param kv The key and the value
     * @return The new map
     */
    def +[K <: S](kv: (K, R[K])): TypedMap[S, R] = new TypedMap(map + kv)
    /**
     * Creates a map with one less element.
     *
     * @tparam [K] The type of the key
     * @param k The key
     * @return The new map
     */
    def -[K <: S](k: K): TypedMap[S, R] = new TypedMap(map - k)

    /**
     * Clears the map.
     *
     * @return `this`
     */
    def clear(): TypedMap[S, R] = {
        map = Map.empty[S, Any]
        this
    }

    /**
     * The set of all keys.
     *
     * @return The set of all keys.
     */
    def keySet : Set[S] = map.keySet
}

object TypedMap {
    /**
     * The empty map.
     *
     * @tparam [S] The supertype of all keys
     * @tparam [R] The map between keys and values
     * @return The empty map with types S and R
     */
    def empty[S, R[_ <: S]] = new TypedMap[S, R]()

    /**
     * The map with some values.
     *
     * @tparam [S] The supertype of all keys
     * @tparam [R] The map between keys and values
     * @param tuples The tuples to use to create a map
     * @return The map with the underlying map being `tuples`
     */
    def apply[S, R[_ <: S]](tuples: (S, Any)*) = new TypedMap[S, R](Map(tuples: _*))
}
(ns dijkstra.main
  (:require [clojure.set :as set]
            [dorothy.core :as dot]
            [dorothy.jvm :refer [show!]]))


(defn reject-keys
  "The inverse of select-keys, it returns `m` without the `unwanted` keys."
  [m unwanted]
  (let [desired (set/difference (into #{} (keys m)) (into #{} unwanted))]
    (select-keys m desired)))


(defn update-routes
  "Updates `routes`, which is a map from node name to another map of two
  entries: `:parent`, which is preceding entry in the shortest path to a node,
  and `:weight`, which is the total accumulated weight of the shortest path to
  that node so far. If `node` provides a shorter path to `neighbor` than
  previously known, `routes` will be updated with `node` as the new `:parent` of
  `neighbor`."
  [routes node node-weight neighbor neighbor-weight]
  (let [weight (+ node-weight neighbor-weight)]
    (cond-> routes
      (< weight (get-in routes [neighbor :weight] ##Inf))
      (assoc neighbor {:parent node, :weight weight}))))


(defn dijkstra*
  "Recursively travels from `node` to its neighbors, as defined by `graph`,
  updating `routes` with the shortest paths to the nodes as it visits them. It
  keeps track of which nodes have been `visited`."
  [graph node routes visited]
  (let [node-weight (get-in routes [node :weight] 0)
        neighbors (get graph node)
        next-routes (reduce-kv (fn [m neighbor neighbor-weight]
                                 (update-routes m
                                   node node-weight
                                   neighbor neighbor-weight))
                               routes
                               neighbors)
        next-visited (conj visited node)
        next-node (->> (reject-keys next-routes next-visited)
                       (sort-by (fn [[_ {weight :weight}]] weight))
                       ffirst)]
    (if next-node
      (recur graph next-node next-routes next-visited)
      next-routes)))


(defn path
  "Given a completed `routes` map, this constructs a vector of the node names
  along the shortest path from `start` to `finish`."
  [routes start finish]
  (loop [path '()
         node finish]
    (if-let [next (get-in routes [node :parent])]
      (recur (conj path node) next)
      (into [] (conj path start)))))


(defn dijkstra
  "The entry point for Dijkstra's algorithm. It takes a `graph`, which is a map
  whose keys are node names, and whose values are also maps defining the
  outbound edges of the node: keys are the destination nodes, and values are the
  weights of traveling to those nodes. It returns a vector of node names giving
  the shortest path from `start` to `finish`."
  [graph start finish]
  (-> (dijkstra* graph start {} #{})
      (path start finish)))


;; display utilities


(defn segments
  "Converts a `path` vector into a set of vectors containing each segment of the
  path.

  Example:

      (segments [:a :b :c]) ; => #{[:a :b] [:b :c]}"
  [path]
  (->> path
       (partition 2 1)
       (map vec)
       (into #{})))


(defn dot-edge
  "Creates a Dorothy-style edge-definition vector for the given edge. It creates
  a bolder style for edges that are part of the `shortest-path`."
  [shortest-path edge weight]
  (let [attrs (cond-> {:label weight, :style :dashed}
                (contains? shortest-path edge) (assoc :style :bold))]
    (conj edge attrs)))


(defn ->dorothy
  "Takes a `graph` as given to [[dijkstra]] and returns a vector suitable for
  use in drawing a Dorothy digraph displaying the graph with the `shortest-path`
  highlighted."
  [graph shortest-path]
  (let [path-segments (segments shortest-path)]
    (-> (reduce-kv (fn [v node neighbors]
                     (reduce-kv (fn [v neighbor weight]
                                  (conj v (dot-edge path-segments [node neighbor] weight)))
                                v
                                neighbors))
                   []
                   graph)
        (concat (map #(vector % {:style :bold}) shortest-path))
        vec)))


(defn display
  "Opens a window showing `graph`, with the shortest path from `start` to
  `finish` highlighted."
  [graph start finish]
  (let [shortest-path (dijkstra graph start finish)]
    (-> graph
        (->dorothy shortest-path)
        dot/digraph
        dot/dot
        show!)))


;; some graphs


(comment
  (let [graph {:start {:a 6, :b 2}
               :a     {:finish 1}
               :b     {:a 3, :finish 5}}]
    (display graph :start :finish))


  (let [graph {:start {:a 4, :b 2}
               :a     {:fin 3}
               :b     {:a 7, :fin 6}}]
    (display graph :start :fin))


  (let [graph {:start {:a 4, :b 2}
               :a     {:c 1, :finish 3}
               :b     {:a 3, :finish 5}
               :c     {:finish 1}}]
    (display graph :start :finish))


  (let [graph {:start {:a 5, :c 2}
               :a     {:b 4, :d 2}
               :b     {:d 6, :finish 3}
               :c     {:a 8, :d 7}
               :d     {:finish 1}}]
    (display graph :start :finish)))

# Dijkstra's algorithm

Just an implementation of [Dijkstra's algorithm][] in Clojure, written while I
was reading _[Grokking Algorithms][]_. I got carried away and added some support
for visualizing a graph and its shortest path using the [Dorothy][] library.

Just fire up a REPL via `clj` and go:

```clojure
(require '[dijkstra.main :as dijkstra])
(dijkstra/display {:start {:a 6, :b 2}
                   :a {:finish 1}
                   :b {:a 3, :finish 5}}
                   :start :finish)
```

And then marvel at one of the most famous algorithms in computer-science
history, I guess.

[Dijkstra's algorithm]: https://en.wikipedia.org/wiki/Dijkstra's_algorithm
[Grokking Algorithms]: https://www.manning.com/books/grokking-algorithms
[Dorothy]: https://github.com/daveray/dorothy

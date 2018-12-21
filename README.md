# tools.analyzer.jvm.deps

A small library providing a couple of utility functions on top of [tools.analyzer.jvm](https://github.com/clojure/tools.analyzer.jvm) for code dependency analysis

## Example Usage

```clojure
user=> (require '[clojure.tools.analyzer.jvm.deps :as d])
nil

user=> (def form '(fn [b] (if-let [a (seq (map inc b))] (cons 0 a) b)))
#'user/form

user=> (pprint (d/deps form))
#{#'clojure.core/if-let #'clojure.core/seq #'clojure.core/map
  #'clojure.core/cons #'clojure.core/inc #'clojure.core/let
  #'clojure.core/fn}
nil

user=> (pprint (d/transitive-deps #'+))
#{#'clojure.core/seq
  #'clojure.core/>1?
  #'clojure.core/chunk-next
  #'clojure.core/next
  #'clojure.core/chunked-seq?
  #'clojure.core/chunk-first
  #'clojure.core/seq?
  #'clojure.core/sequence
  #'clojure.core/list
  #'clojure.core/cast
  #'clojure.core/instance?
  #'clojure.core/+
  #'clojure.core/first
  #'clojure.core/*unchecked-math*
  #'clojure.core/chunk-cons
  #'clojure.core/chunk-rest
  #'clojure.core/nary-inline
  #'clojure.core/map
  #'clojure.core/concat
  #'clojure.core/reduce1
  #'clojure.core/cons
  #'clojure.core/rest}

user=> (pprint (d/expansion-steps form))
((fn* ([b] (if-let [a (seq (map inc b))] (cons 0 a) b)))
 (fn [b] (if-let [a (seq (map inc b))] (cons 0 a) b))
 (fn* ([b] (do (if-let [a (seq (map inc b))] (cons 0 a) b))))
 (fn* ([b] (if-let [a (seq (map inc b))] (cons 0 a) b)))
 (fn*
  ([b]
   (clojure.core/let
    [temp__5718__auto__ (seq (map inc b))]
    (if
     temp__5718__auto__
     (clojure.core/let [a temp__5718__auto__] (cons 0 a))
     b))))
 (fn*
  ([b]
   (let*
    [temp__5718__auto__ (seq (map inc b))]
    (do
     (if
      temp__5718__auto__
      (clojure.core/let [a temp__5718__auto__] (cons 0 a))
      b)))))
 (fn*
  ([b]
   (let*
    [temp__5718__auto__ (seq (map inc b))]
    (if
     temp__5718__auto__
     (clojure.core/let [a temp__5718__auto__] (cons 0 a))
     b))))
 (fn*
  ([b]
   (let*
    [temp__5718__auto__ (seq (map inc b))]
    (if
     temp__5718__auto__
     (let* [a temp__5718__auto__] (do (cons 0 a)))
     b))))
 (fn*
  ([b]
   (let*
    [temp__5718__auto__ (seq (map inc b))]
    (if
     temp__5718__auto__
     (let* [a temp__5718__auto__] (cons 0 a))
     b)))))
nil
```

## Caveat and Limitations

It currently won't be able to follow through dependencies of functions not installed via `def` (e.g. functions generated as part of runtime metaprogramming or via `intern`)

## License

Copyright © 2015-2018 Nicola Mometto.

Distributed under the Eclipse Public License, the same as Clojure.

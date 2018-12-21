# tools.analyzer.jvm.deps

A small library providing a couple of utility functions on top of `tools.analyzer.jvm` for code dependency analysis

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

user=> (pprint (d/transitive-deps form))
#{#'clojure.core/seq
  #'clojure.core/fn
  #'clojure.core/next
  #'clojure.core/chunked-seq?
  #'clojure.core/chunk-first
  #'clojure.core/seq?
  #'clojure.core/sequence
  #'clojure.core/list
  #'clojure.core/instance?
  #'clojure.core/if-let
  #'clojure.core/first
  #'clojure.core/*unchecked-math*
  #'clojure.core/chunk-cons
  #'clojure.core/chunk-rest
  #'clojure.core/let
  #'clojure.core/map
  #'clojure.core/concat
  #'clojure.core/inc
  #'clojure.core/cons
  #'clojure.core/rest}

user=> (pprint (d/macroexpansion-steps form))
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

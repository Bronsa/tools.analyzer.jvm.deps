(ns clojure.tools.analyzer.jvm.deps
  (:require
   [clojure.tools.analyzer :as a]
   [clojure.tools.analyzer.jvm :as a.j]
   [clojure.tools.analyzer.jvm.utils :as j.u]
   [clojure.tools.analyzer.ast :as ast]
   [clojure.tools.analyzer.env :as env]
   [clojure.tools.analyzer.utils :as u]
   [clojure.tools.analyzer.passes.jvm.emit-form :as j.e]
   [clojure.tools.analyzer.passes.emit-form :as e]))

(defn ^:private mexpansions
  ([ast _] (mexpansions ast))
  ([ast]
   (binding [e/-emit-form* mexpansions]
     (or (:raw ast)
         (j.e/-emit-form* ast {})))))

(defn deps
  "Takes a form or a var and returns a set of the vars it (directly) depends on,
   up to function call boundaries"
  [form]
  (if (var? form)
    #{form}
    (let [deps      (atom #{})
          mexpander (fn [form env]
                      (let [f (if (seq? form) (first form) form)
                            v (u/resolve-sym f env)]
                        (when-let [var? (and (not (-> env :locals (get f)))
                                             (var? v))]
                          (swap! deps conj v)))
                      (a.j/macroexpand-1 form env))]
      (a.j/analyze form (a.j/empty-env)
                   {:bindings {#'a/macroexpand-1 mexpander}})
      @deps)))

(def ^:private env (a.j/global-env))

(defn ^:private path-for [ns]
  (let [res ^java.net.URL (j.u/ns-url ns)]
    (assert res (str "Can't find " ns " in classpath"))
    (let [filename (str res)]
      (.getPath res))))

(defn transitive-deps
  "Like deps, but attempts to resolve transitive dependencies through
   function call boundaries."
  ([form] (transitive-deps form #{}))
  ([form seen]
   (env/ensure env
     (let [direct-deps (deps form)]
       (run! (comp a.j/analyze-ns symbol namespace symbol) direct-deps)
       (->> direct-deps
            (mapcat
             (fn [v]
               (if (contains? seen v)
                 []
                 (let [sym (symbol v)
                       ns (symbol (namespace sym))
                       v-ast (->> (get-in (env/deref-env) [::a.j/analyzed-clj (path-for ns)])
                                  (filter (every-pred (comp #{:def} :op)
                                                      (comp #{(symbol (name sym))} :name)))
                                  first)
                       transitive-deps (when v-ast
                                         (binding [*ns* (the-ns ns)]
                                           (transitive-deps (j.e/emit-form v-ast) (conj seen v))))]
                   (conj transitive-deps v)))))
            (into #{}))))))

(defn expansion-steps
  "Takes a form and returns a seq of all the expansion steps
   the compiler will apply, which includes macroexpansion, inlining,
   normalizations and more.
   If include-meta? is true, will include the expansion steps of
   metadata forms."
  ([form] (expansion-steps form false))
  ([form include-meta?]
   (let [a    (a.j/analyze form)
         c    (count (mapcat :raw-forms (ast/nodes a)))
         asts (loop [a a asts [] i 0]
                (if (< i c)
                  (let [extract-raw (fn [ast]
                                      (if-let [[r & rs] (seq (:raw-forms ast))]
                                        (reduced (assoc ast :raw r :raw-forms rs))
                                        ast))
                        a (unreduced (ast/prewalk a  extract-raw))]
                    (recur (ast/prewalk a #(dissoc % :raw)) (conj asts a) (inc i)))
                  (conj asts a)))]
     ((if include-meta? identity dedupe)
      (mapv mexpansions asts)))))

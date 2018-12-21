(ns clojure.tools.analyzer.jvm.deps
  (:require
   [clojure.tools.analyzer :as a]
   [clojure.tools.analyzer.jvm :as a.j]
   [clojure.tools.analyzer.ast :as ast]
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
  "Takes a form and returns a (non-transitive) set of the vars it depends on"
  [form]
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
    @deps))

(defn mexpansion-steps
  "Takes a form and returns a seq of all the macroexpansion steps
   the compiler will apply.
   If include-meta? is true, will include the macroexpansion steps of
   metadata forms."
  ([form] (mexpansion-steps form false))
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

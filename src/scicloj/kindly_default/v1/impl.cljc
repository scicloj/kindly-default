(ns scicloj.kindly-default.v1.impl
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]))

(defn form->kind [form]
  (when-let [m (some-> form meta)]
    (or (some->> m
                 :tag
                 resolve
                 deref
                 namespace
                 (= "kind"))
        (some->> m
                 keys
                 (filter #(-> %
                              namespace
                              (= "kind")))
                 first))))

(defn test? [value]
  (some-> value
          meta
          :test
          fn?))

(defn value->kind-by-logic [value]
  (cond
    ;; (and (vector? value)
    ;;      (-> value first keyword?))
    ;; :kind/hiccup
    ;;
    (test? value) :kind/test
    (var? value) :kind/var
    (map? value) :kind/map
    (set? value) :kind/set
    (vector? value) :kind/vector
    (sequential? value) :kind/seq
    ;;
    :else nil))

(defn value->kind [value]
  (or (-> value
          meta
          :kindly/kind)
      (-> value
          kindness/kind)
      (-> value
          value->kind-by-logic)))

(defn ->kind
  ([value form]
   (or (-> form
           form->kind)
       (-> value
           value->kind))))


(defn check-predicate-kinds [value predicate-kinds]
  (->> predicate-kinds
       (map (fn [[predicate k]]
              (when (predicate value)
                k)))
       (filter identity)
       first))

(def default-predicate-kinds
  [[(fn [v]
      (-> v type pr-str (= "tech.v3.dataset.impl.dataset.Dataset")))
    :kind/dataset]])


#?(:clj
   (extend-protocol kindness/Kindness
     java.awt.image.BufferedImage
     (kind [image]
       :kind/buffered-image)))


(->> [:kind/hidden :kind/pprint :kind/println
      :kind/test :kind/var
      :kind/map :kind/set :kind/vector :kind/seq
      :kind/table
      :kind/hiccup :kind/reagent
      :kind/vega :kind/vega-lite :kind/cytoscape :kind/echarts]
     (run! kindly/add-kind!))

(ns scicloj.kindly-default.v1.api
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]
            [scicloj.kindly-default.v1.impl :as impl]
            [clojure.test]))

(defn create-advisor
  ([]
   (create-advisor {:predicate-kinds impl/default-predicate-kinds}))
  ([{:keys [predicate-kinds]}]
   (fn [{:as context :keys [value form]}]
     (if (:kind context)
       context
       (assoc context
              :kind (or (impl/->kind value form)
                        (impl/check-predicate-kinds value
                                                    predicate-kinds)))))))

(defn setup! []
  (kindly/set-only-advisor! (create-advisor))
  :ok)


(defn is-> [value predicate & args]
  (clojure.test/is (apply predicate value args))
  value)

(defn code [code-string]
  (-> [code-string]
      (kindly/consider :kind/code)
      (vary-meta
       assoc
       :kindly/hide-code? true)))

(defn md [markdown-string]
  (-> [markdown-string]
      (kindly/consider :kind/md)
      (vary-meta
       assoc
       :kindly/hide-code? true)))

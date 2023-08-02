(ns scicloj.kindly-default.v1.api-test
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]
            [scicloj.kindly.v3.kind :as kind]
            [scicloj.kindly-default.v1.api :as api]
            [clojure.test :refer [deftest is]]))

(def default-advisor
  (api/create-advisor))

(deftest default-test
  (is (-> {:value 9}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [nil])))))

(deftype MyType1 [])
(extend-protocol kindness/Kindness
  MyType1
  (kind [this]
    :kind/mytestkind1))

(deftest type-with-user-defined-kindness-test
  (is (-> {:value (MyType1.)}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/mytestkind1])))))

(deftype MyType2 [])

(deftest type-without-user-defined-kindness-test
  (is (-> {:value (MyType2.)}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [nil])))))

(deftest nil-test
  (is (-> {:value nil}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [nil])))))

(deftest value-with-kind-metadata-test
  (is (-> {:value (-> {:some :data}
                      (with-meta {:kindly/kind :kind/mytestkind2}))}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/mytestkind2])))))

(deftest consider-test
  (is (-> {:some :data}
          (kindly/consider :kind/mytestkind3)
          meta
          :kindly/kind
          (= :kind/mytestkind3)))
  (is (-> {:value (-> {:some :data}
                      (kindly/consider :kind/mytestkind4))}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/mytestkind4])))))

(kindly/add-kind! :kind/mytestkind5)

(deftest add-kind-test
  (is (-> {:value (-> {:some :data}
                      kind/mytestkind5)}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/mytestkind5])))))

(deftest form-metadata-test
  (is (-> {:form (read-string "^:kind/mytestkind6 (+ 0 1 2)")
           :value 3}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/mytestkind6]))))
  (is (-> {:form (read-string "^{:kind/mytestkind7 true} (+ 0 1 2)")
           :value 3}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/mytestkind7])))))

;; (deftest hiccup-test
;;   (is (-> {:value [:h4 "hi"]}
;;           (kindly/advice [default-advisor])
;;           :kind
;;           (= :kind/hiccup))))

(deftest kind-by-logid-test
  (is (-> {:value #'clojure.core/reduce}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/var]))))
  (is (-> {:value #'default-test}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/test]))))
  (is (-> {:value {:x 9}}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/map]))))
  (is (-> {:value #{0 1 2}}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/set]))))
  (is (-> {:value [0 1 2]}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/vector]))))
  (is (-> {:value '(0 1 2)}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/seq]))))
  (is (-> {:value (range 3)}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/seq])))))

(import java.awt.image.BufferedImage)

(deftest image-test
  (is (-> {:value (BufferedImage. 4 4 BufferedImage/TYPE_INT_RGB)}
          (kindly/advice [default-advisor])
          (->> (map :kind)
               (= [:kind/buffered-image])))))

(deftest predicate-test
  (let [advisor (api/create-advisor
                 {:predicate-kinds [[(fn [v] (= v 3))
                                     :kind/three]
                                    [(fn [v] (-> v type pr-str (= "java.lang.String")))
                                     :kind/string]
                                    [vector?
                                     :kind/vector1]]})]
    (is (-> {:value 3}
            (kindly/advice [advisor])
            (->> (map :kind)
                 (= [:kind/three]))))
    (is (-> {:value "abcd"}
            (kindly/advice [advisor])
            (->> (map :kind)
                 (= [:kind/string]))))
    (is (-> {:value [3]}
            (kindly/advice [advisor])
            (->> (map :kind)
                 (= [:kind/vector1]))))))

(deftest added-kinds-test
  (is
   (->> [[kind/hidden :kind/hidden]
         [kind/pprint :kind/pprint]
         [kind/println :kind/println]
         [kind/hiccup :kind/hiccup]
         [kind/vega :kind/vega]
         [kind/vega-lite :kind/vega-lite]
         [kind/table :kind/table]]
        (every? (fn [[f k]]
                  (-> {:value (f [:abcd])}
                      (kindly/advice [default-advisor])
                      (->> (map :kind)
                           (= [k]))))))))

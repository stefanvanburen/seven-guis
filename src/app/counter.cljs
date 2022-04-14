(ns app.counter
  {:clj-kondo/config '{:lint-as {reagent.core/with-let clojure.core/let}}}
  (:require [reagent.core :as r]))


(defn counter
  "The task is to build a frame containing a label or read-only textfield T and
  a button B. Initially, the value in T is '0' and each click of B increases
  the value in T by one."
  []
  (r/with-let [count (r/atom 0)]
    [:section
      [:h2 "Counter"]
      [:output {:for "counter"} @count]
      [:button {:on-click #(swap! count inc)
                :id "counter"}
       "Count"]]))

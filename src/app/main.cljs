(ns app.main
  (:require
    [reagent.dom :as rdom]
    [reagent.core :as r]))

(defn counter []
  (let [cnt (r/atom 0)]
    (fn []
      [:div
        [:input {:type "text" :readonly true :value @cnt}]
        [:button {:on-click #(swap! cnt inc)}
         "Count"]])))

(defn app []
  [:main
   [counter]])


(defn ^:export main! []
  (rdom/render [app] (js/document.querySelector "#app")))

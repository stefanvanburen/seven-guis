(ns app.main
  (:require
    [reagent.dom :as rdom]
    [reagent.core :as r]))

(defn counter []
  (let [cnt (r/atom 0)]
    (fn []
      [:section
        [:h2 "Counter"]
        [:input {:type "text" :readOnly true :value @cnt}]
        [:button {:on-click #(swap! cnt inc)}
         "Count"]])))

(defn temperature-converter []
  (let [temp (r/atom {}) ; starts off empty
        to-fahrenheit (fn [celsius]
                        (+ 32 (* 9 (/ celsius 5))))
        to-celsius (fn [fahrenheit]
                     (* (/ 5 9) (- fahrenheit 32)))]
    (fn []
      [:section
       [:h2 "Temperature Converter"]

       [:input {:type "number"
                :value (:celsius @temp)
                :id "celsius"
                :on-change (fn [e]
                             (let [celsius (-> e .-target .-value)]
                               (reset! temp {:celsius celsius
                                             :fahrenheit (to-fahrenheit celsius)})))}]


       [:label {:for "celsius"} "Celsius"]

       [:span " = "]

       [:input {:type "number"
                :value (:fahrenheit @temp)
                :id "fahrenheit"
                :on-change (fn [e]
                             (let [fahrenheit (-> e .-target .-value)]
                               (reset! temp {:celsius (to-celsius fahrenheit)
                                             :fahrenheit fahrenheit})))}]
       [:label {:for "fahrenheit"} "Fahrenheit"]])))

(defn app []
  [:main
   [:h1 "Seven GUIs"]
   [counter]
   [temperature-converter]])


(defn ^:export main! []
  (rdom/render [app] (js/document.querySelector "#app")))

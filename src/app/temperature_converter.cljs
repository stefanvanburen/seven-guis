(ns app.temperature-converter
  {:clj-kondo/config '{:lint-as {reagent.core/with-let clojure.core/let}}}
  (:require [reagent.core :as r]))

(defn temperature-converter
  "The task is to build a frame containing two textfields Tc and Tf
  representing the temperature in Celsius and Fahrenheit, respectively.
  Initially, both Tc and Tf are empty. When the user enters a numerical value
  into Tc the corresponding value in Tf is automatically updated and vice
  versa. When the user enters a non-numerical string into Tc the value in Tf is
  _not_ updated and vice versa. The formula for converting a temperature C in
  Celsius into a temperature F in Fahrenheit is _C = (F-32) * (5/9)_ and the
  dual direction is _F = C * (9/5) + 32_."
  []
  (r/with-let [temp (r/atom {}) ; starts off empty
               to-fahrenheit (fn [celsius]
                               (+ 32 (* 9 (/ celsius 5))))
               to-celsius (fn [fahrenheit]
                            (* (/ 5 9) (- fahrenheit 32)))]

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
     [:label {:for "fahrenheit"} "Fahrenheit"]]))

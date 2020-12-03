(ns app.main
  (:require [reagent.dom :as rdom]
            [reagent.core :as r]
            [cljs.reader :refer [parse-timestamp]]))

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

(defn flight-booker
  "The task is to build a frame containing a combobox C with the two options
  “one-way flight” and “return flight”, two textfields T1 and T2 representing
  the start and return date, respectively, and a button B for submitting the
  selected flight. T2 is enabled iff C’s value is “return flight”. When C has
  the value “return flight” and T2’s date is strictly before T1’s then B is
  disabled. When a non-disabled textfield T has an ill-formatted date then T is
  colored red and B is disabled. When clicking B a message is displayed
  informing the user of his selection (e.g. “You have booked a one-way flight
  on 04.04.2014.”). Initially, C has the value “one-way flight” and T1 as well
  as T2 have the same (arbitrary) date (it is implied that T2 is disabled)."
  []
  (let [state (r/atom {:selected "one-way"
                       :start-date "2020-08-22"
                       :start-date-valid? true
                       :end-date "2020-08-22"
                       :end-date-valid? true})]

    (fn []
      [:section
       [:h2 "Flight Booker"]
       [:select {:on-change #(swap! state assoc :selected (-> % .-target .-value))
                 :value (:selected @state)}
        [:option {:value "one-way"
                  :selected (= (:selected @state) "one-way")}
         "one-way flight"]

        [:option {:value "return"
                  :selected (= (:selected @state) "return")}
         "return flight"]]

       [:input {:type "date"
                :value (:start-date @state)
                :style {:background (when-not (:start-date-valid? @state)
                                      "red")}
                :on-change (fn [e]
                             (let [value (-> e .-target .-value)
                                   ; try to parse the timestamp.
                                   ; If it's invalid, set ts to nil
                                   ts (try (parse-timestamp value)
                                           (catch :default _ nil))]
                               (swap! state assoc :start-date-valid? (not (nil? ts)))
                               (swap! state assoc :start-date value)))}]

       [:input (merge {:type "date"
                       :value (:end-date @state)
                       :style {:background (when-not (:end-date-valid? @state)
                                             "red")}
                       :on-change (fn [e]
                                    (let [value (-> e .-target .-value)
                                          ; try to parse the timestamp.
                                          ; If it's invalid, set ts to nil
                                          ts (try (parse-timestamp value)
                                                  (catch :default _ nil))]
                                      (swap! state assoc :end-date-valid? (not (nil? ts)))
                                      (swap! state assoc :end-date value)))}
                      ; disable the end date for a one-way flight
                      (when (= (:selected @state) "one-way")
                        {:disabled true}))]

       [:button (merge {:type "button"
                        :on-click #(swap! state assoc :booking-message (if (= (:selected @state) "one-way")
                                                                         (str "You have booked a one-way flight on " (:start-date @state))
                                                                         (str "You have booked a round-trip flight - out on " (:start-date @state) ", return on " (:end-date @state))))}
                       ; if either date is not a valid date, disable booking
                       (if-not (and (:end-date-valid? @state) (:start-date-valid? @state))
                         {:disabled true}
                         ; otherwise, we know both dates are valid
                         (let [start-date (parse-timestamp (:start-date @state))
                               end-date (parse-timestamp (:end-date @state))]
                           ; if the end date is before the start date, disable
                           (when (< end-date start-date)
                             {:disabled true}))))

        "Book"]
       [:div (:booking-message @state)]])))


(defn app []
  [:main
   [:h1 "Seven GUIs"]
   [counter]
   [temperature-converter]
   [flight-booker]])


(defn ^:export main! []
  (rdom/render [app] (js/document.querySelector "#app")))

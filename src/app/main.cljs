(ns app.main
  (:require [reagent.dom :as rdom]
            [reagent.core :as r]
            [clojure.string :refer [starts-with?]]
            [cljs.reader :refer [parse-timestamp]]))

(defn counter
  "The task is to build a frame containing a label or read-only textfield T and
  a button B. Initially, the value in T is '0' and each click of B increases
  the value in T by one."
  []
  (let [cnt (r/atom 0)]
    (fn []
      [:section
        [:h2 "Counter"]
        [:input {:type "text" :readOnly true :value @cnt}]
        [:button {:on-click #(swap! cnt inc)}
         "Count"]])))

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
  'one-way flight' and 'return flight', two textfields T1 and T2 representing
  the start and return date, respectively, and a button B for submitting the
  selected flight. T2 is enabled iff C’s value is 'return flight'. When C has
  the value 'return flight' and T2’s date is strictly before T1’s then B is
  disabled. When a non-disabled textfield T has an ill-formatted date then T is
  colored red and B is disabled. When clicking B a message is displayed
  informing the user of his selection (e.g. 'You have booked a one-way flight
  on 04.04.2014.'). Initially, C has the value 'one-way flight' and T1 as well
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
                                   ;; try to parse the timestamp.
                                   ;; If it's invalid, set ts to nil
                                   ts (try (parse-timestamp value)
                                           (catch :default _ nil))]
                               (swap! state assoc :start-date-valid? (not (nil? ts)))
                               (swap! state assoc :start-date value)))}]

       [:input {:type "date"
                :value (:end-date @state)
                :style {:background (when-not (:end-date-valid? @state)
                                      "red")}
                :on-change (fn [e]
                             (let [value (-> e .-target .-value)
                                   ;; try to parse the timestamp.
                                   ;; If it's invalid, set ts to nil
                                   ts (try (parse-timestamp value)
                                           (catch :default _ nil))]
                               (swap! state assoc :end-date-valid? (not (nil? ts)))
                               (swap! state assoc :end-date value)))
                ;; disable the end date for a one-way flight
                :disabled (= (:selected @state) "one-way")}]

       [:button {:type "button"
                 :on-click #(swap! state
                                   assoc :booking-message
                                   (if (= (:selected @state) "one-way")
                                     (str "You have booked a one-way flight on " (:start-date @state))
                                     (str "You have booked a round-trip flight - out on " (:start-date @state) ", return on " (:end-date @state))))
                 :disabled
                 ;; if either date is not a valid date, disable booking
                 (if-not (and (:end-date-valid? @state) (:start-date-valid? @state))
                   true
                   ;; otherwise, we know both dates are valid
                   (let [start-date (parse-timestamp (:start-date @state))
                         end-date (parse-timestamp (:end-date @state))]
                     ;; if the end date is before the start date, disable
                     (< end-date start-date)))}

        "Book"]
       [:div (:booking-message @state)]])))

(defn timer
  "The task is to build a frame containing a gauge G for the elapsed time e, a
  label which shows the elapsed time as a numerical value, a slider S by which
  the duration d of the timer can be adjusted while the timer is running and a
  reset button R. Adjusting S must immediately reflect on d and not only when S
  is released. It follows that while moving S the filled amount of G will
  (usually) change immediately. When e ≥ d is true then the timer stops (and G
  will be full). If, thereafter, d is increased such that d > e will be true
  then the timer restarts to tick until e ≥ d is true again. Clicking R will
  reset e to zero."
  []
  (let [initial-state {:elapsed-tenth-seconds 0
                       :paused false
                       :max 100}
        state (r/atom initial-state)
        interval-func (fn [] (js/setInterval #(swap! state update :elapsed-tenth-seconds inc) 100))]
    ;; call the interval-func, getting the clear-interval fn we need to stop
    ;; the counter momentarily if it hits the top.
    (swap! state assoc :interval-id (interval-func))
    (fn []
      (when (>= (:elapsed-tenth-seconds @state) (:max @state))
        (swap! state assoc :paused true)
        (js/clearInterval (:interval-id @state)))
      [:section
       [:h2 "Timer"]

       [:fieldset
        [:label {:for "elapsed-time"} "Elapsed Time:"]
        [:progress {:id "elapsed-time" :max (:max @state) :value (:elapsed-tenth-seconds @state)}]]

       [:span (str (/ (:elapsed-tenth-seconds @state) 10) "s")]

       [:fieldset
        [:label {:for "duration"} "Duration"]
        [:input {:type "range" :id "duration" :min 0 :max 200 :value (:max @state)
                 :on-change #(do
                               ;; if we're paused and the duration slider is
                               ;; getting bigger, re-enable the timer.
                               (when (and (:paused @state) (> (-> % .-target .-value) (:max @state)))
                                 (swap! state assoc :paused false)
                                 (swap! state assoc :interval-id (interval-func)))
                               (swap! state assoc :max (js/parseInt (-> % .-target .-value))))}]]

       [:button {:type "button"
                 :on-click #(do
                              ;; if we're paused, kick things off again
                              (when (:paused @state)
                                (swap! state assoc :interval-id (interval-func)))
                              ;; want to keep the interval-id and max, but reset
                              ;; the rest to the initial values
                              (reset! state (conj initial-state {:interval-id (:interval-id @state)
                                                                 :max (:max @state)})))}

        "Reset"]])))

(defn crud
  "The task is to build a frame containing the following elements: a textfield
  Tprefix, a pair of textfields Tname and Tsurname, a listbox L, buttons BC, BU
  and BD and the three labels as seen in the screenshot. L presents a view of
  the data in the database that consists of a list of names. At most one entry
  can be selected in L at a time. By entering a string into Tprefix the user
  can filter the names whose surname start with the entered prefix—this should
  happen immediately without having to submit the prefix with enter. Clicking
  BC will append the resulting name from concatenating the strings in Tname and
  Tsurname to L. BU and BD are enabled iff an entry in L is selected. In
  contrast to BC, BU will not append the resulting name but instead replace the
  selected entry with the new name. BD will remove the selected entry. The
  layout is to be done like suggested in the screenshot. In particular, L must
  occupy all the remaining space."
  []
  (let [state (r/atom {:selected ""
                       :people {}
                       :filter ""
                       :name ""
                       :surname ""})]
    (fn []
      [:section
       [:h2 "CRUD"]
       ;; filter
       [:fieldset {:style {:border "none"}}
        [:label {:for "prefix"} "Filter prefix:"]
        [:input {:type "text" :id "prefix" :value (:filter @state)
                 :on-change #(swap! state assoc :filter (-> % .-target .-value))}]]

       ;; name select
       [:select {:size 5
                 :style {:width "50%"}
                 :on-change #(swap! state assoc :selected (-> % .-target .-value))}
        (let [prefix-filter (:filter @state)
              people (filter (fn [[_ person]]
                               (starts-with? (:surname person) prefix-filter))
                            (:people @state))]
         (for [[uuid person] people]
           ^{:key uuid} [:option {:value uuid} (str (:surname person) ", " (:name person))]))]

       ;; name input
       [:fieldset {:style {:border "none"}}
        [:label {:for "name"} "Name:"]
        [:input {:type "text" :id "name" :value (:name @state)
                 :on-change #(swap! state assoc :name (-> % .-target .-value))}]]
       [:fieldset {:style {:border "none"}}
        [:label {:for "surname"} "Surname:"]
        [:input {:type "text" :id "surname" :value (:surname @state)
                 :on-change #(swap! state assoc :surname (-> % .-target .-value))}]]

       ;; actions
       [:button {:type "button"
                 :on-click #(swap! state assoc-in [:people (str (random-uuid))] {:name (:name @state)
                                                                                 :surname (:surname @state)})}
        "Create"]
       [:button {:type "button"
                 :on-click #(swap! state assoc-in [:people (:selected @state)] {:name (:name @state)
                                                                                :surname (:surname @state)})
                 :disabled (= "" (:selected @state))}
        "Update"]
       [:button {:type "button"
                 :on-click #(do
                              (swap! state assoc :people (dissoc (:people @state) (:selected @state)))
                              (swap! state assoc :selected ""))
                 :disabled (= "" (:selected @state))}
        "Delete"]])))

(defn app []
  [:main
   [:h1 "Seven GUIs"]
   [counter]
   [temperature-converter]
   [flight-booker]
   [timer]
   [crud]])


(defn ^:export main! []
  (rdom/render [app] (js/document.querySelector "#app")))

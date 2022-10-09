(ns app.timer
  (:require [reagent.core :as r]))

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

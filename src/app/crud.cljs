(ns app.crud
  {:clj-kondo/config '{:lint-as {reagent.core/with-let clojure.core/let}}}
  (:require [reagent.core :as r]
            [clojure.string :refer [starts-with?]]))


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
  (r/with-let [state (r/atom {:selected ""
                              :people {}
                              :filter ""
                              :name ""
                              :surname ""})]
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
      "Delete"]]))

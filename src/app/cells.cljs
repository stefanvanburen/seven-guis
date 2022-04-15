(ns app.cells
  "The task is to create a simple but usable spreadsheet application. The
  spreadsheet should be scrollable. The rows should be numbered from 0 to 99
  and the columns from A to Z. Double-clicking a cell C lets the user change
  C’s formula. After having finished editing the formula is parsed and
  evaluated and its updated value is shown in C. In addition, all cells which
  depend on C must be reevaluated. This process repeats until there are no more
  changes in the values of any cell (change propagation). Note that one should
  not just recompute the value of every cell but only of those cells that
  depend on another cell’s changed value. If there is an already provided
  spreadsheet widget it should not be used. Instead, another similar widget
  (like JTable in Swing) should be customized to become a reusable spreadsheet
  widget.

  Cells is a more authentic and involved task that tests if a particular
  approach also scales to a somewhat bigger application. The two primary
  GUI-related challenges are intelligent propagation of changes and widget
  customization. Admittedly, there is a substantial part that is not
  necessarily very GUI-related but that is just the nature of a more authentic
  challenge. A good solution’s change propagation will not involve much effort
  and the customization of a widget should not prove too difficult. The
  domain-specific code is clearly separated from the GUI-specific code. The
  resulting spreadsheet widget is reusable.

  Cells is directly inspired by the SCells spreadsheet example from the book
  Programming in Scala. Please refer to the book (or the implementations in
  this repository) for more details especially with respect to the not directly
  GUI-related concerns like parsing and evaluating formulas and the precise
  syntax and semantics of the spreadsheet language."
  {:clj-kondo/config '{:lint-as {reagent.core/with-let clojure.core/let}}}
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

(def columns (take 10 "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
(def rows (take 10 (range 100)))

(defn cell-key [c r]
  (str c r))

(def initial-cells
  "'A1', 'A2', ..., 'Z100'"
  (let [ks (for [c columns]
             (for [r rows]
               (cell-key c r)))
        vs {:value "hey"}]
    (zipmap (flatten ks) (repeat vs))))

(def state (r/atom {:cells initial-cells
                    :editing? ""}))

(defn- inp [location]
  [:input {:type "text"
           :value (:value (get (:cells @state) location))
           :on-change (fn [e]
                        (swap! state
                               update-in [:cells location]
                               assoc :value (-> e .-target .-value)))
           ;; what happens when we de-select the input? recalculate?
           :on-blur #(swap! state assoc :editing? "")}])

;; We need this so that when on-double-click switches from output -> input, the
;; input is focused.
;; TODO: can we do this without a separate method?
(def cell-input (with-meta inp
                           {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn- cell-output [location]
  [:output {:on-double-click (fn [_] (swap! state assoc :editing? location))}
   (:value (get (:cells @state) location))])

(defn- table-header []
  [:thead
   [:tr
    [:th] ;; empty, for top left - TODO: might be a11y issue?
    (for [c columns]
      ^{:key c}
      [:th c])]])

(defn- table-body []
  [:tbody
   (doall
    (for [r rows]
      ^{:key r}
      [:tr
       [:th r] ;; left column
       (doall
        (for [c columns]
          ^{:key (cell-key c r)}
          [:td
           (if (not= (:editing? @state) (cell-key c r))
            [cell-output (cell-key c r)]
            [cell-input (cell-key c r)])]))]))])

(defn cells
  []
  [:section
   [:h2 "Cells"]
   [:table
    [table-header]
    [table-body]]])

(ns app.circle-drawer
  {:clj-kondo/config '{:lint-as {reagent.core/with-let clojure.core/let}}}
  (:require [reagent.core :as r]))

(defn circle-drawer
  "The task is to build a frame containing an undo and redo button as well as a
  canvas area underneath. Left-clicking inside an empty area inside the canvas
  will create an unfilled circle with a fixed diameter whose center is the
  left-clicked point. The circle nearest to the mouse pointer such that the
  distance from its center to the pointer is less than its radius, if it
  exists, is filled with the color gray. The gray circle is the selected circle
  C. Right-clicking C will make a popup menu appear with one entry “Adjust
  diameter..”. Clicking on this entry will open another frame with a slider
  inside that adjusts the diameter of C. Changes are applied immediately.
  Closing this frame will mark the last diameter as significant for the
  undo/redo history. Clicking undo will undo the last significant change (i.e.
  circle creation or diameter adjustment). Clicking redo will reapply the last
  undoed change unless new changes were made by the user in the meantime."
  []
  (let [default-radius 20
        state (r/atom {:adjusting? false
                       :previous-radius nil
                       ;; add the initial empty entry
                       :history [{}]})]

    (r/create-class
      {:display-name "circle-drawer"

       :component-did-update
       (fn []
         (let [canvas (:canvas @state)
               ctx (.getContext canvas "2d")]
           ;; clear out previous canvas
           (.clearRect ctx 0 0 (.-width canvas) (.-height canvas))
           ;; draw each circle
           (doseq [[[x y] {:keys [radius selected?]}] (peek (:history @state))]
             (.beginPath ctx)
             (.arc ctx x y radius 0 (* 2 (.-PI js/Math)))
             (.stroke ctx)
             (when selected?
               (set! (.-fillStyle ctx) "grey")
               (.fill ctx)))))

       :reagent-render
       (fn []
         [:section
          [:h2 "Circle Drawer"]

          [:fieldset {:style {:border "none"}}

           [:button {:type "button"
                     :on-click #(do
                                  ;; always turn off "adjusting"
                                  (swap! state assoc :adjusting? false)
                                  (swap! state assoc :redo (peek (:history @state)))
                                  (swap! state assoc :history (pop (:history @state))))
                     :disabled (= (count (:history @state)) 0)}
            "Undo"]

           [:button {:type "button"
                     :disabled (= (:redo @state) nil)
                     :on-click #(do
                                  ;; always turn off "adjusting"
                                  (swap! state assoc :adjusting? false)
                                  (swap! state assoc :history (conj (:history @state) (:redo @state)))
                                  (swap! state assoc :redo nil))}
            "Redo"]]
          (when (:adjusting? @state)
            [:fieldset
              (let [[[x y] {:keys [radius]}] (first (filter #(:selected? (val %)) (peek (:history @state))))]
                [:<>
                  [:label {:for "diameter-range"} (str "Adjust diameter of circle at (" x ", " y ").")]
                  [:input#diameter-range
                   {:type "range"
                    :min 1
                    :max 100
                    :value radius
                    :on-change
                    #(swap! state
                            update-in
                            [:history (dec (count (:history @state))) [x y]]
                            assoc :radius (int (-> % .-target .-value)))}]
                  [:button
                   {:type "button"
                    :on-click
                    #(do
                       (swap! state assoc :adjusting? false)
                       (swap! state assoc :redo nil)
                       ;; strangest part of this implementation: because we've
                       ;; held on to the radius of the circle being adjusted,
                       ;; but the adjustment is taking place IN place, after we
                       ;; "close" the "popup", we have to insert a history
                       ;; entry containing that previous radius before the
                       ;; current state, but after all the other history.
                       (swap! state
                              assoc :history
                              (conj
                                ;; previous history
                                (pop (:history @state))
                                ;; current history, with the previous radius
                                (assoc-in (peek (:history @state)) [[x y] :radius] (:previous-radius @state))
                                ;; current history
                                (peek (:history @state)))))}
                   "close"]])])

          [:canvas
           {:style {:border "1px solid black"}
            ;; associate this DOM node in state
            :ref #(swap! state assoc :canvas %)
            ;; handles right clicks
            :on-context-menu
            (fn [e]
              (.preventDefault e)
              (let [ctx (.getContext (:canvas @state) "2d")
                    nativeEvent (.-nativeEvent e)
                    x (.-offsetX nativeEvent)
                    y (.-offsetY nativeEvent)
                    radius 20]
                ;; create the path that the new circle would take.
                (.beginPath ctx)
                (.arc ctx x y radius 0 (* 2 (.-PI js/Math)))
                (doseq [[[x y] {:keys [radius selected?]}] (peek (:history @state))]
                  (when (and selected? (.isPointInPath ctx x y))
                    (swap! state assoc :previous-radius radius)
                    (swap! state assoc :adjusting? true)))))

            :on-click
            (fn [e]
              (swap! state assoc :adjusting? false)
              (let [ctx (.getContext (:canvas @state) "2d")
                    nativeEvent (.-nativeEvent e)
                    clickX (.-offsetX nativeEvent)
                    clickY (.-offsetY nativeEvent)
                    click-inside-circle? (atom false)]
                (doseq [[[x y] {:keys [radius]}] (peek (:history @state))]
                  (.beginPath ctx)
                  (.arc ctx x y radius 0 (* 2 (.-PI js/Math)))
                  (let [point-in-path? (.isPointInPath ctx clickX clickY)]
                    (swap! state
                           update-in
                           [:history (dec (count (:history @state))) [x y]]
                           ;; only "select" a circle if the click is within the
                           ;; path of the circle and we haven't already found a
                           ;; circle that encompasses this click (this can
                           ;; happen if a circle is resized to encompass
                           ;; another circle).
                           assoc :selected? (and point-in-path? (not @click-inside-circle?)))
                    ;; so that we only "select" one circle, keep track if we've already found one.
                    (when point-in-path?
                      (reset! click-inside-circle? true))))

                ;; if the click isn't inside a circle, add a new one.
                (when-not @click-inside-circle?
                  (swap! state assoc :redo nil)
                  (let [history (:history @state)]
                    (swap! state
                           assoc :history
                          (conj
                            history
                            (assoc
                              (peek history)
                              [clickX clickY]
                              {:radius default-radius
                               :selected? false})))))))}]])})))

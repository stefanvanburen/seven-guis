(ns app.main
  {:clj-kondo/config '{:lint-as {reagent.core/with-let clojure.core/let}}}
  (:require [reagent.dom :as rdom]
            [app.counter :refer [counter]]
            [app.temperature-converter :refer [temperature-converter]]
            [app.flight-booker :refer [flight-booker]]
            [app.timer :refer [timer]]
            [app.crud :refer [crud]]
            [app.circle-drawer :refer [circle-drawer]]))

(defn app []
  [:main
   [:h1 "Seven GUIs"]
   [:a {:href "https://github.com/stefanvanburen/seven-guis"} "Source Code"]
   [counter]
   [temperature-converter]
   [flight-booker]
   [timer]
   [crud]
   [circle-drawer]])

(defn ^:export main! []
  (rdom/render [app] (js/document.querySelector "#app")))

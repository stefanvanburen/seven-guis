(ns app.main
  (:require [reagent.dom.client :as rdomc]
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
  (let [root (rdomc/create-root (js/document.getElementById "app"))]
    (rdomc/render root [app])))

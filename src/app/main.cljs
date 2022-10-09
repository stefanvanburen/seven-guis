(ns app.main
  (:require [reagent.dom :as rdom]
            [app.counter :refer [counter]]
            [app.temperature-converter :refer [temperature-converter]]
            [app.flight-booker :refer [flight-booker]]
            [app.timer :refer [timer]]
            [app.crud :refer [crud]]
            [app.circle-drawer :refer [circle-drawer]]
            [app.cells :refer [cells]]))

(defn app []
  [:main
   [:h1 "Seven GUIs"]
   [:a {:href "https://github.com/stefanvanburen/seven-guis"} "Source Code"]
   [counter]
   ; [temperature-converter]
   ; [flight-booker]
   ; [timer]
   ; [crud]
   ; [circle-drawer]
   [cells]])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^:export main! []
  (rdom/render [app] (js/document.querySelector "#app")))

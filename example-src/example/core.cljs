(ns example.core
  (:require [reagent.core :as reagent]
            [re-fill.core :as re-fill]))

(defn hello-view []
  [:div
   [:h1 re-fill/test-string]
   [:h3 "Edit this and watch it change!"]])

(reagent/render-component [hello-view]
                          (. js/document (getElementById "app")))

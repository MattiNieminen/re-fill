(ns re-fill.debounce
  (:require [re-frame.core :as rf]
            [re-frame.db :as db]))

(rf/reg-fx
 :re-fill/debounce
 (fn [{:keys [key event timeout]}]
   (js/clearTimeout (get-in @db/app-db [:re-fill/debounce key]))
   (swap! db/app-db
          assoc-in
          [:re-fill/debounce key]
          (js/setTimeout (fn []
                           (rf/dispatch event)
                           (swap! db/app-db
                                  assoc
                                  :re-fill/debounce
                                  (dissoc (:re-fill/debounce @db/app-db) key)))
                         (or timeout 400)))))

(rf/reg-event-fx
 :re-fill/debounce
 (fn [_ [_ options]]
   {:re-fill/debounce options}))

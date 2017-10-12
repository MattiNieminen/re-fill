(ns re-fill.debounce
  (:require [re-frame.core :as rf]
            [re-frame.db :as db]))

(rf/reg-fx
 :re-fill/debounce
 (fn [{:keys [id event timeout]}]
   (let [id (or id (first event))]
     (js/clearTimeout (get-in @db/app-db [:re-fill/debounce id]))
     (swap! db/app-db
            assoc-in
            [:re-fill/debounce id]
            (js/setTimeout (fn []
                             (rf/dispatch event)
                             (swap! db/app-db
                                    assoc
                                    :re-fill/debounce
                                    (dissoc (:re-fill/debounce @db/app-db) id)))
                           (or timeout 400))))))

(rf/reg-fx
 :re-fill/stop-debounce
 (fn [id]
   (js/clearTimeout (get-in @db/app-db [:re-fill/debounce id]))
   (swap! db/app-db
          assoc
          :re-fill/debounce
          (dissoc (:re-fill/debounce @db/app-db) id))))

(rf/reg-event-fx
 :re-fill/debounce
 (fn [_ [_ options]]
   {:re-fill/debounce options}))

(rf/reg-event-fx
 :re-fill/stop-debounce
 (fn [_ [_ id]]
   {:re-fill/stop-debounce id}))

(rf/reg-sub
 :re-fill/debounce
 (fn [db _]
   (:re-fill/debounce db)))

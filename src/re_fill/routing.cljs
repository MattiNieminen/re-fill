(ns re-fill.routing
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as rf]
            [re-frame.db :as db]))

(rf/reg-fx
 :re-fill/init-routing
 (fn [_]
   (let [pushy-instance (get-in @db/app-db [:re-fill/routing :pushy-instance])]
     (if-not pushy-instance
       (->> (pushy/pushy #(rf/dispatch [:re-fill/change-view %]) identity)
            (swap! db/app-db assoc-in [:re-fill/routing :pushy-instance])
            :re-fill/routing
            :pushy-instance
            pushy/start!)))))

(rf/reg-event-fx
 :re-fill/init-routing
 (fn [{:keys [db]} [_ routes]]
   {:db (assoc-in db [:re-fill/routing :routes] routes)
    :re-fill/init-routing nil}))

(rf/reg-event-fx
 :re-fill/change-view
 (fn [{:keys [db]} [_ token]]
   (let [{:keys [routes]} (:re-fill/routing db)
         bidi-match (bidi/match-route routes token)]
     {:db (assoc-in db [:re-fill/routing :bidi-match] bidi-match)
      :dispatch [(:handler bidi-match) bidi-match]})))

(rf/reg-fx
 :re-fill/navigate
 (fn [{:keys [pushy-instance routes bidi-args]}]
   (let [path (apply bidi/path-for routes bidi-args)]
     (if path
       (pushy/set-token! pushy-instance path)
       (js/console.error "No matching route for" bidi-args)))))

(rf/reg-event-fx
 :re-fill/navigate
 (fn [{:keys [db]} [_ bidi-args]]
   (let [{:keys [pushy-instance routes]} (:re-fill/routing db)]
     {:re-fill/navigate {:pushy-instance pushy-instance
                         :routes routes
                         :bidi-args bidi-args}})))

(rf/reg-sub
 :re-fill/routing
 (fn [db _]
   (:re-fill/routing db)))

(defn routed-view
  [views]
  (let [r @(rf/subscribe [:re-fill/routing])
        component (or (get views (get-in r [:bidi-match :handler]))
                      (:else views))]
    [component]))

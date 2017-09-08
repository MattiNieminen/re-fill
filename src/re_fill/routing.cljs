(ns re-fill.routing
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(defonce state (atom nil))

(rf/reg-fx
 :re-fill/init-routing*
 (fn [routes]
   (when-let [{:keys [pushy-instance]} @state]
     (pushy/stop! pushy-instance))
   (->> (pushy/pushy #(rf/dispatch [:re-fill/change-view %]) (partial bidi/match-route routes))
        (swap! state assoc :routes routes :pushy-instance)
        :pushy-instance
        pushy/start!)))

(rf/reg-event-fx
 :re-fill/init-routing
 (fn [_ [_ routes]]
   {:re-fill/init-routing* routes}))

(rf/reg-event-fx
 :re-fill/change-view
 (fn [{:keys [db]} [_ bidi-match]]
   {:db (assoc db :re-fill/routing {:bidi-match bidi-match})
    :dispatch [(:handler bidi-match) bidi-match]}))

(rf/reg-fx
 :re-fill/navigate*
 (fn [bidi-args]
   (let [{:keys [pushy-instance routes]} @state
         path (apply bidi/path-for routes bidi-args)]
     (if path
       (pushy/set-token! pushy-instance path)
       (js/console.error "No matching route for" bidi-args)))))

(rf/reg-event-fx
 :re-fill/navigate
 (fn [{:keys [db]} [_ bidi-args extra-params]]
   {:db (assoc-in db [:re-fill/routing :extra-params] extra-params)
    :re-fill/navigate* bidi-args}))

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

(ns example.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [re-fill.routing :as routing]
            [re-fill.notifications :as notifications]
            [re-fill.debounce :as debounce]))

;;
;; Bidi-routes
;;

(def routes ["/" {"" :routes/home
                  ["page/" :id] :routes/page}])

;;
;; Views
;;

(defn controls-view
  []
  (let [debounce @(rf/subscribe [:re-fill/debounce])]
    [:div.controls
     [:a.controls__a {:href "/"} "Home"]
     [:a.controls__a {:href "/page/1"} "Page 1 (by link)"]
     [:a.controls__a {:href "/page/2"} "Page 2 (by link)"]
     [:button.controls__button
      {:on-click #(rf/dispatch [:re-fill/navigate [:routes/page :id 2]])}
      "Page 2 (by dispatch)"]
     [:button.controls__button
      {:on-click (fn [_] (rf/dispatch [:re-fill/notify
                                      {:type :success
                                       :content "Success!"}
                                      {:hide-after 3000}]))}
      "Notify success!"]
     [:button.controls__button
      {:on-click (fn [_] (rf/dispatch [:re-fill/notify
                                      {:type :warning
                                       :content "Warning!"}]))}
      "Notify warning!"]
     [:button.controls__button
      {:on-click (fn [_] (rf/dispatch [:re-fill/debounce
                                      {:id :test-notify
                                       :event [:re-fill/notify
                                               {:type :success
                                                :content "From debounce!"}
                                               {:hide-after 3000}]
                                       :timeout 1000}]))}
      "Notify with debounce"]
     [:button.controls__button
      {:on-click (fn [_] (rf/dispatch [:re-fill/stop-debounce :test-notify]))
       :disabled (not (:test-notify debounce))}
      "Stop debounce"]
     [:button.controls__button
      {:on-click (fn [_] (rf/dispatch [:re-fill/refresh-page]))}
      "Refresh Page"]]))

(defn notifications-view
  []
  (let [notifications @(rf/subscribe [:re-fill/notifications])]
    [:div.notifications
     (for [{:keys [id type content]} notifications]
       [:div.notification
        {:key id
         :class (case type
                  :success "notification--success"
                  :warning "notification--warning")}
        [:span.notification__span content]
        [:button.notification__button
         {:on-click #(rf/dispatch [:re-fill/delete-notification id])}
         "x"]])]))

(defn home-view
  []
  [:h1 "This is home view"])

(defn page-view
  []
  (let [{:keys [bidi-match]} @(rf/subscribe [:re-fill/routing])]
    [:h1 (str "This is page " (get-in bidi-match [:route-params :id]))]))

(defn loading-view
  []
  [:div.loading
   [:h1.loading__header "We are loading, please wait."]])

(defn main-view
  [views]
  [:div.main
   [controls-view]
   [:div.main-content
    [routing/routed-view views]
    ;; For debugging this example. Don't do this in your app!
    [:h2 (pr-str @re-frame.db/app-db)]]
   [notifications-view]])

;;
;; Event handlers for routes
;;

(rf/reg-event-fx
 :routes/home
 (fn [_ [_ bidi-match]]
   ;; In real apps, this would return effects for fetching data and setting state.
   (js/console.log "Navigated to " bidi-match)))

(rf/reg-event-fx
 :routes/page
 (fn [_ [_ bidi-match]]
   ;; In real apps, this would return effects for fetching data and setting state.
   (js/console.log "Navigated to " bidi-match)))

;;
;; Route -> view -mapping
;;

(def views {:routes/home home-view
            :routes/page page-view
            :else loading-view})

;;
;; Entry point
;;

(defn init!
  []
  (rf/dispatch [:re-fill/init-routing routes])
  (r/render [main-view views]
            (js/document.getElementById "app")))

(init!)

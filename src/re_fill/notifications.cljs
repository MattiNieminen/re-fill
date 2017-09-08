(ns re-fill.notifications
  (:require [re-frame.core :as rf]
            [re-fill.random]))

(rf/reg-event-fx
 :re-fill/notify
 [(rf/inject-cofx :re-fill/uuids 1)]
 (fn [{:keys [db uuids]} [_ notification options]]
   (let [id (first uuids)
         {:keys [hide-after]} options]
     (merge
      {:db (update db :re-fill/notifications conj (assoc notification :id id))}
      (if (> hide-after 0)
        {:dispatch-later [{:ms hide-after
                           :dispatch [:re-fill/delete-notification id]}]})))))

(rf/reg-event-db
 :re-fill/delete-notification
 (fn [db [_ id]]
   (assoc db
          :re-fill/notifications
          (remove #(= (:id %) id) (:re-fill/notifications db)))))


(rf/reg-sub
 :re-fill/notifications
 (fn [db _]
   (:re-fill/notifications db)))

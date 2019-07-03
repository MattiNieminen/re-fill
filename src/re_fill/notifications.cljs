(ns re-fill.notifications
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx
 :re-fill/notify
 (fn [{:keys [db]} [_ notification options]]
   (let [id (random-uuid)
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

(ns re-fill.random
  (:require [re-frame.core :as rf]
            [cljs-uuid.core :as uuid]))

(rf/reg-cofx
 :re-fill/uuids
 (fn [coeffects amount]
   (assoc coeffects
          :uuids
          (->> #(str (uuid/make-random)) repeatedly (take (or amount 1))))))

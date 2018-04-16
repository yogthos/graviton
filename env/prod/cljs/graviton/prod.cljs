(ns graviton.prod
  (:require
    [graviton.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)

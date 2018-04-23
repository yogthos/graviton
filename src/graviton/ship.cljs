(ns graviton.ship
  (:require
    [graviton.engine :as engine]
    [graviton.physics :refer [gravitational-acceleration-at-point]]))

(defn delta-x [{:keys [x]} delta]
  (* delta x))

(defn delta-y [{:keys [y]} delta]
  (* delta y))

(defn move-ship [{:keys [velocity id x y] :as ship} {:keys [width height delta actors]}]
  (let [acceleration (gravitational-acceleration-at-point x y (filterv #(not= id (:id %)) actors))
        velocity     (-> (merge-with + {:x (delta-x acceleration delta)
                                        :y (delta-y acceleration delta)} velocity)
                         (update :x #(* % (if
                                            (or (and (> 0 x) (not (pos? %)))
                                                (and (> x width) (pos? %)))
                                            -0.33
                                            1)))
                         (update :x max -10)
                         (update :x min 10)
                         (update :y #(* % (if
                                            (or (and (> 0 y) (not (pos? %)))
                                                (and (> y height) (pos? %)))
                                            -0.33
                                            1)))
                         (update :y max -10)
                         (update :y min 10)
                         )]
    ;; (println "Acc: " acceleration "  --  Vel: " velocity)
    (-> ship
        (update :x #(+ % (delta-x velocity delta)))
        (update :y #(+ % (delta-y velocity delta)))
        (assoc :velocity velocity))))

(defn instance []
  {:id       :ship
   :graphics (engine/sprite "ship.gif")
   :z-index  1
   :velocity {:y 0
              :x 0}
   :x        150
   :y        170
   :width    35
   :height   45
   :mass     35
   :update   move-ship})

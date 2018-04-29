(ns graviton.ship
  (:require
    [graviton.engine :as engine]
    [graviton.physics :refer [gravitational-acceleration-at-point nearest-point]]))

(defn delta-x [{:keys [x]} delta]
  (* delta x))

(defn delta-y [{:keys [y]} delta]
  (* delta y))

(defn move-ship [{:keys [velocity x y] :as ship} {:keys [vector-field force-radius width height delta]}]
  (let [{ax :x
         ay :y
         :as acceleration} (gravitational-acceleration-at-point force-radius x y vector-field)
        acceleration (if (< 1 (+ (* ax ax) (* ay ay))) {:x -1 :y -1} acceleration)
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
                         (update :y min 10))]
    (-> ship
        (update :x #(+ % (delta-x velocity delta)))
        (update :y #(+ % (delta-y velocity delta)))
        (assoc :velocity velocity))))

(defn ship-icon []
  (doto (js/PIXI.Graphics.)
    (.beginFill 0x3355ff 0.5)
    (.lineStyle 3 0xFF5500)
    (.moveTo 0 0)
    (.lineTo 25 10)
    (.lineTo 0 20)
    (.lineTo 0 0)
    (.endFill)))

(defn instance []
  {:id       :ship
   :type     :player
   :graphics (ship-icon)
   :z-index  1
   :velocity {:y 0 :x 0}
   :width    25
   :height   20
   :radius   10
   :mass     35
   :init     (fn [ship state]
               (assoc ship :x (/ (:width state) 2)
                           :y (/ (:height state) 2)))
   :update   move-ship})

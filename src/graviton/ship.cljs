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
        velocity     (-> (merge-with + {:x (delta-x acceleration delta)
                                        :y (delta-y acceleration delta)} velocity)
                         (update :x #(* % (if
                                            (or (and (> 0 x) (not (pos? %)))
                                                (and (> x width) (pos? %)))
                                            -0.33
                                            1)))
                         (update :x max -20)
                         (update :x min 20)
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
    (.moveTo -12.5 -10)
    (.lineTo 12.5 0)
    (.lineTo -12.5 10)
    (.lineTo -12.5 -10)
    (.endFill)))

(defn instance [state-atom]
  (let [ship-entity (ship-icon)]
    (set! (.-interactive ship-entity) true)
    {:id       :ship
     :type     :player
     :graphics ship-entity
     :z-index  1
     :velocity {:y 0 :x 0}
     :width    25
     :height   20
     :radius   10
     :mass     35
     :init     (fn [ship state]
                 (engine/drag-event (:graphics ship) state-atom {:on-start (fn [state event]
                                                                             (when (<= 1 (count (filterv #(= :attractor (:type %)) (:actors @state-atom))))
                                                                               (.on (:graphics ship) "pointermove" (fn [event]
                                                                                                                     (let [state @state-atom
                                                                                                                           {:keys [x y] :as actor} (some #(when (= :ship (:id %)) %) (:actors state))
                                                                                                                           local-pos (.-global (.-data event))
                                                                                                                           dx (- (.-x local-pos) x)
                                                                                                                           dy (- (.-y local-pos) y)]
                                                                                                                       (engine/set-graphics-position (assoc actor :velocity {:x dx :y dy})))))))
                                                                 :on-end (fn [state event]
                                                                           (when  (<= 1 (count (filterv #(= :attractor (:type %)) (:actors state))))
                                                                                 (let [local-pos (.getLocalPosition (.-data event) (:stage state))
                                                                                       x (.-x local-pos)
                                                                                       y (.-y local-pos)]
                                                                                   (.start (:ticker state))
                                                                                   (set! (.-interactive ship-entity) false)
                                                                                   (update state :actors #(mapv (fn [actor] (if (= (:id actor) :ship)
                                                                                                                              (let [dx (- x (:x actor))
                                                                                                                                    dy (- y (:y actor))
                                                                                                                                    scale (* 3 (/ (engine/sigmoid (+ (* dx dx) (* dy dy))) (js/Math.sqrt (+ (* dx dx) (* dy dy)))))
                                                                                                                                    velocity {:x (* dx scale)
                                                                                                                                              :y (* dy scale)}]
                                                                                                                                (println "click: " x ", " y " -- actor: " (:x actor) ", " (:y actor) " -- delta: " dx ", " dy " -- Velocity: " velocity)
                                                                                                                                (assoc actor :velocity velocity))
                                                                                                                              actor)) %)))))})
                 (assoc ship :x (/ (:width state) 2)
                        :y (/ (:height state) 2)))
     :update   move-ship}))

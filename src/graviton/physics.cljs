(ns graviton.physics)


(defn nearest-point [radius p]
  (- p (- (mod p (* 2 radius)) radius)))

(defn gravitational-acceleration-at-point [radius px py vector-field]
  (let [{ax  :x
         ay  :y
         :as acceleration} (get vector-field [(nearest-point radius px)
                                              (nearest-point radius py)])
        scale (if (< 1 (+ (* ax ax) (* ay ay))) (/ 1 (js/Math.hypot ax ay)) 1)]
    (when (not= scale 1) (js/Math.hypot (* ax scale) (* ay scale)))
    {:x (* ax scale)
     :y (* ay scale)}))

(ns graviton.physics)


(defn nearest-point [radius p]
  (- p (- (mod p (* 2 radius)) radius)))

(defn gravitational-acceleration-at-point [radius px py vector-field]
  (let [{ax  :x
         ay  :y
         :as acceleration} (get vector-field [(nearest-point radius px)
                                              (nearest-point radius py)])]
    (if (< 0.75 (+ (* ax ax) (* ay ay))) {:x -1 :y -1} acceleration)))

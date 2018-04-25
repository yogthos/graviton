(ns graviton.prizes
  (:require
    [graviton.engine :as engine]))

(defn color [r g b]
  (reduce (fn [acc n] (+ n (* 256 acc))) [r g b]))

(defn max-255 [v]
  (if (> v 255) 255 v))

(defn instance [x y radius]
  {:id       (keyword (str "prize-" x y))
   :x        x
   :y        y
   :mass     0
   :width    radius
   :height   radius
   :graphics (engine/draw-circle
               (js/PIXI.Graphics.)
               {:fill-color     (color 0 255 (max-255 radius))
                :line-color     (color 0 (max-255 radius) 255)
                :line-thickness 3
                :x              0
                :y              0
                :radius         radius})})

(defn random-prizes [{:keys [width height] :as state}]
  (update state
          :actors
          into
          (for [i (range (+ 3 (rand-int 5)))]
            (instance
              (rand-int (- width 50))
              (rand-int (- height 50))
              (+ 10 (rand-int 10))))))

(ns graviton.attractor
  (:require
    [graviton.engine :as engine]))

(defn color [radius]
  (js/PIXI.utils.rgb2hex
    (clj->js (repeat 3 (let [size (int (* 1.5 radius))] (if (< size 256) size 255))))))

(defn instance [x y radius]
  {:id     (keyword (str "attractor-" x y))
   :x      x
   :y      y
   :mass   radius
   :width  radius
   :height radius
   :sprite (engine/draw-circle
             (js/PIXI.Graphics.)
             {:color  (color radius)
              :x      0
              :y      0
              :radius radius})})

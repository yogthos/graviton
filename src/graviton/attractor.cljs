(ns graviton.attractor
  (:require
    [graviton.engine :as engine]))

(defn color [radius]
  (reduce (fn [acc n] (+ n (* 256 acc))) (repeat 3 (let [size (int (* 1.5 radius))] (if (< size 256) size 255)))))

(defn instance [x y radius]
  {:id       (keyword (str "attractor-" x y))
   :x        x
   :y        y
   :mass     radius
   :width    radius
   :height   radius
   :graphics (engine/draw-circle
               (js/PIXI.Graphics.)
               {#_#_:fill-color  (color radius)
                :line-color  (color radius)
                :line-thickness 5
                :x      0
                :y      0
                :radius radius})})

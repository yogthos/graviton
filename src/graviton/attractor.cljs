(ns graviton.attractor
  (:require
    [graviton.engine :as engine]))

(defn circle [color x y radius]
  (doto (js/PIXI.Graphics.)
    (.lineStyle 0)
    (.beginFill color)
    (.drawCircle 0 0 radius)
    (.endFill)))

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
   :sprite (circle (color radius) x y radius)})

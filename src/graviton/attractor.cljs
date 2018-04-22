(ns graviton.attractor
  (:require
    [graviton.engine :as engine]))

(defn circle [color x y radius]
  (doto (js/PIXI.Graphics.)
    (.lineStyle 0)
    (.beginFill color 1)
    (.drawCircle 0 0 radius)
    (.endFill)))

(defn instance [x y radius]
  {:id     (keyword (str "attractor-" x y))
   :x      x
   :y      y
   :mass   radius
   :width  radius
   :height radius
   :sprite (circle 0x0000FF x y radius)})

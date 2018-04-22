(ns graviton.attractor
  (:require
    [graviton.engine :as engine]))

(defn circle [color x y radius]
  (doto (js/PIXI.Graphics.)
    (.lineStyle 0)
    (.beginFill color 1)
    (.drawCircle 0 0 radius)
    (.endFill)))

(defn instance [idx x y mass]
  (let [width  (js/Math.ceil (* 50 (engine/sigmoid (/ (- mass 20) 100))))
        height (js/Math.ceil (* 50 (engine/sigmoid (/ (- mass 20) 100))))]
    {:id     (keyword (str "attractor-" (inc idx)))
     :x      x
     :y      y
     :mass   mass
     :width  width
     :height height
     :sprite (circle 0x0000FF x y (/ width 2))}))

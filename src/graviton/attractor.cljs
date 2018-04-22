(ns graviton.attractor
  (:require
    [graviton.engine :as engine]))

(defn instance [idx x y mass]
  {:id     (keyword (str "attractor-" (inc idx)))
   :x      x
   :y      y
   :mass   mass
   :width  (js/Math.ceil (* 50 (engine/sigmoid (/ (- mass 20) 100))))
   :height (js/Math.ceil (* 50 (engine/sigmoid (/ (- mass 20) 100))))
   :sprite (engine/sprite "circle.png")})

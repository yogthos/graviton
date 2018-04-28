(ns graviton.deathzone
  (:require [graviton.engine :as engine]))

(defn color [r g b]
  (reduce (fn [acc n] (+ n (* 256 acc))) [r g b]))

(defn max-255 [v]
  (if (> v 255) 255 v))

(defn instance [x y radius]
  {:id       (keyword (str "deathzone-" x y))
   :type     :deathzone
   :x        x
   :y        y
   :mass     0
   :radius   radius
   :graphics (js/PIXI.Graphics.)
   :init     (fn [deathzone state]
               (engine/draw-circle
                 (:graphics deathzone)
                 {:fill-color     (color 255 0 0)
                  :line-color     (color 255 60 0)
                  :line-thickness 3
                  :x              0
                  :y              0
                  :radius         radius})
               deathzone)
   :update   (fn [deathzone state] deathzone)})

(defn valid-coords? [dzx dzy actors]
  (every?
    true?
    (for [{:keys [x y]} actors]
      (or (> 200 (js/Math.abs (- dzx x)))
          (> 200 (js/Math.abs (- dzy y)))))))

(defn random-coords [{:keys [width height actors] :as state}]
  [(rand-int (- width 100))
   (rand-int (- height 100))]
  #_(let [x (rand-int (- width 100))
          y (rand-int (- height 100))]
      (if (valid-coords? x y actors)
        [x y]
        (recur state))))

(defn random-deathzone [state]
  (let [[x y] (random-coords state)
        deathzone (instance x y (+ 60 (rand-int 10)))]
    ((:init deathzone) deathzone state)
    (engine/add-actor-to-stage state deathzone)
    (update state :actors conj deathzone)))


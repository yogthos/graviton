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
                  :line-color     (color 230 60 0)
                  :line-thickness 4
                  :x              0
                  :y              0
                  :opacity        0.4
                  :radius         (* radius 1.1)})
               deathzone)
   :update   (fn [deathzone state] deathzone)})

(defn random-deathzone [state]
  (let [{:keys [x y radius]} (engine/random-xyr (:width state) (:height state) {:min-r 20
                                                                                :max-r 30
                                                                                :existing (:actors state)})
        deathzone (instance x y radius)]
    ((:init deathzone) deathzone state)
    (engine/add-actor-to-stage state deathzone)
    (update state :actors conj deathzone)))


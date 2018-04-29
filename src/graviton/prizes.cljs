(ns graviton.prizes
  (:require
    [graviton.engine :as engine]))

(defn color [r g b]
  (reduce (fn [acc n] (+ n (* 256 acc))) [r g b]))

(defn max-255 [v]
  (if (> v 255) 255 v))

(defn instance [x y radius]
  {:id       (keyword (str "prize-" x y))
   :type     :prize
   :x        x
   :y        y
   :mass     0
   :radius   radius
   :graphics (js/PIXI.Graphics.)
   :init     (fn [prize state]
               (engine/draw-circle
                 (:graphics prize)
                 {:fill-color     (color 0 255 (max-255 radius))
                  :line-color     (color 0 (max-255 radius) 255)
                  :opacity        0.7
                  :line-thickness 3
                  :x              0
                  :y              0
                  :radius         radius})
               prize)
   :update   (fn [prize state] prize)})

(defn random-prizes [{:keys [width height total-prizes actors] :as state}]
  (update state
          :actors
          (fnil into [])
          (mapv (fn [{:keys [x y radius]}]
                  (instance x y radius))
                (engine/random-xyrs total-prizes width height {:min-r 40
                                                               :max-r 50
                                                               :padding 0
                                                               :existing actors}))))

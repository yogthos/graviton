(ns graviton.prizes
  (:require
    [graviton.engine :as engine]))

;https://github.com/a-jie/Proton
;https://github.com/a-jie/Proton/blob/master/example/game/pixijs/pixi-game.html

(defn emitter [proton x y radius width height]
  (let [emitter (doto (js/Proton.Emitter.)
                  (.addInitialize (js/Proton.Body. "/assets/particle.png"))
                  (.addInitialize (js/Proton.Life. 1))
                  (.addInitialize (js/Proton.Mass. 1 #_(* 2 radius)))
                  (.addInitialize (js/Proton.Radius. (* 0.4 radius) radius))
                  (.addBehaviour (js/Proton.Alpha. 1 0))
                  (.addBehaviour (js/Proton.Color. "#4F1500" "#0029FF"))
                  (.addBehaviour (js/Proton.Scale. 0.5 0.1))
                  (.addBehaviour (js/Proton.CrossZone.
                                   (js/Proton.RectZone. 0 0 width height)
                                   "dead")))]
    (set! (.-rate emitter) (js/Proton.Rate. (js/Proton.Span. 5 7) (js/Proton.Span. 0.01 0.02)))
    (set! (-> emitter .-p .-x) x)
    (set! (-> emitter .-p .-y) y)
    (.addEmitter proton emitter)
    (.emit emitter 0.5)
    emitter))

(defn color [r g b]
  (reduce (fn [acc n] (+ n (* 256 acc))) [r g b]))

(defn max-255 [v]
  (if (> v 255) 255 v))

(defn rotate [{:keys [emitter tha x y] :as prize} state]
  (set! (-> emitter .-p .-x)
        (+ x (* 10 (js/Math.sin (+ tha (/ js/Math.PI 2))))))
  (set! (-> emitter .-p .-y)
        (+ y (* 10 (js/Math.cos (+ tha (/ js/Math.PI 2))))))
  #_(.preEmit emitter 1.2)
  (update prize :tha + 0.1))

(defn instance [x y radius]
  {:id     (keyword (str "prize-" x y))
   :x      x
   :y      y
   :mass   0
   :tha    0
   :width  radius
   :height radius
   :init   (fn [prize {:keys [proton width height] :as state}]
             (assoc prize
               :emitter (emitter proton x y radius width height)
               :graphics
               (js/PIXI.Graphics.)
               #_(engine/draw-circle
                   (js/PIXI.Graphics.)
                   {:fill-color     (color 0 255 (max-255 radius))
                    :line-color     (color 0 (max-255 radius) 255)
                    :line-thickness 3
                    :x              0
                    :y              0
                    :radius         radius})))
   :update rotate})

(defn random-prizes [{:keys [width height] :as state}]
  (update state
          :actors
          into
          (for [_ (range (+ 3 (rand-int 5)))]
            (instance
              (rand-int (- width 100))
              (rand-int (- height 100))
              (+ 10 (rand-int 10))))))

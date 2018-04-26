(ns graviton.force-field
  (:require
    [graviton.engine :as engine]
    [graviton.physics :as physics :refer [gravitational-acceleration-at-point]]))

(defn sigmoid [v]
  (/ v (+ 1 (js/Math.abs v))))


(defn draw-gravity-vector [graphics x y {:keys [force-radius vector-field] :as state}]
  (let [{ax :x ay :y :as acceleration} (gravitational-acceleration-at-point force-radius x y vector-field)
        ax         (* 100 ax)
        ay         (* 100 ay)
        magnitude  (+ (* ax ax) (* ay ay))
        theta      (js/Math.atan2 ax ay)
        redness    1
        greenness  70
        max-length 8
        color      (+ (* (js/Math.round (* 0xff (sigmoid (* magnitude redness)))) 0x10000)
                      (- 0xff00 (* (js/Math.round (* 0xff (sigmoid (/ magnitude greenness)))) 0x100)))
        width      (* 2 (sigmoid (* 5 magnitude)))]

    ;; Change max-length truncation to preserve direction
    (engine/draw-line graphics {:color color
                                :width width
                                :start {:x x
                                        :y y}
                                :end   {:x (+ x (* max-length (js/Math.sin theta) (sigmoid magnitude)))
                                        :y (+ y (* max-length (js/Math.cos theta) (sigmoid magnitude)))}})))

(defn draw-vector-field [vector-field state]
  (let [spacing  (:force-radius state)
        graphics (:graphics vector-field)]
    (.clear graphics)
    (doseq [x (map #(* spacing %) (range (js/Math.ceil (/ (:width state) spacing))))
            y (map #(* spacing %) (range (js/Math.ceil (/ (:height state) spacing))))]
      (draw-gravity-vector graphics x y state))))

(defn instance []
  {:id     "force-field"
   :init   (fn [vector-field state]
             (let [{:keys [graphics] :as vector-field} (assoc vector-field :graphics (js/PIXI.Graphics.))]
               (set! (.-width graphics) (:width state))
               (set! (.-height graphics) (:height state))
               (.addChild (:stage state) graphics)
               vector-field))
   :update (fn [vector-field state])})

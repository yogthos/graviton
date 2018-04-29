(ns graviton.force-field
  (:require
    [graviton.engine :as engine]
    [graviton.physics :as physics :refer [gravitational-acceleration-at-point]]))


(defn draw-gravity-vector [graphics x y {:keys [force-radius vector-field]}]
  (let [{ax :x ay :y} (gravitational-acceleration-at-point force-radius x y vector-field)
        ax         (* 100 ax)
        ay         (* 100 ay)
        magnitude  (+ (* ax ax) (* ay ay))
        theta      (js/Math.atan2 ax ay)
        redness    1
        greenness  100
        max-length 8
        color      (+ (* (max 0x00 (- (js/Math.round (* 0x1cb (engine/sigmoid (* magnitude redness)))) 0xcc)) 0x10000)
                      (- 0xff00 (* (+ 0x00 (js/Math.round (* 0xff (engine/sigmoid (/ magnitude greenness))))) 0x100)))
        width      (* 3 (engine/sigmoid (* 5 magnitude)))]

    ;; Change max-length truncation to preserve direction
    (engine/draw-line graphics {:color color
                                :width width
                                :start {:x x
                                        :y y}
                                :end   {:x (+ x (* max-length (js/Math.sin theta) (engine/sigmoid magnitude)))
                                        :y (+ y (* max-length (js/Math.cos theta) (engine/sigmoid magnitude)))}})))

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

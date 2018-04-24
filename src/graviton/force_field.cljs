(ns graviton.force-field
  (:require
    [graviton.engine :as engine]
    [graviton.physics :refer [gravitational-acceleration-at-point]]))

(defn sigmoid [v]
  (/ v (+ 1 (js/Math.abs v))))

(defn draw-gravity-vector [graphics x y state]
  (let [{ax :x ay :y :as acceleration} (gravitational-acceleration-at-point x y (filterv #(not= (:id %) :ship) (:actors state)))
        ax         (* 100 ax)
        ay         (* 100 ay)
        magnitude  (+ (* ax ax) (* ay ay))
        theta (js/Math.atan2 ax ay)
        redness    1
        greenness  70
        max-length 5
        color      (+ (* (js/Math.round (* 0xff (sigmoid (* magnitude redness)))) 0x10000)
                      (- 0xff00 (* (js/Math.round (* 0xff (sigmoid (/ magnitude greenness)))) 0x100)))
        width      (* 0.75 (sigmoid (* 5 magnitude)))]

    ;; Change max-length truncation to preserve direction
    (engine/draw-line graphics {:color color
                                  :width width
                                  :start {:x x
                                          :y y}
                                  :end   {:x (+ x (* max-length (js/Math.sin theta) (sigmoid magnitude)))
                                          :y (+ y (* max-length (js/Math.cos theta) (sigmoid magnitude)))}})))

(defn draw-vector-field [vector-field state]
  (let [spacing 7
        graphics (:graphics vector-field)]
    (.clear graphics)
    (doseq [x (map #(* spacing %) (range (js/Math.ceil (/ (:width state) spacing))))
              y (map #(* spacing %) (range (js/Math.ceil (/ (:height state) spacing))))]
        (draw-gravity-vector graphics x y state))))

(defn instance []
  {:id       "force-field"
   :graphics (js/PIXI.Graphics.)
   :init     (fn [{:keys [graphics]} state]
               (set! (.-width graphics) (:width state))
               (set! (.-height graphics) (:height state))
               (.addChild (:stage state) graphics))
   :update   (fn [vector-field state]
               (draw-vector-field vector-field state))})

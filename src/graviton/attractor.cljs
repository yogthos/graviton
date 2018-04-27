(ns graviton.attractor
  (:require
    [graviton.engine :as engine]))

(defn vector-field-for-actor [state actor]
  (let [half-spacing (:force-radius state)
        spacing      (* 2 half-spacing)
        {:keys [x y mass]} actor]
    (persistent!
      (reduce
        (fn [acc [k v]]
          (assoc! acc k v))
        (transient {})
        (for [px (map #(+ half-spacing (* spacing %)) (range (js/Math.ceil (/ (:width state) spacing))))
              py (map #(+ half-spacing (* spacing %)) (range (js/Math.ceil (/ (:height state) spacing))))]
          (let [dx    (- px x)
                dy    (- py y)
                r     (- (/ mass (+ (* dx dx) (* dy dy) 0.0000001))) ;; add small base r to prevent divide by zero errors
                theta (js/Math.atan2 dy dx)
                vec   {:x (* r (js/Math.cos theta))
                       :y (* r (js/Math.sin theta))}]
            [[px py] vec]))))))

(defn color [radius]
  (reduce (fn [acc n] (+ n (* 256 acc))) (repeat 3 (let [size (int (* 1.5 radius))] (if (< size 256) size 255)))))

(defn instance [state x y radius]
  (let [radius (min 150 radius)
        mass   (/ (* radius radius) 25)]
    {:id           (keyword (str "attractor-" x y))
     :type         :attractor
     :x            x
     :y            y
     :mass         mass
     :width        radius
     :height       radius
     :vector-field (vector-field-for-actor state {:x x :y y :mass mass})
     :init         (fn [attractor state]
                     (assoc attractor
                       :graphics
                       (engine/draw-circle
                         (js/PIXI.Graphics.)
                         {:line-color     (color radius)
                          :line-thickness 5
                          :x              0
                          :y              0
                          :radius         radius})))}))

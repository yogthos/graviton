(ns graviton.physics)

(defn vector-field-for-actor [state actor spacing]
  (let [{:keys [x y mass]} actor]
    (for [px (map #(* spacing %) (range (js/Math.ceil (/ (:width state) spacing))))
          py (map #(* spacing %) (range (js/Math.ceil (/ (:height state) spacing))))]
      (let [dx    (- px x)
            dy    (- py y)
            r     (/ mass (+ (* dx dx) (* dy dy) 0.0000001)) ;; add small base r to prevent divide by zero errors
            theta (js/Math.atan2 dy dx)]
        {:x (* r (js/Math.cos theta))
         :y (* r (js/Math.sin theta))}))))

(defn gravitational-acceleration-at-point [px py actors]
  (apply merge-with +
         (map (fn [{:keys [x y mass]}]
                (let [dx    (- px x)
                      dy    (- py y)
                      r     (/ mass (+ (* dx dx) (* dy dy) 0.0000001))
                      theta (js/Math.atan2 dy dx)]
                  {:x (* -1 r (js/Math.cos theta))
                   :y (* -1 r (js/Math.sin theta))})) actors)))

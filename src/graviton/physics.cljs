(ns graviton.physics)

(defn gravitational-acceleration-at-point [px py actors]
  (apply merge-with +
         (map (fn [{:keys [x y mass]}]
                (let [dx    (- px x)
                      dy    (- py y)
                      r     (/ mass (+ (* dx dx) (* dy dy) 0.0000001))
                      theta (js/Math.atan (/ dy dx))]
                  {:x (* (if (> px x) -1 1) r (js/Math.cos theta))
                   :y (* (if (>= px x) -1 1) r (js/Math.sin theta))})) actors)))

(ns graviton.core
  (:require
    [graviton.engine :as engine]
    [clojure.walk :refer [prewalk postwalk]]
    [reagent.core :as r]))

;assets https://itch.io/game-assets/free/tag-2d

;; -------------------------
;; Views
(defn canvas [state]
  (r/create-class
   {:component-did-mount (engine/init-canvas state)
    #_#_:should-component-update (constantly false)
     :render (fn [] (println "RENDER") [:canvas {:width 600 :height 600}])}))

(defn delta-x [{:keys [x]} delta]
  (* delta x))

(defn delta-y [{:keys [y]} delta]
  (* delta y))


(defn move-ship [{:keys [velocity id x y] :as ship} {:keys [width height delta actors]}]
  (let [acceleration (engine/gravitational-acceleration-at-point x y (filterv #(not= id (:id %)) actors))
        velocity (-> (merge-with + {:x (delta-x acceleration delta)
                                    :y (delta-y acceleration delta)} velocity)
                     (update :x #(* % (if
                                        (or (and (> 0 x) (not (pos? %)))
                                            (and (> x width) (pos? %)))
                                      -0.33
                                      1)))
                     (update :x max -10)
                     (update :x min 10)
                     (update :y #(* % (if
                                        (or (and (> 0 y) (not (pos? %)))
                                            (and (> y height) (pos? %)))
                                      -0.33
                                      1)))
                     (update :y max -10)
                     (update :y min 10)
                     )]
    ;; (println "Acc: " acceleration "  --  Vel: " velocity)
    (-> ship
        (update :x #(+ % (delta-x velocity delta)))
        (update :y #(+ % (delta-y velocity delta)))
        (assoc :velocity velocity))))

(defn update-actors [state]
  (postwalk
    (fn [node]
      (if (and (:sprite node) (:update node))
        (let [updated-node ((:update node) node state)]
          (engine/set-sprite-position updated-node)
          updated-node)
        node))
    (:actors state)))

(defn update-game-state [state]
  (assoc state :actors (update-actors state)))

(def state (atom
                {:update update-game-state
                 :actors (into [{:id     :ship
                                 :sprite (engine/sprite "ship.gif")
                                 :velocity {:y 0
                                            :x 0}
                                 :x      150
                                 :y      170
                                 :width 35
                                 :height 45
                                 :mass 35
                                 :update move-ship}]
                               (mapv (fn [idx x y mass]
                                       {:id (keyword (str "attractor-" (inc idx)))
                                        :x x
                                        :y y
                                        :mass mass
                                        :width (js/Math.ceil (* 50 (engine/sigmoid (/ (- mass 20) 100))))
                                        :height (js/Math.ceil (* 50 (engine/sigmoid (/ (- mass 20) 100))))
                                        :sprite (engine/sprite "circle.png")}) (range 5) (repeatedly #(+ 50 (rand-int 500))) (repeatedly #(+ 50 (rand-int 500))) (repeatedly #(+ 50 (rand-int 100)))))}))
(defn game []
  [:div
   [:h2 "Graviton"]
   [canvas state]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [game] (.getElementById js/document "app")))

(defn init! []
  (mount-root))

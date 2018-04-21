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
     :render (fn [] (println "RENDER") [:canvas {:width 500 :height 500}])}))

(defn delta-x [{:keys [x]} delta]
  (* delta x))

(defn delta-y [{:keys [y]} delta]
  (* delta y))

(defn polar-sum [v1 v2 & vs]
  (cond
    (not-empty vs)
    (polar-sum v1 (apply polar-sum v2 vs))
    (nil? v2) v1
    :else {:r (js/Math.sqrt (+ (:r v1) (:r v2) (* 2 (:r v1) (:r v2) (js/Math.cos (- (:theta v2) (:theta v1))))))
           :theta (+ (:theta v1) (js/Math.atan2 (* (:r v2) (js/Math.sin (- (:theta v2) (:theta v1))))
                                                (+ (:r v1) (* (:r v2) (js/Math.cos (- (:theta v2) (:theta v1)))))))}))


(defn move-ship [{:keys [velocity id x y] :as ship} {:keys [width height delta actors]}]
  (let [acceleration (engine/gravitational-acceleration-at-point x y (filterv #(not= id (:id %)) actors))
        velocity (-> (merge-with + {:x (delta-x acceleration delta)
                                    :y (delta-y acceleration delta)} velocity)
                     (update :x max -10)
                     (update :x min 10)
                     (update :y max -10)
                     (update :y min 10))]
    ;; (println "Acc: " acceleration "  --  Vel: " velocity)
    (-> ship
        (update :x #(mod (+ % (delta-x velocity delta)) width))
        (update :y #(mod (+ % (delta-y velocity delta)) height))
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
                 :actors [{:id     :ship
                           :sprite (engine/sprite "ship.gif")
                           :velocity {:y 0
                                      :x 2}
                           :x      250
                           :y      200
                           :mass 1
                           :update move-ship}
                          {:id :attractor-1
                           :sprite (engine/sprite "circle.png")
                           :x 250
                           :y 250
                           :width 50
                           :height 50
                           :mass 300}
                          {:id :attractor-1
                           :sprite (engine/sprite "circle.png")
                           :x 400
                           :y 400
                           :width 25
                           :height 25
                           :mass 200}]}))
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

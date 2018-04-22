(ns graviton.core
  (:require
    [graviton.attractor :as attractor]
    [graviton.engine :as engine]
    [graviton.ship :as ship]
    [clojure.walk :refer [prewalk postwalk]]
    [reagent.core :as r]))

;assets https://itch.io/game-assets/free/tag-2d

;; -------------------------
;; Views
(defn canvas [state]
  (r/create-class
    {:component-did-mount (engine/init-canvas state)
     #_#_:should-component-update (constantly false)
     :render              (fn [] (println "RENDER") [:canvas {:width 600 :height 600}])}))

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

(defn stage-click [state event]
  (let [{:keys [x y]} (engine/click-coords (:stage @state) event)
        attractor (attractor/instance (str x y) x y (+ 50 (rand-int 500)))]
    (engine/add-to-stage (:stage @state) attractor)
    (swap! state
           update :actors
           conj attractor)))

(defn stage-click-drag [action]
  (let [drag-state (atom {})]
    {:on-start (fn [state event]
                 (swap! drag-state assoc :start (engine/click-coords (:stage @state) event)))
     :on-end   (fn [state event]
                 (when-let [start-coords (:start @drag-state)]
                   (action state start-coords (engine/click-coords (:stage @state) event))
                   (reset! drag-state {})))}))

;;TODO should create the attractor on start of drag
;;add it to the stage, and grow it interactively as the drag event happens
(defn add-attractor [state start-coords end-coords]
  (let [{start-x :x start-y :y} start-coords
        {end-x :x end-y :y} end-coords
        attractor (attractor/instance
                    (str start-x start-y)
                    start-x start-y
                    (js/Math.sqrt
                      (+ (js/Math.pow (js/Math.abs (- start-x end-x)) 2)
                         (js/Math.pow (js/Math.abs (- start-y end-y)) 2))))]
    (println
      "\n" start-coords end-coords
      "\na:" (js/Math.pow (js/Math.abs (- start-y end-y)) 2)
      "\nb:" (js/Math.pow (js/Math.abs (- start-x end-x)) 2)
      "\nsize" (js/Math.sqrt
        (+ (js/Math.pow (js/Math.abs (- start-x end-x)) 2)
           (js/Math.pow (js/Math.abs (- start-y end-y)) 2))))
    (engine/add-to-stage (:stage @state) attractor)
    (swap! state
           update :actors
           conj attractor)))

(def state (atom
             {
              ;:on-click stage-click
              :on-drag (stage-click-drag add-attractor)
              :update  update-game-state
              :actors  [(ship/instance)]}))
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

(ns graviton.core
  (:require
    [graviton.attractor :as attractor]
    [graviton.engine :as engine]
    [graviton.force-field :as force-field]
    [graviton.ship :as ship]
    [clojure.walk :refer [postwalk]]
    [reagent.core :as r]))

;assets https://itch.io/game-assets/free/tag-2d

;; -------------------------
;; Views
(defn canvas [state]
  (r/create-class
    {:component-did-mount
     (engine/init-canvas state)
     #_#_:should-component-update
         (constantly false)
     :render
     (fn [] (println "RENDER") [:canvas {:width 900 :height 600}])}))

(defn update-actors [state]
  (postwalk
    (fn [node]
      (if (and (:sprite node) (:update node))
        (let [updated-node ((:update node) node state)]
          (engine/set-sprite-position updated-node)
          updated-node)
        node))
    (:actors state)))

(defn update-objects [state]
  (doseq [{:keys [update] :as object} (:objects state)]
    (update object state)))

(defn update-game-state [state]
  (update-objects state)
  (assoc state :actors (update-actors state)))

(defn stage-click-drag [action]
  (let [drag-state (atom {})]
    {:on-start (fn [state event]
                 (let [{:keys [x y] :as point} (engine/click-coords (:stage @state) event)]
                   (swap! drag-state assoc
                          :start point
                          :line (let [line (js/PIXI.Graphics.)]
                                  (engine/draw-line line {:color 255 :width 10 :start point :end point})
                                  (.addChild (:stage @state) line)
                                  line))))
     :on-move  (fn [state event]
                 (when-let [line (:line @drag-state)]
                   (.clear line)
                   (engine/draw-line line {:color 0xFF0000
                                           :width 1
                                           :start (:start @drag-state)
                                           :end   (engine/click-coords (:stage @state) event)})))
     :on-end   (fn [state event]
                 (when-let [start-coords (:start @drag-state)]
                   (action state start-coords (engine/click-coords (:stage @state) event))
                   (.removeChild (:stage @state) (:line @drag-state))
                   (reset! drag-state {})))}))

(defn add-attractor [state start-coords end-coords]
  (let [{start-x :x start-y :y} start-coords
        {end-x :x end-y :y} end-coords
        attractor (attractor/instance
                    start-x start-y
                    (js/Math.sqrt
                      (+ (js/Math.pow (js/Math.abs (- start-x end-x)) 2)
                         (js/Math.pow (js/Math.abs (- start-y end-y)) 2))))]
    (engine/add-to-stage (:stage @state) attractor)
    (let [state (swap! state update :actors conj attractor)]
      (doseq [{:keys [update] :as object} (:objects state)]
        (update object state)))))

(def state (atom
             {:on-drag (stage-click-drag add-attractor)
              :update  update-game-state
              :objects [(force-field/instance)]
              :actors  [(ship/instance)]}))

(defn restart [state]
  (swap! state
         (fn [state]
           (engine/remove-actors-from-stage state)
           (let [ship (ship/instance)]
             (engine/add-to-stage (:stage state) ship)
             (assoc state :actors [ship])))))

(defn game []
  [:div
   [:h2 "Graviton"]
   [canvas state]
   [:button
    {:on-click #(restart state)}
    "restart"]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [game] (.getElementById js/document "app")))

(defn init! []
  (mount-root))

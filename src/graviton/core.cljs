(ns graviton.core
  (:require
    [graviton.attractor :as attractor]
    [graviton.engine :as engine]
    [graviton.force-field :as force-field]
    [graviton.ship :as ship]
    [graviton.ui :as ui]
    [clojure.walk :refer [postwalk]]
    [reagent.core :as r]))

;assets https://itch.io/game-assets/free/tag-2d

(defn update-actors [state]
  (postwalk
    (fn [node]
      (if (and (:graphics node) (:update node))
        (let [updated-node ((:update node) node state)]
          (engine/set-graphics-position updated-node)
          updated-node)
        node))
    (:actors state)))

(defn update-scene-objects [state]
  (doseq [{:keys [update] :as object} (into (:background state) (:foreground state))]
    (update object state)))

(defn update-game-state [state]
  (assoc state :actors (update-actors state)))

(defn stage-click-drag [action]
  (let [drag-state (volatile! {})]
    {:on-start (fn [state event]
                 (let [{:keys [x y] :as point} (engine/click-coords (:stage state) event)]
                   (vswap! drag-state assoc
                          :start point
                          :line (let [line (js/PIXI.Graphics.)]
                                  (engine/draw-line line {:color 255 :width 10 :start point :end point})
                                  (.addChild (:stage state) line)
                                  line))))
     :on-move  (fn [state event]
                 (when-let [line (:line @drag-state)]
                   (.clear line)
                   (engine/draw-line line {:color 0xFF0000
                                           :width 1
                                           :start (:start @drag-state)
                                           :end   (engine/click-coords (:stage state) event)})))
     :on-end   (fn [state event]
                 (when-let [start-coords (:start @drag-state)]
                   (.removeChild (:stage state) (:line @drag-state))
                   (vreset! drag-state {})
                   (action state start-coords (engine/click-coords (:stage state) event))))}))

(defn add-attractor [state start-coords end-coords]
  (let [{start-x :x start-y :y} start-coords
        {end-x :x end-y :y} end-coords
        attractor (attractor/instance
                    start-x start-y
                    (js/Math.sqrt
                      (+ (js/Math.pow (js/Math.abs (- start-x end-x)) 2)
                         (js/Math.pow (js/Math.abs (- start-y end-y)) 2))))]
    (engine/add-actor-to-stage (:stage state) attractor)
    (let [state (update state :actors conj attractor)]
      (update-scene-objects state)
      state)))



(def initial-state-map {:on-drag    (stage-click-drag add-attractor)
                        :update     update-game-state
                        :background [(force-field/instance)]
                        ; menus, score, etc
                        :foreground []
                        :actors     [(ship/instance)]})

(def state (volatile! nil))

(declare menu)

(defn init-state [state]
  (vreset! state (update initial-state-map :foreground (fnil into []) (menu state))))

(defn destroy-pixi-objects [state]
  (vswap! state
         (fn [state]
           (engine/remove-actors-from-stage state)
           (engine/remove-objects-from-stage state)
           (let [ship        (ship/instance)
                 force-field (force-field/instance)]
             (engine/add-actor-to-stage (:stage state) ship)
             (engine/add-object-to-stage (:stage state) force-field)
             (assoc state :actors [ship]
                          :background [force-field]
                          :foreground [])))))
(defn restart [state]
  #_(destroy-pixi-objects state)
  (init-state state))

(defn menu [state]
  [(ui/button {:label    "start"
               :x        100
               :y        100
               :width    200
               :height   50
               :on-click #(println "clicked") #_(restart state)})])

(defn canvas [state]
  (r/create-class
    {:component-did-mount
     (engine/init-canvas state)
     :render
     (fn []
       (println "initializing")
       [:canvas {:width (.-innerWidth js/window) :height (.-innerHeight js/window)}])}))

(defn game []
  [canvas state])

;; -------------------------
;; Initialize app

(defn mount-root []
  (init-state state)
  (r/render [game] (.getElementById js/document "app")))

(defn init! []
  (mount-root))

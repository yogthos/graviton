(ns graviton.core
  (:require
    [graviton.attractor :as attractor]
    [graviton.deathzone :as deathzone]
    [graviton.engine :as engine]
    [graviton.force-field :as force-field]
    [graviton.prizes :as prizes]
    [graviton.ship :as ship]
    [graviton.ui :as ui]
    [clojure.walk :refer [postwalk]]
    [reagent.core :as r]
    [cljsjs.pixi]
    [cljsjs.pixi-sound]))

(defn update-actors [state]
  (update
    state
    :actors
    (fn [actors]
      (postwalk
        (fn [node]
          (if (and (:graphics node) (:update node))
            (let [updated-node ((:update node) node state)]
              (engine/set-graphics-position updated-node)
              updated-node)
            node))
        actors))))

(defn update-scene-objects [state]
  (doseq [{:keys [update] :as object} (into (:background state) (:foreground state))]
    (update object state)))

(defn add-deathzones [{:keys [actors] :as state}]
  (if (> (mod (count (filter #(= (:type %) :attractor) actors)) 4) 2)
    (deathzone/random-deathzone state)
    state))

;;todo only run when adding/removing actors
(defn group-actors-by-type [actors]
  (reduce
    (fn [entities {:keys [type] :as actor}]
      (case type
        :player (assoc entities :player actor)
        :deathzone (update entities :deathzones (fnil conj []) actor)
        :prize (update entities :prizes (fnil conj []) actor)
        entities))
    {}
    actors))

(declare initial-state-map)
(def state (volatile! nil))

(defn restart []
  (vswap! state assoc :game-state :stopped)

  (engine/clear-stage @state)
  (vswap! state
          (fn [current-state]
            (-> current-state
                (merge (select-keys (initial-state-map) [:game-state :background :actors :foreground :vector-field]))
                (prizes/random-prizes))))
  (engine/init-scene state)
  (engine/init-render-loop state)
  (.start (:ticker @state)))

(defn final-score [{:keys [width height score]}]
  (ui/text-box {:x 20 :y 20 :text (str "Final Score: " score " prizes collected!")}))

(defn restart-button [{:keys [width height score total-prizes]}]
  (ui/button {:label    (if (= score total-prizes) "You Win" "Try Again")
              :x        (- (/ width 2) 100)
              :y        (- (/ height 2) 25)
              :width    200
              :height   50
              :on-click restart}))

(defn end-game-screen [state]
  (let [button (restart-button state)
        score  (final-score state)]
    (engine/add-actor-to-stage state button)
    (engine/add-actor-to-stage state score)
    (-> state
        (assoc :score 0 :game-state :game-over)
        (update :actors into [button score]))))

(defn deathzone-collisions [state player deathzones]
  (if (and deathzones (some (fn [zone] (engine/collides? player zone)) deathzones))
    (end-game-screen state)
    state))

(defn find-prize-collisions [{:keys [stage] :as state} player prizes]
  (reduce
    (fn [state {:keys [id] :as prize}]
      (if (engine/collides? player prize)
        (do
          (engine/remove-from-stage stage prize)
          (-> state
              (update :actors (fn [actors] (vec (remove #(= (:id %) id) actors))))
              (update :score inc)))
        state))
    state
    prizes))

(defn prize-collisions [state player prizes]
  (let [{:keys [total-prizes score] :as new-state} (find-prize-collisions state player prizes)]
    (if (= total-prizes score)
      (end-game-screen new-state)
      new-state)))

(defn collisions [{:keys [actors] :as state}]
  (let [{player     :player
         deathzones :deathzones
         prizes     :prizes} (group-actors-by-type actors)]
    (-> state
        (deathzone-collisions player deathzones)
        (prize-collisions player prizes))))

(defn update-game-state [state]
  (when (engine/started? state)
    (-> state
        (update-actors)
        (collisions))))

(defn stage-click-drag [action]
  (let [drag-state (volatile! {})]
    {:on-start (fn [state event]
                 (let [{:keys [x y] :as point} (engine/click-coords (:stage state) event)]
                   (when (engine/started? state)
                     (vswap! drag-state assoc
                             :start point
                             :line (let [line (js/PIXI.Graphics.)]
                                     (engine/draw-line line {:color 255 :width 10 :start point :end point})
                                     (.addChild (:stage state) line)
                                     line)))))
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

(defn add-attractor [state {:keys [x y] :as start-coords} end-coords]
  (let [attractor    (attractor/instance state x y (engine/distance start-coords end-coords))
        vector-field (:vector-field attractor)
        attractor    (dissoc attractor :vector-field)]
    (engine/add-actor-to-stage state attractor)
    (let [state (-> state
                    (update :actors conj attractor)
                    (update :vector-field #(merge-with (partial merge-with +) % vector-field)))]
      (force-field/draw-vector-field (some #(when (= (:id %) "force-field") %) (:background state)) state)
      (update-scene-objects state)
      (add-deathzones state))))

(defn initial-state-map []
  {:score        0
   :total-prizes 5
   :game-state   :started #_:stopped
   :vector-field nil
   :force-radius 25
   :on-drag      (stage-click-drag add-attractor)
   :update       update-game-state
   :background   [(force-field/instance)]
   :foreground   []
   :actors       [(ship/instance state)]})

(defn init-state [state]
  (vreset! state (initial-state-map)))

(defn canvas [state]
  (r/create-class
    {:component-did-mount
     (engine/init-canvas state #(-> % #_ui/help-menu (vswap! prizes/random-prizes)))
     :render
     (fn []
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

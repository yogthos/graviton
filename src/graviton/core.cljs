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
     :render (fn [] (println "RENDER") [:canvas {:width 600 :height 600}])}))

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

(defn stage-click [event]
  (js/console.log "stage hit")
  (js/console.log event))

(def state (atom
                {:on-click stage-click
                 :update update-game-state
                 :actors (into [(ship/instance)]
                               (mapv attractor/instance (range 5) (repeatedly #(+ 50 (rand-int 500))) (repeatedly #(+ 50 (rand-int 500))) (repeatedly #(+ 50 (rand-int 100)))))}))
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

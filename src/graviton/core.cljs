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
     :render (fn [] [:canvas {:width 500 :height 500}])}))

(defn delta-x [{:keys [r theta]} delta]
  (* delta r (js/Math.cos theta)))

(defn delta-y [{:keys [r theta]} delta]
  (* delta r (js/Math.sin theta)))

(defn move-ship [{:keys [velocity] :as ship} {:keys [width height delta]}]
  (-> ship
      (update :x #(if (> % width) -1 (+ % (delta-x velocity delta))))
      (update :y #(if (> % height) -1 (+ % (delta-y velocity delta))))))

(defn update-actors [state]
  (postwalk
    (fn [node]
      (if (:sprite node)
        (let [updated-node ((:update node) node state)]
          (engine/move-sprite updated-node)
          updated-node)
        node))
    (:actors state)))

(defn update-game-state [state]
  (assoc state :actors (update-actors state)))

(def state (atom
                {:update update-game-state
                 :actors [{:id     :ship
                           :sprite (engine/sprite "ship.gif")
                           :velocity {:r 5
                                      :theta 0.1}
                           :x      0
                           :y      100
                           :update move-ship}]}))
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

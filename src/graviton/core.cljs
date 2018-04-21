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
    {:component-did-mount engine/init-canvas
     :render (fn [] [:canvas {:width 500 :height 500}])}))

(defn move-ship [ship width height]
  (-> ship
      (update :x #(if (> % width) -1 (inc %)))
      #_(update :y #(if (> % height) -1 (inc %)))))

(defn update-actors [actors width height]
  (postwalk
    (fn [node]
      (if (:sprite node)
        (let [updated-node ((:update node) node width height)]
          (engine/move-sprite updated-node)
          updated-node)
        node))
    actors))

(defn update-game-state [{:keys [width height] :as state}]
  (update state :actors update-actors width height))

(defn game []
  (r/with-let [state (atom
                       {:update update-game-state
                        :actors [{:id     :ship
                                  :sprite (engine/sprite "ship.gif")
                                  :x      0
                                  :y      100
                                  :update move-ship}]})]
    [:div
     [:h2 "Graviton"]
     [canvas state]]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [game] (.getElementById js/document "app")))

(defn init! []
  (mount-root))

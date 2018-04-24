(ns graviton.ui
  (:require [graviton.engine :as engine]))

(defn button [{:keys [label on-click x y width height]}]
  (let [text   (-> (js/PIXI.Text. label (js/PIXI.TextStyle.
                                          #js {:fill       "#FF00FF"
                                               :fontSize   30
                                               :fontFamily "Arial"}))
                   #_(engine/set-anchor 0.5 0.5))
        button (doto (js/PIXI.Graphics.)
                 #_(.addChild text)
                 (.on "pointerdown" (fn [& args] (js/alert "CLICKED!")) #_on-click)
                 (.lineStyle 2 0xFF00FF 1)
                 (.beginFill 0xFF00BB 0.25)
                 (.drawRoundedRect 0 0 width height 15)
                 (.endFill))]
    (set! (.-interactive button) true)
    (set! (.-buttonMode button) true)
    (set! (.-hitArea button) (js/PIXI.Rectangle. 0 0 width height))
    {:graphics button
     :x        x
     :y        y
     :init     (fn [{:keys [graphics] :as button} state]
                 (engine/set-graphics-position button)
                 (.addChild (:stage state) graphics))
     :update   (fn [])}))

(defn text-box [{:keys [text x y width height]}]
  {:graphics (js/PIXI.Text. text (js/PIXI.TextStyle.
                                   #js {:fill       "#FF00FF"
                                        :fontSize   30
                                        :fontFamily "Arial"}))
   :init     (fn [{:keys [graphics] :as text} state]
               (engine/set-graphics-position text)
               (.addChild (:stage state) graphics))
   :update   (fn [])})

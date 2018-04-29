(ns graviton.ui
  (:require [graviton.engine :as engine]
            [graviton.prizes :as prizes]))

(defn text-field [text size]
  (-> (js/PIXI.Text. text (js/PIXI.TextStyle.
                            #js {:fill       "#FF00FF"
                                 :fontSize   size
                                 :fontFamily "Arial"}))))

(defn text-box [{:keys [text x y]}]
  {:x        x
   :y        y
   :graphics (text-field text 30)
   :init     (fn [text state] text)
   :update   (fn [])})

(defn button [{:keys [label on-click x y width height]}]
  (let [text   (engine/set-anchor (text-field label 30) 0.5 0.5)
        button (doto (js/PIXI.Graphics.)
                 (.addChild (do
                              (set! (.-x text) (/ width 2))
                              (set! (.-y text) (/ height 2))
                              text))
                 (.on "pointerdown" on-click)
                 (.lineStyle 2 0xFF00FF 1)
                 (.beginFill 0xFF00BB 0.25)
                 (.drawRoundedRect 0 0 width height 15)
                 (.endFill))]
    (set! (.-interactive button) true)
    (set! (.-buttonMode button) true)
    {:graphics button
     :x        x
     :y        y
     :init     (fn [{:keys [graphics] :as button} state]
                 (engine/set-graphics-position button)
                 (.addChild (:stage state) graphics)
                 button)
     :update   (fn [])}))

(defn start-button [state parent-graphics]
  (let [graphics (doto (js/PIXI.Graphics.)
                   (.lineStyle 2 0xFF00FF 1)
                   (.beginFill 0xFF00BB 0.25)
                   (.drawRoundedRect -10 -3 120 40 15)
                   (.endFill)
                   (.addChild (text-field "START" 30))
                   (.on "pointerdown" #(do
                                         (.removeChild (:stage @state) parent-graphics)
                                         (vswap! state assoc :game-state :started)
                                         (vswap! state prizes/random-prizes)
                                         (engine/add-stage-on-click-event state)
                                         (engine/init-scene state))))]
    (set! (.-interactive graphics) true)
    (set! (.-buttonMode graphics) true)
    graphics))

(defn help-menu [state]
  (let [graphics     (js/PIXI.Graphics.)
        text-lines   ["Instructions:"
                      ""
                      "Drag to create gravity wells that will guide your ship to the prizes"
                      "The ship will start moving along the gravity fields"
                      "Keep placing attractors if you need to, but be careful! If you place too many, you'll create death zones that will end the game when touched!"
                      ""
                      "Have fun!"
                      ""
                      ""
                      "Game by Scot Brown & Dmitri Sotniov"]
        instructions (map-indexed (fn [idx text]
                                    (let [text (text-field text 15)]
                                      (set! (.-x text) 50)
                                      (set! (.-y text) (* 30 (inc idx)))
                                      text))
                                  text-lines)
        button       (start-button state graphics)]
    (doseq [line instructions]
      (.addChild graphics line))
    (set! (.-x button) (- (/ (:width @state) 2) 60))
    (set! (.-y button) (- (/ (:height @state) 2) 20))
    (.addChild graphics button)
    (.addChild (:stage @state) graphics)
    state))

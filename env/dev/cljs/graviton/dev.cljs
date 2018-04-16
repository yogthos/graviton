(ns ^:figwheel-no-load graviton.dev
  (:require
    [graviton.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)

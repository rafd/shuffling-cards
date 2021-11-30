(ns shuffleviz.core
  (:gen-class)
  (:require
    [bloom.omni.core :as omni]))

(def config
  {:omni/title "Card Shuffling Visualization"
   :omni/cljs {:main "shuffleviz.core"}})

(defn start! []
  (omni/start! omni/system config))

(defn stop! []
  (omni/stop!))

(defn -main []
  (start!))

(ns shuffleviz.core
  (:gen-class)
  (:require
    [bloom.commons.config :as config]
    [bloom.omni.core :as omni]))

(def config
  {:omni/title "Card Shuffling Visualization"
   :omni/cljs {:main "shuffleviz.core"}
   :omni/http-port (config/read "config.edn"
                                [:map
                                 [:http-port integer?]])})

(defn start! []
  (omni/start! omni/system config))

(defn stop! []
  (omni/stop!))

(defn -main []
  (start!))

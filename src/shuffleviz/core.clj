(ns shuffleviz.core
  (:gen-class)
  (:require
    [bloom.commons.config :as config]
    [bloom.omni.core :as omni]))

(def config
  (config/read "config.edn"
               [:map
                [:environment [:enum :prod :dev]]
                [:http-port integer?]]))

(def omni-config
  {:omni/title "Card Shuffling Visualization"
   :omni/cljs {:main "shuffleviz.core"}
   :omni/http-port (:http-port config)
   :omni/environment (:environment config)
   :omni/api-routes []})

(defn start! []
  (omni/start! omni/system omni-config))

(defn stop! []
  (omni/stop!))

(defn -main []
  (start!))

(defproject card-shuffling-viz "0.0.1"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.764"]
                 [io.bloomventures/omni "0.27.6"]
                 [io.bloomventures/commons "0.11.2"]
                 [cljsjs/d3 "6.2.0-0"]
                 [garden "1.3.9"]
                 [reagent "0.10.0"]

                 ;; dep fixes:
                 [com.fasterxml.jackson.core/jackson-core "2.11.2"]
                 [com.fasterxml.jackson.core/jackson-databind "2.11.2"]]

  :main shuffleviz.core

  :plugins [[io.bloomventures/omni "0.27.6"]]

  :omni-config shuffleviz.core/config

  :profiles {:uberjar
             {:aot :all
              :prep-tasks [["omni" "compile"]
                           "compile"]}})

(ns shuffleviz.ui.legend
  (:require
    [shuffleviz.misc :refer [color-scale]]))

(defn part [styles text]
  [:div {:style (merge {:text-align "center"
                        :min-width "3em"
                        :height "1.5em"
                        :line-height "1.5em"
                        :flex-grow 1}
                       styles)}
   text])

(defn legend-view []
  [:div.legend {:style {:position "fixed"
                        :display "flex"
                        :z-index 1000
                        :top 0
                        :left 0
                        :width "100%"}}
   [part {:background (color-scale 0) :color "white"} "Too Rare"]
   [part {:background (color-scale 0.1)}]
   [part {:background (color-scale 0.2)}]
   [part {:background (color-scale 0.3)}]
   [part {:background (color-scale 0.4)}]
   [part {:background (color-scale 0.5) :color "black"} "Random"]
   [part {:background (color-scale 0.6)}]
   [part {:background (color-scale 0.7)}]
   [part {:background (color-scale 0.8)}]
   [part {:background (color-scale 0.9)}]
   [part {:background (color-scale 1) :color "black"} "Too Frequent"]])

(ns shuffleviz.ui
  (:require
    [shuffleviz.explore :as explore]
    [shuffleviz.slides :as slides]))

(defn app-view []
  [:div
   [slides/slides-view]])

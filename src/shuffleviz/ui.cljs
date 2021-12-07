(ns shuffleviz.ui
  (:require
    [bloom.commons.ui.emoji-favicon :refer [emoji-favicon]]
    [shuffleviz.explore :as explore]
    [shuffleviz.slides :as slides]))

(defn app-view []
  [:div
   [emoji-favicon "🃏"]
   [slides/slides-view]])

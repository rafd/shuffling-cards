(ns ^:figwheel-hooks
  shuffleviz.core
  (:require
    [reagent.dom :as rdom]
    [shuffleviz.ui :as ui]))

(enable-console-print!)

(defn render
  []
  (rdom/render
    [ui/app-view]
    (js/document.getElementById "app")))

(defn ^:export init
  []
  (render))

(defn ^:after-load reload
  []
  (render))

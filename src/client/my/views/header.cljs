(ns my.views.header
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]))

(defui header []
  ($ :header.app-header
     ($ :img {:src "https://raw.githubusercontent.com/pitch-io/uix/master/logo.png"
              :width 32})))

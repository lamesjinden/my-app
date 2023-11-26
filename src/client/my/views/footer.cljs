(ns my.views.footer
  (:require
   [uix.core :as uix :refer [defui $]]
   [uix.dom]))

(defui footer []
  ($ :footer.app-footer
     ($ :small "made with "
        ($ :a {:href "https://github.com/pitch-io/uix"}
           "UIx"))))
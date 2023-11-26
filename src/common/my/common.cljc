(ns my.common
  (:require #?(:cljs [shadow.cljs.modern :as s])
            #?(:clj [clojure.string :as s])))

(defn say-hello [name]
  #?(:clj  (format "hello, %s! (from Java)" name)
     :cljs (s/js-template "hello, " name "! (from JS)")))
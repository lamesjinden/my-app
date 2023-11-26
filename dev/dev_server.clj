(ns dev-server
  (:require
   [clojure.pprint]
   [ring.middleware.reload :as reload]
   [ring.middleware.cors :as cors]
   [my.host :as host]
   [my.server :as server]))

(set! *warn-on-reflection* true)

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    (println "stopping server")

    (@server :timeout 100)
    (reset! server nil)))

(defn create-server [& args]
  (println "\ncreating dev server")
  (when args
    (println "  args:")
    (clojure.pprint/pprint args))

  (let [application-settings (host/gather-application-settings args)
        request-pipeline (-> (server/create-request-pipeline)
                             (reload/wrap-reload)
                             (cors/wrap-cors :access-control-allow-origin [#".*"]
                                             :access-control-allow-methods [:get :put :post :delete]))]

    (println "\ninitialize dev server app:")
    (when application-settings
      (println "  application-settings:")
      (clojure.pprint/pprint application-settings))

    (reset! server (server/create-server application-settings request-pipeline))))

(defn -main [& args]
  (apply create-server args))

(comment

  (create-server)

  (stop-server)

  ;
  )


(ns my.server
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [taoensso.timbre :as timbre :refer [debug error info infof trace]]
            [my.routing :as routing]))

;; todo - add route+handler for a static page templated with selmer
;; todo - add route+handler for a host page for SPA

(defn create-request-pipeline
  "returns the ring request-handling pipeline"
  []
  (let [ring-defaults (-> api-defaults
                          (assoc :static {:resources "public"}))]
    (-> #'routing/request-handler
        (wrap-defaults  ring-defaults)
        (wrap-json-response))))

(defn- server-settings->display-strs [server-settings]
  (cond-> []
    (:ip server-settings)
    (conj (str "IP:\t" (:ip server-settings)))

    (:max-body server-settings)
    (conj (str "Max Body Size (bytes):\t" (:max-body server-settings)))

    (:max-line server-settings)
    (conj (str "Max Line Length:\t" (:max-line server-settings)))

    (:port server-settings)
    (conj (str "Port:\t" (:port server-settings)))

    (:queue-size server-settings)
    (conj (str "Queue Size:\t" (:queue-size server-settings)))

    (:thread server-settings)
    (conj (str "Threads:\t" (:thread server-settings)))

    (:worker-name-prefix server-settings)
    (conj (str "Worker Prefix:\t" (:worker-name-prefix server-settings)))))

(defn gather-server-settings [application-settings]
  (let [server-settings (select-keys application-settings [:ip :port :thread :worker-name-prefix :queue-size :max-body :max-line])]
    (doseq [s (server-settings->display-strs server-settings)]
      (debug s))
    server-settings))

(defn create-server [application-settings request-pipeline]
  (let [server-settings (gather-server-settings application-settings)]
    (debug "Running server...")

    (let [disposable (run-server request-pipeline server-settings)]
      (debug "Server running.")

      disposable)))

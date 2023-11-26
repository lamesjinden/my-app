(ns my.host
  (:require [clojure.edn :as edn]
            [babashka.cli :as cli]
            [babashka.fs :as fs]
            [my.server :as server]
            [taoensso.timbre :as timbre :refer [debug error fatal info infof trace]])
  (:gen-class))

(def default-log-level :error)

(def default-options
  {:port       8090
   :ip         "127.0.0.1"
   :working-directory "."})

(def cli-errors! (atom []))

(defn cli-error-fn [{:keys [_spec type cause msg _option] :as data}]
  (if (= :org.babashka/cli type)
    (condp = cause
      :require
      (swap! cli-errors! conj (str "Missing required argument:\t" msg))

      :validate
      (swap! cli-errors! conj (str "Invalid argument:\t" msg))

      :restrict
      (swap! cli-errors! conj (str "Restricted argument:\t" msg))

      :coerce
      (swap! cli-errors! conj (str "Failed Coercion:\t" msg))

      :else
      (swap! cli-errors! conj (str "Argument error:\t" cause ";\t" msg)))
    (throw (ex-info msg data))))

(def cli-spec {:help
               {:ref          "<help>"
                :desc         "Display the Help description."
                :alias        :h
                :coerce       :keyword}

               :configuration-file
               {:ref "<configuration-file>"
                :desc "Path to the configuration file."
                :alias :c
                :validate {:pred #(fs/exists? %)
                           :ex-msg (fn [m] (str "Configuration file does not exist: " (:value m)))}}

               :verbose
               {:ref          "<verbosity>"
                :desc         "Logging verbosity. <verbosity> can be provided as a switch (-v or --verbose) or key=val (--verbosity=false)."
                :alias        :v
                :coerce       []}

               :working-directory
               {:ref "<working-directory>"
                :desc "Working directory for the server application."
                :alias :d}

               :ip
               {:ref "<ip>"
                :desc "IP address of the interface for the server application."
                :alias :i}

               :port
               {:ref "<port>"
                :desc "Port for the server application."
                :alias :p
                :coerce :long
                :validate {:pred #(< 0 % 0x10000)
                           :ex-msg (fn [m] (str "Not a valid port number: " (:value m)))}}

               :thread
               {:ref "<thread>"
                :desc "Number of request handling threads."
                :coerce :long
                :validate {:pred pos-int?
                           :ex-msg (fn [m] (str "Not a valid thread number: " (:value m)))}}

               :worker-name-prefix
               {:ref "<worker-name-prefix>"
                :desc "Worker thread name prefix."}

               :queue-size
               {:ref "<queue-size>"
                :desc "Maximum number of pending requests."
                :coerce :long
                :validate {:pred pos-int?
                           :ex-msg (fn [m] (str "Not a valid queue size: " (:value m)))}}

               :max-body
               {:ref "<max-body>"
                :desc "Maximum allowed request body length; specified in bytes."
                :coerce :long
                :validate {:pred pos-int?
                           :ex-msg (fn [m] (str "Not a valid maximum body size: " (:value m)))}}

               :max-line
               {:ref "<max-line>"
                :desc "Maximum allowed request header length; specified in characters."
                :coerce :long
                :validate {:pred pos-int?
                           :ex-msg (fn [m] (str "Not a valid maximum line size: " (:value m)))}}})

(defn- args->opts [args]
  (cli/parse-opts args {:spec cli-spec
                        :error-fn cli-error-fn}))

(defn- read-config-file [config-file-path]
  (try
    (-> config-file-path
        slurp
        edn/read-string)
    (catch Exception _
      {})))

(defn gather-application-settings [cli-args]
  (let [cli-settings (args->opts cli-args)
        config-file-settings (or (some-> (:configuration-file cli-settings)
                                         (read-config-file))
                                 {})
        settings (merge default-options config-file-settings cli-settings)]
    settings))

(defn configure-logging [application-settings]
  (let [verbose (:verbose application-settings)
        min-level (cond
                    (>= (count verbose) 3) :trace
                    (= (count verbose) 2) :debug
                    (= (count verbose) 1) :info
                    :else default-log-level)]
    (timbre/set-min-level! min-level)))

(def dispose-server! (atom nil))

(defn- stop-server []
  (when-let [disposable @dispose-server!]
    (debug "Stopping server...")

    (disposable :timeout 100)

    (debug "Server stopped.")
    (reset! dispose-server! nil))
  (shutdown-agents))

(defn- start-server [application-settings]
  (configure-logging application-settings)

  (let [request-pipeline (server/create-request-pipeline)
        disposable (server/create-server application-settings request-pipeline)]
    (reset! dispose-server! disposable))

  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. ^Runnable #'stop-server)))

(defn -main [& args]
  (let [application-settings (gather-application-settings args)]
    (configure-logging application-settings)
    (debug application-settings)
    (cond
      (:help application-settings)
      (println (cli/format-opts {:spec cli-spec}))

      (seq @cli-errors!)
      (do
        (doseq [e @cli-errors!]
          (fatal e))
        (System/exit 1))

      :else
      (try
        (start-server application-settings)
        (catch Exception e
          (fatal e)
          (System/exit 2))))))

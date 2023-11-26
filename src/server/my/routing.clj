(ns my.routing
  (:require [clojure.java.io :as io]
            [my.util :as util]
            [ring.util.response :as ring-util]
            [selmer.parser]))

(defn handle-not-found [{:keys [uri] :as _request}]
  (util/create-not-found uri))

(def index-local-path "public/index.html")
(def templates-directory-path "resources/public/templates")
(def admin-templates-directory-path (format "%s/admin" templates-directory-path))

(defn handle-root-request [{:keys [uri] :as _request}]
  (util/->html-response (slurp (io/resource index-local-path))))

(defn handle-api-account-request [{:keys [uri] :as _request}]
  (ring-util/response {:username "admin"
                       :user-id "00001"}))

(defn handle-admin-root-request [{:keys [uri] :as _request}]
  (let [admin-root-template-path (format "%s/dashboard.html" admin-templates-directory-path)]
    (-> (slurp admin-root-template-path)
        (selmer.parser/render {:username "PlayerOne"})
        (util/->html-response))))

(def routes {:root        {:get handle-root-request}
             :api-account {:get handle-api-account-request}
             :admin-root  {:get handle-admin-root-request}
             :not-found   nil})

(defn router [uri]
  (cond
    (= uri "/")
    :root

    (= uri "/api/account")
    :api-account

    (contains? #{"/admin" "/admin/"} uri)
    :admin-root

    :else :not-found))

(defn request-handler [request]
  (let [uri (:uri request)
        method (:request-method request)
        handler (as-> (router uri) $
                  (get routes $ {})
                  (get $ method handle-not-found))]
    (handler request)))
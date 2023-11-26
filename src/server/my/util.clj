(ns my.util
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [ring.util.response :as resp])
  (:import (java.io PrintWriter StringWriter)
           (java.nio.file Path)
           (java.util.zip ZipEntry ZipOutputStream)))

(defn exception-stack [e]
  (let [sw (new StringWriter)
        pw (new PrintWriter sw)]
    (.printStackTrace e pw)
    (str "Exception :: " (.getMessage e) (-> sw .toString))))

(defn create-not-found [uri-or-page-name]
  (-> (resp/not-found (str "Not found " uri-or-page-name))
      (resp/content-type "text")))

(defn create-ok []
  (-> "thank you"
      (resp/response)
      (resp/content-type "text/html")))

(defn create-not-available [body]
  (-> body
      (resp/response)
      (resp/status 503)))

(defn ->html-response [html]
  (-> html
      (resp/response)
      (resp/content-type "text/html")))

(defn ->json-response [json]
  (-> json
      (resp/response)
      (resp/content-type "application/json")))

(defn content-disposition
  "Returns an updated Ring response with the a Content-Disposition header corresponding
  to the given content-disposition."
  [resp content-disposition]
  (ring.util.response/header resp "Content-Disposition" content-disposition))

(defn ->zip-file-response [^Path zip-file-path]
  (let [content-type "application/zip"
        fileName (str (.getFileName zip-file-path))
        content-disposition-value (format "attachment; filename=%s" fileName)]
    (-> (str zip-file-path)
        (resp/file-response)
        (resp/content-type content-type)
        (content-disposition content-disposition-value))))

(defn zip-directory
  "see https://stackoverflow.com/a/27066626
  modifications made such that the resulting hierarchy within the zip is relative to relative-to-path,
  otherwise zip entries were nested in absolute system paths"
  [source-directory-str destination-file-str relative-to-path]
  (with-open [zip (ZipOutputStream. (io/output-stream destination-file-str))]
    (doseq [f (file-seq (io/file source-directory-str)) :when (.isFile f)]
      (let [f-absolute-path (.toPath f)
            f-relative-path (.relativize relative-to-path f-absolute-path)]
        (.putNextEntry zip (ZipEntry. (str f-relative-path)))
        (io/copy f zip)
        (.closeEntry zip)))))

(defn nonblank [x & args]
  (if (not (str/blank? x))
    x
    (some (fn [arg] (and (not (str/blank? arg)) arg)) args)))
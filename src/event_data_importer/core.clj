(ns event-data-importer.core
  (:require [event-data-importer.scholix :refer [scholix->event]]
            [event-data-common.event-bus :as bus]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [config.core :refer [env]]
            [clojure.java.io :refer [writer reader as-file file]]))

(defn scholix-file
  [input-filename output-filename]
  (let [input-file (as-file input-filename)
        output-file (as-file output-filename)]

    (when-not (.exists input-file)
      (log/error "Input file does not exist! Exiting.")
      (System/exit 1))

    (with-open [out (writer output-file)
                in (reader input-file)]
      (let [objects (->> in line-seq (map #(json/read-str % :key-fn keyword)))
            events (map scholix->event objects)]
        (doseq [event events]
          (.write out (json/write-str event))
          (.write out "\n"))))))

(defn scholix-dir
  [input-dirname output-dirname]
  (let [files (-> input-dirname file .listFiles)
        output-dir (file output-dirname)]

    (doseq [f files]
      (log/info "Process" f)
      ; Same filename in corresponding output dir.
      (scholix-file f (file output-dir (.getName f))))))

(defn upload
  [input-files]
  (log/info "Upload" (count input-files) "files")
  (doseq [input-file input-files]
    (log/info "Uploading" input-file)
      (with-open [in (reader input-file)]
        (let [events (->> in line-seq (map #(json/read-str % :key-fn keyword)))]
          (doseq [event events]
            (bus/post-event event))))))

(defn -main
  [& args]
  (condp = (first args)
    "scholix-file" (scholix-file (nth args 1) (nth args 2))
    "scholix-dir" (scholix-dir (nth args 1) (nth args 2))
    "upload" (upload (rest args))

    :default (log/error "Didn't recognise command.")))


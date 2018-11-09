(ns event-data-importer.scholix
  "Transform Scholix link packages into Events.
   This is intended to be run manually and tweaked until we get reasonable coverage."
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [crossref.util.doi :as cr-doi]
            [clj-time.coerce :as clj-time-coerce]
            [clj-time.format :as clj-time-format]
            [clojure.string :refer [blank?]])
  (:import [java.util UUID]))

(def date-format
  (:date-time-no-ms clj-time-format/formatters))


(def source-token
  "36d164c8-ebda-441c-a79f-8173ea082107")

(def relationship-types
  {"IsReferencedBy" "is_referenced_by"})

; https://raw.githubusercontent.com/lagotto/lagotto/master/db/seeds/development/work_types.yml

(def work-types
  "Accept tuple of [type, subtype], where subtype can be nil."
  {
    ["dataset" nil] "dataset"
    ["literature" "journal article"] "article-journal"})

(defn ->metadata
  "Construct subj or obj metadata object from target or destination."
  [input]
  (let [alternative-id (-> input :Identifier :ID)
        alternative-id-type (-> input :Identifier :IDScheme)
        work-type-id (-> input :Type ((juxt :Name :SubType)) work-types)
        title (-> input :Title)
        issued (some->>
                input
                :PublicationDate
                clj-time-coerce/from-string
                (clj-time-format/unparse date-format))

        ; Don't need to include alternative-id if it's a DOI.
        alternative-not-doi (not (#{"DOI" "doi"} alternative-id-type))]

      (when (and (-> input :Type) (not work-type-id))
        (log/error "Didn't recognise type for" (-> input :Type)))

; (log/info "Work type" (-> input :Type) "=>" (-> input :Type ((juxt :Name :SubType))) "=>" work-type-id)

    (cond-> {}
      (and alternative-id alternative-not-doi) (assoc :alternative_id alternative-id)
      (and alternative-id-type alternative-not-doi) (assoc :alternative_id_type alternative-id-type)
      work-type-id (assoc :work_type_id work-type-id)
      issued (assoc :issued issued)
      (not (blank? title)) (assoc :title title))))
  

(def TODO nil)
(defn scholix->event
  [input]


  ; TODO normalize DOIs if present

  (let [subj-id (-> input :Source :Identifier :IDURL)
        obj-id (-> input :Target :Identifier :IDURL)
        
        subj (-> input :Source ->metadata (assoc :pid subj-id))
        obj (-> input :Target ->metadata (assoc :pid obj-id))
        
        occurred-at (->> input :LinkedPublicationDate clj-time-coerce/from-string (clj-time-format/unparse date-format))

        ; Lookup may result in nil
        relation-type-id (-> input :RelationshipType :Name relationship-types)

        license-url (-> input :LicenseURL)

        ; These fields must be present
        ok (and relation-type-id )

        ; Generate the Event ID from the subj id, the obj, id and, if present, the provider.
        ; This means we can re-process the same input and de-dupe.
        id (str
            (UUID/nameUUIDFromBytes
              (.getBytes (str subj-id "~" obj-id (:LinkProvider input))
                        "UTF-8")))

        result 
        {:subj_id subj-id
         :obj_id obj-id
         :license license-url
         :source_token source-token
         :occurred_at occurred-at
         :id id
         :action "add"
         :source "crossref"
         :subj subj
         :obj obj
         :relation_type_id relation-type-id}]

        (when-not ok
          (log/error "Failed" input))

        (when-not relation-type-id
          (log/error "Didn't recognise relation."))

        (when-not ok
          (log/error "Failed!" input))

        (when ok result)))


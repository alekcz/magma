(ns magma.core
  (:require [clojure.java.io :as io]
            [google-credentials.core :as g]
            [tick.alpha.api :as t])
  (:import 	;storage bucket
            com.google.cloud.storage.Bucket
            com.google.cloud.storage.BucketInfo
            com.google.cloud.storage.Storage$BucketTargetOption
            com.google.cloud.storage.Storage
            com.google.cloud.storage.StorageOptions
            com.google.cloud.storage.Storage$BlobListOption
            ;firestore
            com.google.firestore.admin.v1.DatabaseName
            com.google.longrunning.Operation
            com.google.cloud.firestore.v1.FirestoreAdminClient
            com.google.cloud.firestore.v1.FirestoreAdminSettings
            com.google.firestore.admin.v1.ExportDocumentsRequest
            com.google.firestore.admin.v1.ImportDocumentsRequest
            com.google.api.gax.core.FixedCredentialsProvider
            )
  (:gen-class))

(t/zone "Africa/Johannesburg")

(def root "magma-")

(defn- storage ^Storage [] 
  (-> (. StorageOptions newBuilder)
      (.setCredentials (g/load-credentials)) 
      (.build) 
      (.getService)))

(defn- admin-settings ^FirestoreAdminSettings [] 
  (-> (. FirestoreAdminSettings newBuilder)
      (.setCredentialsProvider (FixedCredentialsProvider/create (g/load-credentials)))
      (.build)))

(defn- admin-client ^FirestoreAdminClient []
  (FirestoreAdminClient/create (admin-settings)))

(defn- export-request ^ExportDocumentsRequest [name bucket]
  (-> (. ExportDocumentsRequest newBuilder)
      (.setName name)
      (.setOutputUriPrefix bucket)
      (.build)))

(defn- import-request ^ImportDocumentsRequest [name bucket]
  (-> (. ImportDocumentsRequest newBuilder)
      (.setName name)
      (.setInputUriPrefix bucket)
      (.build)))

(defn- bucket-info ^BucketInfo [name] (BucketInfo/of name))

(defn- create-backup-bucket []
  (let [project (.getProjectId (g/load-credentials))]
    (.create (storage) (bucket-info (str root project)) (into-array Storage$BucketTargetOption []))))

(defn- make-timestamp [folder-name] 
  (-> folder-name
    (clojure.string/replace #"/(?!.*\1)" "")
    (clojure.string/replace #"_(?!.*\1)" ".")
    (t/instant)))

(defn- backup [project-id bucket-uri]
    (try 
      (let [client (admin-client)
            name  (DatabaseName/of  project-id "firestore")]
        (.exportDocuments client (export-request (.toString name) bucket-uri)))
      (catch Exception e (str "Caught exception: " (.getMessage e)))))

(defn- restore [project-id backup-uri]
    (try 
      (let [client (admin-client)
            name  (DatabaseName/of  project-id "firestore")]
        (.importDocuments client (import-request (.toString name) backup-uri)))
      (catch Exception e (str "Caught exception: " (.getMessage e)))))

(defn- list-backups [project-id]
  (let [page (.list (storage) (str root project-id) (into-array Storage$BlobListOption [(Storage$BlobListOption/currentDirectory)]))]
    (let [data (seq (.iterateAll page))]
       (sort-by :timestamp
        (for [item data]
          (let [mapped (bean item)]
            {:timestamp (make-timestamp (:name mapped)) :uri (str "gs://"(:bucket mapped) "/" (:name mapped))}))))))

(defn- last-backup [project-id]
  (last (list-backups project-id)))

(defn list-firestore-backups 
  ([]
    (list-backups (.getProjectId (g/load-credentials))))
  ([project-id]
    (list-backups project-id)))

(defn last-firestore-backup
  ([]
    (last-backup (.getProjectId (g/load-credentials))))
  ([project-id]
    (last-backup project-id)))

(defn backup-firestore 
  ([]
    (let [project-id (.getProjectId (g/load-credentials))]
      (backup project-id (str "gs://" root project-id))))
  ([bucket]
    (backup (.getProjectId (g/load-credentials)) bucket))
  ([project-id bucket]
    (backup project-id bucket)))

(defn restore-firestore 
  ([backup-uri]
    (let [project-id (.getProjectId (g/load-credentials))]
      (restore project-id backup-uri)))
  ([project-id backup-uri]
    (restore project-id backup-uri)))

(defn roll-back-firestore 
  ([]
    (let [uri (:uri (last-firestore-backup (.getProjectId (g/load-credentials))))]
      (restore-firestore uri)))
  ([project-id]
    (let [uri (:uri (last-firestore-backup project-id))]
      (restore-firestore uri))))

;(into-array Storage$BucketTargetOption [])
(ns magma.core
  (:require [clojure.java.io :as io]
            [google-credentials.core :as g]
            [tick.alpha.api :as t]
            [environ.core :refer [env]])
  (:import 	;storage bucket
            com.google.cloud.storage.Bucket
            com.google.cloud.storage.BucketInfo
            com.google.cloud.storage.Blob
            com.google.cloud.storage.BlobId
            com.google.cloud.storage.Storage$BucketTargetOption
            com.google.cloud.storage.Storage
            com.google.cloud.storage.StorageOptions
            com.google.cloud.storage.Storage$BlobListOption
            com.google.cloud.storage.Storage$BucketSourceOption
            com.google.cloud.storage.Storage$BlobSourceOption
            ;firestore
            com.google.firestore.admin.v1.DatabaseName
            com.google.longrunning.Operation
            com.google.cloud.firestore.v1.FirestoreAdminClient
            com.google.cloud.firestore.v1.FirestoreAdminSettings
            com.google.firestore.admin.v1.ExportDocumentsRequest
            com.google.firestore.admin.v1.ImportDocumentsRequest
            com.google.api.gax.core.FixedCredentialsProvider)
  (:gen-class))

(def root (atom "magma-"))

(defn- project-id [] (env :google-cloud-project))

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

(defn- make-timestamp [folder-name] 
  (-> folder-name
    (clojure.string/replace #"/(?!.*\1)" "")
    (clojure.string/replace #"_(?!.*\1)" ".")
    (t/instant)))

(defn- backup [bucket-uri]
  (let [client (admin-client)
        name  (DatabaseName/of (project-id) "firestore")]
    (.exportDocuments client (export-request (.toString name) bucket-uri))))
  
(defn- restore [backup-uri]
  (let [client (admin-client)
        name  (DatabaseName/of (project-id) "firestore")]
    (.importDocuments client (import-request (.toString name) backup-uri))))

(defn- list-backups []
  (let [page (.list (storage) (str @root (project-id)) (into-array Storage$BlobListOption [(Storage$BlobListOption/currentDirectory)]))]
    (let [data (seq (.iterateAll page))]
      (sort-by :timestamp
        (for [item data]
          (let [mapped (bean item)]
            { :id (:blobId mapped)
              :name (:name mapped)
              :bucket (:bucket mapped)
              :timestamp (make-timestamp (:name mapped)) 
              :uri (str "gs://" (:bucket mapped) "/" (:name mapped))}))))))

(defn- last-backup []
  (last (list-backups)))

(defn- list-sub-directory [folder]
  (let [page (.list (storage) (str @root (project-id)) (into-array Storage$BlobListOption [(Storage$BlobListOption/prefix folder)]))]
    (let [data (seq (.iterateAll page))]
        (sort-by :name
          (for [item data]
            (let [mapped (bean item)]
              { :id (:blobId mapped)
                :name (:name mapped)
                :uri (str "gs://" (:bucket mapped) "/" (:name mapped))}))))))

(defn- delete-backup [folder]
  (let [contents (list-sub-directory folder)]
    (doseq [file contents]
      (.delete (storage) (:id file) (into-array Storage$BlobSourceOption [])))))                              

(defn create-backup-bucket []
  (.create (storage) (bucket-info (str @root (project-id))) (into-array Storage$BucketTargetOption [])))

(defn deleting-your-backup-bucket-is-an-extremely-bad-idea  []
  (let [backups (list-backups)]
    (doseq [backup backups]
      (delete-backup (:name backup)))
    (Thread/sleep 5000)
    (.delete (storage) (str @root (project-id)) (into-array Storage$BucketSourceOption []))))

(defn rename-root [prefix]
  (let [clean-prefix (clojure.string/replace (str prefix) #"[!@#$%^&*\\\/]" "")]
    (reset! root (str "magma-" clean-prefix "-"))))

(defn list-firestore-backups []
    (list-backups))

(defn last-firestore-backup []
  (last-backup))

(defn backup-firestore []
  (backup (str "gs://" @root (project-id))))

(defn restore-firestore [backup-uri]
    (restore backup-uri))

(defn roll-back-firestore []
    (let [uri (:uri (last-firestore-backup))]
      (Thread/sleep 60000)
      (restore-firestore uri)))

(defn deleting-your-backups-is-a-really-bad-idea [backup-name]
    (delete-backup backup-name))

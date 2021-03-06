(ns magma.core-test
  (:require [clojure.test :refer :all]
            [magma.core :as magma]
            [google-credentials.core :as g]
            [clj-uuid :as uuid]))

(def short-wait 5000)
(def long-wait 20000)

(defn fixture [f]
	(let [prefix (uuid/v1)
        _ (magma/rename-root prefix)]
    (magma/create-backup-bucket)
    (Thread/sleep short-wait)
	  (f)
    (Thread/sleep long-wait)
    (magma/deleting-your-backup-bucket-is-an-extremely-bad-idea)
    (Thread/sleep long-wait)))

(use-fixtures :each fixture)

(deftest backup-test
  (testing "Make 2 backups and then delete them"
    (let [project-id (.getProjectId (g/load-credentials))]
      (magma/backup-firestore)
      (Thread/sleep short-wait)
      (magma/backup-firestore)
      (Thread/sleep short-wait)
      (magma/roll-back-firestore)
      (Thread/sleep short-wait)
      (let [backup-list (magma/list-firestore-backups)
            first-backup (first (magma/list-firestore-backups))
            last-backup (magma/last-firestore-backup)]
        (magma/restore-firestore (:uri first-backup))
        (Thread/sleep short-wait)
        (is (= 2 (count backup-list)))
        (is (= last-backup (last backup-list)))
        (doseq [b backup-list]
          (is (clojure.string/includes? (:bucket b) (str @magma/root))))
        (magma/deleting-your-backups-is-a-really-bad-idea (:name first-backup))
        (Thread/sleep short-wait)
        (magma/deleting-your-backups-is-a-really-bad-idea (:name last-backup))
        (Thread/sleep short-wait)
        (is (= 0 (count (magma/list-firestore-backups))))))))
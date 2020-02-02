(defproject magma "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [ [org.clojure/clojure "1.10.0"]
                  [com.google.cloud/google-cloud-storage "1.22.0"]
                  [alekcz/google-credentials "0.2.1"]
                  [com.google.cloud/google-cloud-firestore "1.32.2"]
                  [com.google.api/gax "1.53.1"]
                  [tick "0.4.23-alpha"]]
  :repl-options {:init-ns magma.core})

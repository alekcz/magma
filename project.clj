(defproject alekcz/magma "0.1.0"
  :description "A Clojure library to back up your Firestore to Google Storage"
  :url "https://github.com/alekcz/magma"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [ [org.clojure/clojure "1.10.0"]
                  [alekcz/google-credentials "0.2.1"]
                  [com.google.api/gax "1.53.1"]
                  [com.google.errorprone/error_prone_annotations "2.0.2"]
                  [io.grpc/grpc-netty-shaded "1.22.1" :exclusions [com.google.errorprone/error_prone_annotations io.grpc/grpc-core]]
                  [io.grpc/grpc-protobuf "1.22.1"]
                  [io.grpc/grpc-stub "1.22.1"]
                  [io.grpc/grpc-api "1.22.1"]
                  [io.grpc/grpc-core "1.22.1" :exclusions [com.google.errorprone/error_prone_annotations io.grpc/grpc-api]]
                  [com.google.cloud/google-cloud-storage "1.22.0" :exclusions [io.grpc/grpc-netty-shaded io.grpc/grpc-core io.grpc/grpc-api]]
                  [com.google.cloud/google-cloud-firestore "1.32.2" :exclusions [io.grpc/grpc-netty-shaded io.grpc/grpc-core io.grpc/grpc-api]]
                  
                  [tick "0.4.23-alpha"]]
  :repl-options {:init-ns magma.core})

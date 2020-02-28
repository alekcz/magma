# magma

A Clojure library to back up your Firestore to Google Storage

![Clojure CI](https://github.com/alekcz/magma/workflows/Clojure%20CI/badge.svg) [![codecov](https://codecov.io/gh/alekcz/magma/branch/master/graph/badge.svg)](https://codecov.io/gh/alekcz/magma)  

[![Clojars Project](https://img.shields.io/clojars/v/alekcz/magma.svg)](https://clojars.org/alekcz/magma)


## Usage

Environment variables:
1. GOOGLE_CLOUD_PROJECT contains the name of the project containing both firestore and the cloud storage
2. GOOGLE_APPLICATION_CREDENTIALS contains the `.json` credential for the service account being used. 

`magma` uses [alekcz/google-credentials](https://github.com/alekcz/google-credentials) to load Google Credentials from the `GOOGLE_APPLICATION_CREDENTIALS`

```clojure 

[alekcz/magma "0.3.0"]

(require '[magma.core :as magma])


(magma/create-backup-bucket)
;by default the root is called magma-<project-id>. 
;The bucket is called named after the root

(magma/list-firestore-backups)

(magma/last-firestore-backup)

(magma/backup-firestore)
(magma/backup-firestore "gs://bucket-name")

(magma/restore-firestore "gs://bucket-name/yyyy-MM-ddTHH:mm:ss.SSS")

(magma/roll-back-firestore) ;for your safety magma waits 60 seconds before starting the roll back

(magma/deleting-your-backups-is-a-really-bad-idea "yyyy-MM-ddTHH:mm:ss_SSS")

;Renaming the root
(magma/rename-root "namespace")
; The root is now magma-namespace-<project-id>
; This function primarily exists to enable testing
```

I'm currently using `magma` to backup my DB just before I deploy a change to my production API.

## License

Copyright © 2020 Alexander Oloo

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

# magma

A Clojure library to back up your Firestore to Google Storage

## Usage

```clojure 
(list-firestore-backups)
(list-firestore-backups "project-id")

(last-firestore-backup)
(last-firestore-backup "project-id")

(backup-firestore)
(backup-firestore "gs://bucket-name")
(backup-firestore "project-id" "gs://bucket-name")

(restore-firestore "gs://bucket-name/yyyy-MM-ddTHH:mm:ss.SSS")
(restore-firestore "project-id" "gs://bucket-name/yyyy-MM-ddTHH:mm:ss.SSS")

(roll-back-firestore) ;for your safety magma waits 60 seconds before starting the roll back
(roll-back-firestore "project-id") ;for your safety magma waits 60 seconds before starting the roll back
```

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

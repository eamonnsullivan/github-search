# github-search

A small library for searching Github. Initially just provides methods for searching an organisation for repositories tagged with particular topics.

## Usage

``` clojure
(def result (get-repos (System/getenv "GITHUB_ACCESS_TOKEN") "my-org" ["topic1" "topic2"]))
(spit "repos.edn" result)
```
The EDN returned will contain basic information about the repos found. For example:

``` edn
[{:name "project1",
  :description
  "A description",
  :url "https://github.com/my-org/project1",
  :sshUrl "git@github.com:my-org/project1.git",
  :updatedAt "2020-04-09T11:01:55Z",
  :languages ["Javascript" "Python" "HTML"]}
 {:name "project2",
  :description "A description for project2",
  :url "https://github.com/my-org/project2",
  :sshUrl "git@github.com:my-org/project2.git",
  :updatedAt "2020-04-09T11:02:28Z",
  :languages ["Clojure" "ClojureScript"]}
]
```

## Development

To build a deployable jar of this library:

    $ clojure -A:jar

To run tests:

    $ clj -A:test:runner

To publish a version to Clojars:

    $ clojure -A:jar
    $ mvn deploy:deploy-file -Dfile=github-search.jar -DpomFile=pom.xml -DrepositoryId=clojars -Durl=https://clojars.org/repo/

## License

Copyright © 2020 Eamonn

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

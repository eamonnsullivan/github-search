# github-search

A small library for searching Github. Initially just provides methods for searching an organisation for repositories tagged with particular topics.

## Usage

You will need an environmental variable, called GITHUB_ACCESS_TOKEN, with a valid access token. At the moment, the access token needs read:org and repo permissions.

``` clojure
(def result (get-repos "bbc" ["dpub" "10percent"]))
(spit "repos.json" result)
```
The JSON return will contain basic information about the repos found. For example:

``` json
[
  {
    "name": "cosmos-login",
    "description": "Log in (ssh) directly to an instance of an application on a specified environment",
    "url": "https://github.com/my-org/cosmos-login",
    "sshUrl": "git@github.com:bbc/cosmos-login.git",
    "languages": [ "Clojure" ]
  },
  {
    "name": "other-repo",
    "description": "A description",
    "url": "https://github.com/my-org/other-repo",
    "sshUrl": "git@github.com:bbc/other-repo.git",
    "languages": [ "Scala" ]
  },
  {
    "name": "another-project",
    "description": "With a description",
    "url": "https://github.com/my-org/another-project",
    "sshUrl": "git@github.com:bbc/another-project.git",
    "languages": [ "Javascript", "Python", "HTML" ]
  }
]
```

## Development

To build a deployable jar of this library:

    $ clojure -A:jar

To run tests:

    $ clj -A:test:runner

## License

Copyright © 2020 Eamonn

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

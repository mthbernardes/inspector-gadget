# inspector-gadget

A Leiningen plugin responsible for finding possible vulnerabilities.

## Usage

Use this for user-level plugins:

Put `[inspector-gadget "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your `:user`
profile.

Use this for project-level plugins:

Put `[inspector-gadget "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

Execute it on your project directory.
`$ lein inspector-gadget`

## Vulnerabilities
- XXE using clojure.xml/parse
- RCE using clojure.core/read-string
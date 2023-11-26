### Full Stack Application Template

#### todo

- add a server route to utilize selmer to render a page

<p></p>

#### Overview

A full-stack web application in Clojure and Clojurescript.

#### Goals

Create a project template using 'modern' cljs technologies that can take
advantage of npm, 'modern' react (i.e. hooks) and the 'modern' react eco-system

- component libraries
- theming

#### Features

- uses babashka task runner to implement high-level development tasks
- uses shadow-cljs to enable access to npm dependencies
- uses uix to take advantage of modern react (18.2+)
- uses lightnightcss to minimize and bundle css

#### Development

**TLDR**

> start a development server: `bb run-dev-server`
>
> start a development client build: `bb run-dev-client`

- The client application can be developed using 'watch' mode or with a connected nrepl
- Similarly, the server application can be developed using 'reload' mode or with a connected repl

##### Client Development

using Watch mode:

- utilizes shadow-cljs watch command to detect changes to source files and reload them in the active browser connection
- run `bb run-dev-client` to start the shadow-cljs watch process
  - the above command also runs a separate watch process that executes `lightningcss` to update styles

using nrepl:

- utilizes the nrepl server provided by shadow-cljs
- if using VS Code with Calva, execute 'Start a project REPL and Connect'
  - after answering the prompts, this will connect to a shadow-cljs nrepl, and switch to a clojurescript repl
- if using Cursive:
  - run `bb run-dev-client` (same command as above) - this lauches the 'watch' build _and_ an nrepl server
  - create and run a configuration for a Remote REPL (type=nrepl, select appropriate context module, use port from repl file='use standard')
  - use `dev_client.clj` to switch to a clojurescript repl

##### Server Development

using Reload mode:

- utilizes ring/wrap-repload to watch for changes in server source files and loads the changes
- configured and started through `dev_server.clj`
- run `bb run-dev-server` to start the server application configured for development

using nrepl:

- utilizes cider-nrepl and nrepl.main to start an nrepl server
- if using VS Code with Calva, execute 'Start a project REPL and Connect'
  - select all relevant aliases from the prompt - including :nrepl-main (ignore the warning as this alias _does_ start an nrepl session)
- if using Cursive:
  - run `bb run-dev-server-repl`
  - create and run an configuration for a Remote REPL (type=nrepl, select appropriate context module, use port from repl file='use standard')
- no matter the choice of editor integration, use `dev_server.clj` to create and start the server application

##### A Note on Editors

With additional configuration, it should be possible to use a single VS Code instance to access
the REPL for both, client and server applications. See links below.

related:

- [calva: shadow-cljs](https://calva.io/shadow-cljs/)
- [shadow-cljs and calva nrepl basics](https://blog.agical.se/en/posts/shadow-cljs-clojure-cljurescript-calva-nrepl-basics/)
- [calva: vscode with two windows](https://calva.io/workspace-layouts/#one-folder-two-windows)

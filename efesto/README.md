# Efesto
-------------------------

This module contains a rewriting of the `drools` framework, that is intended to be used to provide clear separation and composition for each compiler/runtime of the different engines that Drools project contains (Rules engine, DMN engine, PMML engine).
The first step has been to include `efesto` as self-contained module inside `drools`. When all/most of different use-cases scenarios (rules, decisions, predictions, etc) will be covered, and when a clear compilation/runtime separation will be achieved overall the whole repository, then `efesto` submodules will be moved directly under `drools`.


The main focus of this project is on:

1. clear separation between "container" and "content" contexts; the "content" is represented by functionalities exposed
   by the compile-time and runtime; the "context" is the environment in which such functionalities are invoked (
   see [architectural choices](https://docs.google.com/document/d/1n9rKcMh0qnP7R4DUb3xqanFZcN0q7SL8aBRoAdQDSH0) for more
   details)
2. clear separation between compile-time and runtime
3. uniform overall management of different models/engines

Regarding the overall module organization/relationship, this project should follow the guidelines of the "Clean
Architecture" principle.
As per the design style, the microkernel-style will be featured, where each model/engine will represent a
specific `plugin`.

Each plugin should provide a `compilation` and a `runtime` component.

That [brainstorm](https://miro.com/app/board/uXjVO3fJxsY=/) page will be used as a place to share idea, suggestions,
notes, while the [refactoring proposal](https://miro.com/app/board/uXjVO3eklbE=/) will be used to show technical
diagrams.

The most important decisions (reason and choice) will be
documented [here](https://docs.google.com/document/d/1n9rKcMh0qnP7R4DUb3xqanFZcN0q7SL8aBRoAdQDSH0).

The [documentation](./documentation) folder contains diagrams (PUML, Archimate), images, and other resources related to
design/architectural choices.

This effort can not be full-filled by a single person, since it involve decisions at every level and it will have an
impact on the all `drools` codebase; everyone may be interested will be warmly welcomed for ideas, suggestions,
criticism, contribution.


ArchUnit
========
ArchUnit has been introduced to enforce (as much as possible) some general architectural and design guidelines.

See [official documentation](https://www.archunit.org/userguide/html/000_Index.html) and [maven plugin information](https://github.com/societe-generale/arch-unit-maven-plugin) for further details.

JDepend
=======
JDepend is a tool that traverses Java class file directories and generates design quality metrics for each Java package. JDepend allows you to automatically measure the quality of a design in terms of its extensibility, reusability, and maintainability to manage package dependencies effectively.
See [official documentation](http://clarkware.com/software/JDepend.html">http://clarkware.com/software/JDepend.html)
for details.






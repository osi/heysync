# heysync

heysync is transparent message-passing currency in java using [jetlang](http://jetlang.org)

interfaces are annotated with an annotation, and a proxy can be retrieved that will asynchronously
invoke all registered implementions. classes are generated dynamically so no reflection occurs after
the initial setup.

# Norbit  
Extremely fast event system.

## Changes
A list of changes between Orbit and Norbit

### Drop legacy support
Contrary to Orbit, which intends to be a general purpose event system, Norbit is designed with modern Minecraft & Java 17 in mind. For this reason, we dropped legacy Java 8 support in exchange for newer language features.

### Cache lambda MethodHandle
Replace non-static listener cache with `LambdaListener` `MethodHandle` target cache, which allows it and its associated instances to be garbage collected. This not only fixes a memory leak, it also speeds up non-static listener subscription.

### Optimize invoke dynamic
Rewrite lambdas to use exclusively stateless lambdas.

### Identity map
Replace O(n) `Set` iteration with O(1) `Map` hash function thanks to an `IdentityHashMap`

### Remove recursion
Instead of recursively looping through an object's superclasses to get all it's declared methods, loop once using `klass.getMethods`

### Fail-fast
When an error is thrown in the `LambdaListener` constructor, immediately re-throw the error instead of nulling the `Consumer`, which would result in a `NullPointerException` down the line.

### Human-error prevention
Prevent duplicate listener subscription in most cases. Note: This is not a catch-all, static listeners will still be duplicated in the case where the class is already subscribed, and you subscribe an instance.
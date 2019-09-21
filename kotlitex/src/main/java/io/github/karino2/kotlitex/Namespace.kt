package io.github.karino2.kotlitex

/**
 * A `Namespace` refers to a space of nameable things like macros or lengths,
 * which can be `set` either globally or local to a nested group, using an
 * undo stack similar to how TeX implements this functionality.
 * Performance-wise, `get` and local `set` take constant time, while global
 * `set` takes time proportional to the depth of group nesting.
 */

/**
 * Both arguments are optional.  The first argument is an object of
 * built-in mappings which never change.  The second argument is an object
 * of initial (global-level) mappings, which will constantly change
 * according to any global/top-level `set`s done.
 */
class Namespace(val builtins: Map<String, MacroDefinition> = emptyMap(), globalMacros: MutableMap<String, MacroDefinition> = mutableMapOf()) {
    val undefStack = ArrayList<MutableMap<String, MacroDefinition?>>()
    val current = globalMacros

    fun ArrayList<MutableMap<String, MacroDefinition?>>.pop()  = this.removeAt(this.lastIndex)

    /**
     * Start a new nested group, affecting future local `set`s.
     */
    fun beginGroup() {
        undefStack.add(mutableMapOf());
    }

    /**
     * End current nested group, restoring values before the group began.
     */
    fun endGroup() {
        if (this.undefStack.isEmpty()) {
            throw ParseError("Unbalanced namespace destruction: attempt " +
                    "to pop global namespace; please report this as a bug", null)
        }
        val undefs = this.undefStack.pop()
        undefs.keys.forEach {key ->
            val content = undefs[key]
            if(content == null) {
                current.remove(key)
            }else {
                current[key] = content
            }
        }
    }

    /**
     * Detect whether `name` has a definition.  Equivalent to
     * `get(name) != null`.
     */
    fun has(name: String) = this.current.containsKey(name) ||
                this.builtins.containsKey(name)

    /**
     * Get the current value of a name, or `undefined` if there is no value.
     *
     * Note: Do not use `if (namespace.get(...))` to detect whether a macro
     * is defined, as the definition may be the empty string which evaluates
     * to `false` in JavaScript.  Use `if (namespace.get(...) != null)` or
     * `if (namespace.has(...))`.
     */
    fun get(name: String): MacroDefinition? {
        return if (this.current.containsKey(name)) {
            this.current[name]
        } else {
            this.builtins[name]
        }
    }

    /**
     * Set the current value of a name, and optionally set it globally too.
     * Local set() sets the current value and (when appropriate) adds an undo
     * operation to the undo stack.  Global set() may change the undo
     * operation at every level, so takes time linear in their number.
     */
    fun set(name: String, value: MacroDefinition, global: Boolean = false) {
        if (global) {
            // Global set is equivalent to setting in all groups.  Simulate this
            // by destroying any undos currently scheduled for this name,
            // and adding an undo with the *new* value (in case it later gets
            // locally reset within this environment).
            undefStack.forEach {
                it.remove(name)
            }
            if (this.undefStack.isNotEmpty()) {
                this.undefStack.last()[name] = value
            }
        } else {
            // Undo this set at end of this group (possibly to `undefined`),
            // unless an undo is already in place, in which case that older
            // value is the correct one.
            if(undefStack.isNotEmpty()) {
                val top = undefStack.last()
                if(!top.containsKey(name)) {
                    top[name] = this.current[name]
                }
            }
        }
        this.current[name] = value
    }
}
# Boswell Scripting Environment Documentation
This README file serves as the official documentation of the Boswell scripting environment. 
This document is intended for developers who want to create custom scripts for the environment.

### Prerequisites
Before you can start developing scripts for the Boswell environment, you should have a basic 
understanding of OOP and data structures.

-------------------------------------------------------------------

## Groovy Basics
Boswell uses Groovy as its scripting language. Groovy has similar syntax to Java and Kotlin, making it easier for 
developers to learn and use. Below are some simple examples of the Groovy syntax. For a more detailed summary you
can check out official [Groovy Documentation](https://groovy-lang.org/documentation.html). 

```groovy
// this is a single line comment

/* you can also have comments
* that span multiple lines like this */

// the keyword def can be used to define a variable
def x = 5

// def can be used to define variables of various types
def str = "this is a string"
def numbers = [1, 2, 3, 4, 5] // an array

// the syntax for methods are very similar to java
// the usage of public, private, static, void keywords should be very recognizable!
static void sayHello() {
 def hello = "Hello, Boswell!"
 print(hello)
} 
```

-------------------------------------------------------------------

## Scripting Basics
All Groovy scripts in Boswell are executed on a global scope. This means that unlike older projects like 
Odinms, you don't need to invoke various functions like start(), action(), etc. Instead, all code is 
executed line-by-line in the script.

```groovy
// this example is taken directly from the rithTeleport.groovy npc script
// in this example we see each line includes a sayNext() method call
// each line is asynchronously executed so that we can have multiple dialogue boxes back-to-back
script.sayNext("Alright I'll explain to you more about #bHenesys#k. It's a bowman-town located at the southernmost " +
        "part of the island, made on a flatland in the midst of a deep forest and prairies. The weather's just " +
        "right, and everything is plentiful around that town, perfect for living. Go check it out.")
script.sayNext("Around the prairie you'll find weak monsters such as snails, mushrooms, and pigs. According to " +
        "what I hear, though, in the deepest part of the Pig Park, which is connected to the town somewhere, " +
        "you'll find a humongous, powerful mushroom called Mushmom every now and then.")
```

### Auto-generated Script Templates
Whenever a script is encountered in-game but does not exist a simple script will automatically be generated. 
These scripts have some very basic info within them that is meant to make development a bit easier. The naming 
conventions for scripts are taken directly from the wz files, so they'll always remain consistent!

### Scripting Bindings
Script engine bindings provide a convenient way to access methods within the Boswell source. Most of these methods are found 
within ``ScriptFunc.kt`` and can be accessed using the `script` keyword. However, other bindings do exist such as:

```
user -> this allows access to MapleCharacter
field -> this allows access to MapleMap
```

### NPC Scripting
NPC scripts probably take up the majority of scripts needed to provide smooth gameplay. They also have a set of methods 
that are unique to them and necessary to master in order to write their scripts. These NPC script methods are as follows:


``say(msg: String) : Broadcasts a dialogue box with an ok button. This is commonly used for the last dialogue of 
an NPC. This method should only be used for the script type NPC.``

``say(msg: String, templateId: Int) : Broadcasts a dialogue box with an ok button with an NPC that you can specify. 
 This method should only be used for the non-NPC script types.``

``sayNext(msg: String) : Broadcasts a dialogue box with a next button. This is used when dialogue boxes are needed 
in succession. This method should only be used for the script type NPC.``

``sayNext(msg: String, templateId: Int) : Broadcasts a dialogue box with a next button with an NPC that you can 
specify. This method should only be used for the script type NPC.``

``askYesNo(msg: String) : Broadcasts a dialogue box with a yes and no button. It returns the value of the response. 
1 for yes and 0 for no. This method should only be used for the script type NPC.``

``askYesNo(msg: String, templateId: Int) : Broadcasts a dialogue box with a yes and no button. It returns the value of the response.
1 for yes and 0 for no. This method should only be used for non-NPC script types.``

``askMenu(msg: String) : Broadcasts a dialogue box that can handles lists of options to choose from. It returns the selection number of whatever option has been chosen.``

``askAvatar(msg: String, vararg _: Int) :  ``

``askNumber(msg: String, def: Int, min: Int, max: Int) :  ``

``askText(msg:String) :  ``

``askText(msg: String, msgDefault: String, lengthMin: Int, lengthMax: Int) :  ``

### Examples

```groovy
// say(msg: String) -> should only be used for NPC scripts
script.say("Behold, I am the mighty Glimmer Man! One of the most powerful mages in the world!")

// say(msg: String, templateId: Int) -> should be used for all non-NPC scripts
script.say("Behold, I am the mighty Glimmer Man! One of the most powerful mages in the world!", 9201083)

// sayNext(msg: String) -> should only be used for NPC scripts
script.sayNext("Behold, I am the mighty Glimmer Man! One of the most powerful mages in the world!")

// sayNext(msg: String, templateId: Int) -> should be used for all non-NPC scripts
script.sayNext("Behold, I am the mighty Glimmer Man! One of the most powerful mages in the world!", 9201083)

// askYesNo(msg: String)
def ret = script.askYesNo("Oh, and.. so.. this ship will take you to #bEreve#k, the place where you'll find crimson " +
        "leaves soaking up the sun, the gently breeze that glides past the stream, and the Empress of Maple, Cygnus. " +
        "Would you like to head over to #bEreve#k? \r\n\r\n The trip costs #b1000 Mesos#k")
if (ret == 1) { // yes
    user.gainMeso(-1000)
    user.changeMap(130000210)
} else {
    script.say("If you're not interested, then oh well...")
}

//askYesNo(msg: String, templateId: Int) -> should be used for all non-NPC scripts
def ret = script.askYesNo("Oh, and.. so.. this ship will take you to #bEreve#k, the place where you'll find crimson " +
        "leaves soaking up the sun, the gently breeze that glides past the stream, and the Empress of Maple, Cygnus. " +
        "Would you like to head over to #bEreve#k? \r\n\r\n The trip costs #b1000 Mesos#k", 1100007)
if (ret == 1) { // yes
    user.gainMeso(-1000)
    user.changeMap(130000210)
} else {
    script.say("If you're not interested, then oh well...", 1100007)
}

// askMenu(msg: String) example
def sel = script.askMenu("It's understandable that you may be confused about this place if this is your first " + "time around. " +
        "If you got any questions about this place, fire away.\r\n#L0##bWhat kind of towns are here in Victoria Island?" +
        "#l\r\n#L1#Please take me somewhere else.#k#l")
if (sel == 0) {
    // what kind of towns?
} else if (sel == 1) { 
    //i'd like to go somewhere
}
```

# Conclusion
Thank you for reading the Boswell scripting environment documentation. If you have any questions or comments, 
please feel free to contact either myself or join the Boswell discord!
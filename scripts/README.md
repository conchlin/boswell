# Scripting Documentation
This file will be treated as the official documentation of the Boswell scripting environment. This file also assumes that
 you have a basic understanding of OOP and data structures. 

-------------------------------------------------------------------

## Groovy Basics
Boswell has adopted the Groovy language for scripting purposes. The main reason behind this decision is the syntax 
similarities that it shares with Java and Kotlin. Having this similarity could help to reduce the learning curve when 
utilizing and writing these scripts. 

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
All groovy scripts are executed on a global scope. That means that unlike a lot of older sources (Odinms) you do not 
need to invoke various functions like start(), action(), etc. Instead, we simply execute all of our code line-by-line 
right here in the script.

```groovy
// in this example we see each line includes either a sayNext() or say() method call
// each line is asynchronously executed so that we can have multiple dialogue boxes back-to-back
npc.sayNext("The first dialogue box")
npc.sayNext("the second dialogue box")
npc.say("the final dialogue box")
```

### Script Types and Bindings
Boswell has 6 script types ``field, item, npc, portal, quest, reactor`` that are utilized in this version of maplestory. 
Each script type is treated slightly differently and is exposed to different methods based on needs. This is where 
ScriptEngine global bindings come into play. Each script binding pairs a keyword to a file location within the source. 
This provides a streamlined way of accessing methods for scripting purposes and helps provide readability and code 
organization. However, depending on script type not all bindings are added to that specific engine instance (ex. you wouldn't 
need a portal binding if you are running a npc script). The list of bindings are as follows:

```
user -> MapleCharacter.java
field -> MapleMap.java
item -> ScriptItem.kt
npc -> ScriptNpc.kt
portal -> ScriptPortal.kt
quest -> ScriptQuest.kt
reactor -> ScriptReactor.kt
```
Please note: With the exception of `user` and `field`, all script binding related files can be found in `src/main/kotlin/script/bindings/.`.

So if we look at the previous example of code we can now see where exactly these keywords and methods calls are coming from.
```groovy
// by using the npc keyword we can access methods within the ScriptNpc.kt file 
// ScriptNpc.sayNext() and ScriptNpc.say()
npc.sayNext("The first dialogue box")
npc.sayNext("the second dialogue box")
npc.say("the final dialogue box")
// let's say that after talking to the NPC you want to warp the player to a different map
// this can be accomplished by using a different binding and method call (accessing that player's specific instance of MapleCharacter.changeMap())
user.changeMap(100000000)
```

### NPC Scripting
NPC scripts probably take up the majority of scripts needed to provide smooth gameplay. They also have a set of methods 
that are unique to them and necessary to master in order to write their scripts. These NPC script methods are as follows:


``say(msg: String)`` : ``Broadcasts a dialogue box with an ok button. This is commonly used for the last dialogue of an NPC.``

``sayNext(msg: String)`` : ``Broadcasts a dialogue box with a next button. This is used when dialogue boxes are needed in succession.``

``askYesNo(msg: String)`` : ``Broadcasts a dialogue box with a yes and no button. It returns the value of the response. 1 for yes and 0 for no.``
```groovy
// example of using askYesNo()
// since we are evaluating the return value of askYesNo we need to store it in a variable
def ret = npc.askYesNo("Would you like to continue?")

// we then give different responses based on the answer
if (ret == 1) { // yes
    npc.say("Great! Let's continue!")
} else { // no
 // ret == 0
    npc.say("You must have something else to do :(")
}
```

``askMenu(msg: String)`` : ``Broadcasts a dialogue box that can handles lists of options to choose from. It returns the selection number of whatever option has been chosen.``
```groovy
// example of using askMenu()
// we are basing our response off the return value of askMenu()
// each selection should be surrounded by #L<selection number>#<text>#l 
def sel = npc.askMenu("What option do you want to choose? #L0#1st option is best!#l #L1#2nd option is my choice!#l")

// we then handle each possible selection value
if (sel == 0) { 
    npc.say("I agree, the 1st option is the best!")
} else if (sel == 1) {
    npc.say("Yup, you're right. The second option is a great choice.")
}
```

todo: The rest of the npc scripting methods below are implemented but not used. As they are utilized documentation should be added
for them.

``askAvatar(msg: String, vararg _: Int)`` : `` ``

``askNumber(msg: String, def: Int, min: Int, max: Int)`` : `` ``

``askText(msg:String)`` : `` ``

``askText(msg: String, msgDefault: String, lengthMin: Int, lengthMax: Int)`` : `` ``

todo: Sometimes other script types will need access to these npc dialogues. A simple way needs to be added.


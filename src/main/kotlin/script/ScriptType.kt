package script

enum class ScriptType(val type: String) {
    None(""),
    Npc("npc"),
    UserEnterField("field/onUserEnter"),
    FirstEnterField("field/onFirstUserEnter"),
    Portal("portal"),
    Reactor("reactor"),
    Item("item"),
    Quest("quest");
}
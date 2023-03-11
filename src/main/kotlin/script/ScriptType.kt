package script

enum class ScriptType(type: String) {
    None(""),
    Npc("npc"),
    Field("field"),
    FirstEnterField("field"),
    Portal("portal"),
    Reactor("reactor"),
    Item("item"),
    Quest("quest");
}
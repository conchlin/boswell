package script

enum class ScriptMessageType (val type: Byte) {
   Say(0),
   AskYesNo(1),
   AskText(2),
   AskNumber(3),
   AskMenu(4),
   AskQuestion(5),
   AskQuiz(6),
   AskAvatar(7),
   AskPet(9),
   AskAccept(12),
   AskAcceptNoEsc(13),
   AskBoxText(14)
}
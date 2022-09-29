package script

object ScriptMessageType {
   const val Say: Byte = 0
   const val AskYesNo: Byte = 1
   const val AskText: Byte = 2
   const val AskNumber: Byte = 3
   const val AskMenu: Byte = 4
   const val AskQuestion: Byte = 5
   const val AskQuiz: Byte = 6
   const val AskAvatar: Byte = 7
   const val AskPet: Byte = 9
   const val AskAccept: Byte = 12
   const val AskAcceptNoEsc: Byte = 13
   const val AskBoxText: Byte = 14
}
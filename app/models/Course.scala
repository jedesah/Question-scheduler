package models

class Course(val title: String) {
  var questionIdIndex = 0
  var questions: List[Question] = Nil

  def addQuestion(workstationId: String) = {
    questions = Question(questionIdIndex.toString, workstationId) :: questions
    questionIdIndex = questionIdIndex + 1
  }
  
  def removeQuestion(questionId: String) = questions = questions.filter(_.id != questionId)
}
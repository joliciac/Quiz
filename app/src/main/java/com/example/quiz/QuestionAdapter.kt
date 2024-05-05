import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.Question
import com.example.quiz.databinding.ItemQuestionBinding

class QuestionsAdapter(private var questions: MutableList<Question>) :
    RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {

    class QuestionViewHolder(private val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question) {
            binding.txtQuestion.text = question.text
            // Reset the RadioGroup to remove old RadioButtons if this view was recycled
            binding.optionsRadioGroup.removeAllViews()

            // Dynamically create RadioButtons for each option
            question.options.forEach { option ->
                val radioButton = RadioButton(binding.root.context).apply {
                    text = option
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    )
                    isChecked = option == question.correctOption
                }
                binding.optionsRadioGroup.addView(radioButton)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount() = questions.size

    fun addQuestion(question: Question) {
        questions.add(question)
        notifyItemInserted(questions.size - 1)
    }

    fun updateQuestions(newQuestions: List<Question>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = questions.size
            override fun getNewListSize(): Int = newQuestions.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return questions[oldItemPosition].id == newQuestions[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return questions[oldItemPosition] == newQuestions[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        questions = newQuestions.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }
}

package at.bitfire.notesx5.ui

import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.R
import at.bitfire.notesx5.convertLongToDateString
import at.bitfire.notesx5.convertLongToTimeString
import at.bitfire.notesx5.database.VCategory
import at.bitfire.notesx5.database.VComment
import at.bitfire.notesx5.database.VJournalDatabase
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.databinding.FragmentVjournalItemBinding
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_vjournal_item_comment.view.*
import java.util.*


class VJournalItemFragment : Fragment() {

    lateinit var binding: FragmentVjournalItemBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory: VJournalItemViewModelFactory
    lateinit var vJournalItemViewModel: VJournalItemViewModel
    lateinit var inflater: LayoutInflater

    var displayedCategoryChips = mutableListOf<VCategory>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentVjournalItemBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        val arguments = VJournalItemFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)




        // set up view model
        viewModelFactory = VJournalItemViewModelFactory(arguments.item2show, dataSource, application)
        vJournalItemViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemViewModel::class.java)

        binding.model = vJournalItemViewModel
        binding.lifecycleOwner = this




        // set up observers
        vJournalItemViewModel.editingClicked.observe(viewLifecycleOwner, Observer {
            if (it) {
                vJournalItemViewModel.editingClicked.value = false
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVJournalItemEditFragment().setItem2edit(vJournalItemViewModel.vJournal.value!!.vJournal.id))
            }
        })

        vJournalItemViewModel.vJournal.observe(viewLifecycleOwner, {

            if (it?.vJournal != null) {

                val statusArray = resources.getStringArray(R.array.vjournal_status)
                binding.statusChip.text = statusArray[vJournalItemViewModel.vJournal.value!!.vJournal.status]

                val classificationArray = resources.getStringArray(R.array.vjournal_classification)
                binding.classificationChip.text = classificationArray[vJournalItemViewModel.vJournal.value!!.vJournal.classification]


                binding.commentsLinearlayout.removeAllViews()
                vJournalItemViewModel.vJournal.value!!.vComment?.forEach { comment ->
                    val commentView = inflater.inflate(R.layout.fragment_vjournal_item_comment, container, false);
                    commentView.comment_textview.text = comment.text
                    binding.commentsLinearlayout.addView(commentView)

                    // set on Click Listener to open a dialog to update the comment
                    commentView.setOnClickListener{

                        // set up the values for the TextInputEditText
                        val updatedText: TextInputEditText = TextInputEditText(context!!)
                        updatedText.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        updatedText.setText(comment.text)
                        updatedText.isSingleLine = false;
                        updatedText.maxLines = 8

                        // set up the builder for the AlertDialog
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle("Edit comment")
                        builder.setIcon(R.drawable.ic_comment_add)
                        builder.setView(updatedText)


                        builder.setPositiveButton("Save") { _, _ ->
                            // update the comment
                            val updatedComment = comment.copy()
                            updatedComment.text = updatedText.text.toString()
                            vJournalItemViewModel.upsertComment(updatedComment)
                        }
                        builder.setNegativeButton("Cancel") { _, _ ->
                            // Do nothing, just close the message
                        }

                        builder.show()
                    }

                }


/*
                val commentAdapterEntries: ArrayList<String> = arrayListOf()
                vJournalItemViewModel.vJournal.value!!.vComment?.forEach { comment ->
                    commentAdapterEntries.add(comment.text)
                }

                val commentsArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                        context!!,  // Die aktuelle Umgebung (diese Activity)
                        R.layout.fragment_vjournal_item_comment,  // Die ID des Zeilenlayouts (der XML-Layout Datei)
                        R.id.comment_textview,  // Die ID eines TextView-Elements im Zeilenlayout
                        commentAdapterEntries) // Beispieldaten in einer ArrayList

                binding.commentsListview.adapter = commentsArrayAdapter

 */
/*

                vJournalItemViewModel.vJournal.value!!.vComment?.forEach { comment ->
                    val commentTextView = TextView(context)
                    commentTextView.text = comment.text
                    binding.commentsLinearlayout.addView(commentTextView)
                }


 */

            }
        })

        vJournalItemViewModel.vCategory.observe(viewLifecycleOwner, {
            if (it != null)
                addChips(vJournalItemViewModel.vJournal.value!!.vCategory!!)
        })


        binding.addComment.setOnClickListener{

            val newComment: TextInputEditText = TextInputEditText(context!!)
            newComment.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            newComment.isSingleLine = false;
            newComment.maxLines = 8

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Add comment")
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(newComment)

            builder.setPositiveButton("Save") { _, _ ->
               vJournalItemViewModel.upsertComment(VComment(journalLinkId=vJournalItemViewModel.vJournal.value!!.vJournal.id, text = newComment.text.toString()))
            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.show()
        }


        return binding.root
    }

    // adds Chips to the categoriesChipgroup based on the categories List
    private fun addChips(categories: List<VCategory>) {

        categories.forEach() { category ->

            if (category.text.isBlank())     // don't add empty categories
                return@forEach

            if(displayedCategoryChips.indexOf(category) != -1)    // only show categories that are not there yet
                return@forEach

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_item_categories_chip, binding.categoriesChipgroup, false) as Chip
            categoryChip.text = category.text
            binding.categoriesChipgroup.addView(categoryChip)
            displayedCategoryChips.add(category)

            categoryChip.setOnClickListener {

                val selectedCategoryArray = arrayOf(category.text)     // convert to array
                // Responds to chip click
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVjournalListFragmentList().setCategory2filter(selectedCategoryArray)
                )
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_item, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.vjournal_item_share) {

            var shareText: String = "${convertLongToDateString(vJournalItemViewModel.vJournal.value!!.vJournal.dtstart)} ${convertLongToTimeString(vJournalItemViewModel.vJournal.value!!.vJournal.dtstart)}\n"
            shareText += "${vJournalItemViewModel.vJournal.value!!.vJournal.summary}\n\n"
            shareText += "${vJournalItemViewModel.vJournal.value!!.vJournal.description}\n\n"
            //todo add category again
            //shareText += "Categories/Labels: ${vJournalItemViewModel.vJournal.value!!.vCategory}"

            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, vJournalItemViewModel.vJournal.value!!.vJournal.summary)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent(shareIntent))


        }
        return super.onOptionsItemSelected(item)
    }

}


package dev.nario.syno.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.core.graphics.drawable.toDrawable
import dev.nario.syno.R
import dev.nario.syno.activities.HomeActivity
import java.util.Calendar

class ScheduleMatchDialog(
    private val activity: HomeActivity
) {
    fun show() {

        val dialog = Dialog(activity)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_schedule_match)

        dialog.window?.setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.90).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // dialog items
        val etMatchName = dialog.findViewById<EditText>(
            R.id.etMatchName
        )
        val etGameCategory = dialog.findViewById<EditText>(
            R.id.etGameCategory
        )
        val etDate = dialog.findViewById<EditText>(
            R.id.etDate
        )
        val etTime = dialog.findViewById<EditText>(
            R.id.etTime
        )
        val etPeopleLimit = dialog.findViewById<EditText>(
            R.id.etPeopleLimit
        )
        val etMeetingLocation = dialog.findViewById<EditText>(
            R.id.etMeetingLocation
        )
        val etAverageAge = dialog.findViewById<EditText>(
            R.id.etAverageAge
        )
        val spinnerGameType = dialog.findViewById<Spinner>(
            R.id.spinnerGameType
        )
        val btnConfirm = dialog.findViewById<Button>(
            R.id.btnConfirmSchedule
        )
        val btnCancel = dialog.findViewById<Button>(
            R.id.btnCancelSchedule
        )

        // cancel dialog
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // send dialog
        btnConfirm.setOnClickListener {

            val matchName = etMatchName.text.toString().trim()
            val gameCategory = etGameCategory.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()
            val peopleLimit = etPeopleLimit.text.toString().trim()
            val meetingLocation = etMeetingLocation.text.toString().trim()
            val averageAge = etAverageAge.text.toString().trim()

            val gameType = spinnerGameType.selectedItem.toString()

            // validating fields
            if (matchName.isEmpty()) {
                etMatchName.error = "Informe o nome da partida"
                return@setOnClickListener
            }

            if (gameCategory.isEmpty()) {
                etGameCategory.error = "Informe o jogo"
                return@setOnClickListener
            }

            if (date.isEmpty()) {
                etDate.error = "Selecione o dia"
                return@setOnClickListener
            }

            if (time.isEmpty()) {
                etTime.error = "Selecione o horário"
                return@setOnClickListener
            }

            if (peopleLimit.isEmpty()) {
                etPeopleLimit.error = "Informe o limite de pessoas"
                return@setOnClickListener
            }

            if (meetingLocation.isEmpty()) {
                etMeetingLocation.error = "Informe o local ou link"
                return@setOnClickListener
            }

            // TODO: save match on firebase

            dialog.dismiss()
        }

        // date selector
        etDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                activity,
                { _, selectedYear, selectedMonth, selectedDay ->

                    val formattedDate = String.format(
                        "%02d/%02d/%04d",
                        selectedDay,
                        selectedMonth + 1,
                        selectedYear
                    )

                    etDate.setText(formattedDate)
                },
                year,
                month,
                day
            )

            // user can select just dates after today
            datePickerDialog.datePicker.minDate =
                calendar.timeInMillis

            datePickerDialog.show()
        }

        // time selector
        etTime.setOnClickListener {

            val calendar = Calendar.getInstance()

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                activity,
                { _, selectedHour, selectedMinute ->

                    val formattedTime = String.format(
                        "%02d:%02d",
                        selectedHour,
                        selectedMinute
                    )

                    etTime.setText(formattedTime)
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }

        // spinner to select the game type
        val gameTypes = arrayOf(
            "Casual",
            "Jogando sério",
            "Ranked"
        )

        val adapter = ArrayAdapter(
            activity,
            R.layout.spinner_item,
            gameTypes
        )

        spinnerGameType.adapter = adapter


        dialog.show()

        dialog.window?.setLayout(
            (activity.resources.displayMetrics.widthPixels * 0.90).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
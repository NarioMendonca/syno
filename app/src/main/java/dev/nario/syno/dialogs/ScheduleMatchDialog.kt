package dev.nario.syno.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.core.graphics.drawable.toDrawable
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import dev.nario.syno.R
import dev.nario.syno.activities.HomeActivity
import dev.nario.syno.entities.Match
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class ScheduleMatchDialog(
    private val activity: HomeActivity
) {
    var match: Match? = null
    fun show() {
        val db = Firebase.firestore
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
        val btnCloseDetails = dialog.findViewById<ImageButton>(R.id.btnCloseDetails)

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

        btnCloseDetails.setOnClickListener {
            dialog.dismiss()
        }

        // send dialog
        btnConfirm.setOnClickListener {

            val matchName = etMatchName.text.toString().trim()
            val gameCategory = etGameCategory.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()
            val peopleLimit = etPeopleLimit.text.toString().trim().toInt()
            val meetingLocation = etMeetingLocation.text.toString().trim()
            val averageAge = etAverageAge.text.toString().trim().toIntOrNull()

            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val localDateTime = LocalDateTime.parse("$date $time", dateFormatter)
            val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
            val fullDate = Timestamp(Date.from(instant))

            val gameType = spinnerGameType.selectedItem.toString()

            // validating fields
            if (matchName.isEmpty()) {
                etMatchName.error = "Informe o nome dessa partida!"
                return@setOnClickListener
            }

            if (gameCategory.isEmpty()) {
                etGameCategory.error = "Informe o nome do jogo!"
                return@setOnClickListener
            }

            if (date.isEmpty()) {
                etDate.error = "Dia da partida não foi selecionado"
                return@setOnClickListener
            }

            if (time.isEmpty()) {
                etTime.error = "Horário da partida não foi selecionado"
                return@setOnClickListener
            }

            if (peopleLimit < 2) {
                if (peopleLimit.toInt() < 2) {
                    etPeopleLimit.error = "O limite mínimo é dois!"
                    return@setOnClickListener
                }
                etPeopleLimit.error = "Informe o limite de pessoas!"
                return@setOnClickListener
            }

            if (meetingLocation.isEmpty()) {
                etMeetingLocation.error = "Informe o local ou link!"
                return@setOnClickListener
            }

            val matchToCreate = Match(
                null,
                activity.currentUserId,
                matchName,
                gameCategory,
                fullDate,
                peopleLimit,
                meetingLocation,
                averageAge,
                gameType,
                participants = emptyList(),
            )
            match = matchToCreate

            db.collection("matches")
                .add(matchToCreate)
                .addOnSuccessListener { createdMatchDocument ->
                    createdMatchDocument.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val match = document.toObject(Match::class.java)
                            Log.w(activity.TAG, "match converted! ${match.toString()}")
                            activity.addMatchToList(match as Match)
                        }
                    }
                    Log.w(activity.TAG, "match created! id: ${createdMatchDocument.id}, ${createdMatchDocument}")
                }
                .addOnFailureListener { e ->
                    Log.e(activity.TAG, "Error on create match: ", e)
                }

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
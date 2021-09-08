package com.example.accessphonebook

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.accessphonebook.databinding.ActivityMainBinding
import java.lang.Boolean
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions

class MainActivity : AppCompatActivity(), NumberClickListener {
    private lateinit var binding: ActivityMainBinding
    var listView: androidx.recyclerview.widget.RecyclerView? = null
    var dataAdapter: NumberListRecyclerViewAdapter? = null
    private lateinit var receiverName: String
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var phoneNumber = ""
        binding.next.setOnClickListener {
            if (binding.receiverName.text.isNotEmpty()) {
                val phone = binding.receiverPhone.text.toString()
                if (phone.startsWith("0")) {
                    if (phone.length == 10) {
                        this.toast("Please provide a correct number")
                    } else {
                        phoneNumber = "+880" + phone.substring(1)
                        this.toast(phoneNumber)
                    }
                } else if (phone.startsWith("1")) {

                    if (phone.length == 10) {
                        phoneNumber = "+880$phone"
                        this.toast(phoneNumber)
                    } else {
                        this.toast("Please provide a correct number")
                    }

                } else {
                    this.toast("Please provide a correct number")
                }
            } else {
                this.toast("Please enter receiver name")
            }
        }

        binding.contacts.setOnClickListener {
            requestContactPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val contactData: Uri? = data?.data
            val c: Cursor = this.contentResolver.query(contactData!!, null, null, null, null)!!
            if (c.moveToFirst()) {
                var phoneNumber = ""
                var emailAddress = ""
                val phonesList = ArrayList<String>()
                val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
                var hasPhone =
                    c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                hasPhone = if (hasPhone.equals("1", ignoreCase = true)) "true" else "false"
                if (Boolean.parseBoolean(hasPhone)) {
                    val phones: Cursor = this.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null
                    )!!
                    while (phones.moveToNext()) {
                        phoneNumber =
                            phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                        phonesList.add(phoneNumber)
                    }
                    phones.close()
                }

                // Find Email Addresses
                val emails: Cursor = this.contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                    null,
                    null
                )!!
                while (emails.moveToNext()) {
                    emailAddress =
                        emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                }
                emails.close()

                if (phonesList.size > 1) {
                    showContactDialog(name, phonesList)
                } else {
                    binding.receiverName.setText(name)
                    binding.receiverPhone.setText(
                        when {
                            phoneNumber.startsWith("0") -> {
                                phoneNumber.substring(1).replace("-", "")
                            }
                            phoneNumber.startsWith("+") -> {
                                phoneNumber.substring(4).replace("-", "")
                            }
                            else -> {
                                phoneNumber
                            }
                        }
                    )
                }
            }
            c.close()
        }
    }

    private fun showContactDialog(name: String, phoneList: ArrayList<String>) {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.contact_list_dialog)

        val nameTv = dialog.findViewById(R.id.name) as TextView
        listView = dialog.findViewById(R.id.lstContacts)

        receiverName = name

        nameTv.text = name
        dataAdapter = NumberListRecyclerViewAdapter(phoneList, this)

        listView.apply {
            this!!.setHasFixedSize(true)
            DefaultItemAnimator()
            adapter = dataAdapter
            layoutManager = GridLayoutManager(
                this@MainActivity,
                1
            )
            addItemDecoration(DividerDecorator(this@MainActivity))
        }

        //getContacts()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.attributes?.gravity = Gravity.CENTER
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun getContacts() {

        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, 1)

        /*val contentResolver: ContentResolver = requireActivity().contentResolver
            var contactId: String? = null
            var displayName: String? = null
            contactsInfoList = ArrayList<ContactsInfo>()
            val cursor: Cursor = requireActivity().contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )!!
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val hasPhoneNumber =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt()
                    if (hasPhoneNumber > 0) {
                        val contactsInfo = ContactsInfo()
                        contactId =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        displayName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        contactsInfo.contactId = contactId
                        contactsInfo.displayName = displayName
                        val phoneCursor: Cursor = requireActivity().contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )!!
                        if (phoneCursor.moveToNext()) {
                            val phoneNumber =
                                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            contactsInfo.phoneNumber = phoneNumber
                        }
                        phoneCursor.close()
                        (contactsInfoList as ArrayList<ContactsInfo>).add(contactsInfo)
                    }
                }
            }
            cursor.close()
            dataAdapter = MyCustomAdapter(requireActivity(), R.layout.contact_info, contactsInfoList)
            listView?.adapter = dataAdapter*/
    }

    private fun requestContactPermission() {

        Permissions.check(
            this,
            Manifest.permission.READ_CONTACTS,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    getContacts()
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String>?
                ) {
                    super.onDenied(context, deniedPermissions)
                }

                override fun onBlocked(
                    context: Context?,
                    blockedList: java.util.ArrayList<String>?
                ): kotlin.Boolean {
                    return super.onBlocked(context, blockedList)
                }
            })
    }

    override fun onItemClick(number: String) {
        binding.receiverName.setText(receiverName)
        binding.receiverPhone.setText(
            when {
                number.startsWith("0") -> {
                    number.substring(1).replace("-", "")
                }
                number.startsWith("+") -> {
                    if (number.contains(" ")) {
                        number.substring(5).replace("-", "")
                    } else {
                        number.substring(4).replace("-", "")
                    }

                }
                else -> {
                    number
                }
            }
        )
        dialog.dismiss()
    }
}
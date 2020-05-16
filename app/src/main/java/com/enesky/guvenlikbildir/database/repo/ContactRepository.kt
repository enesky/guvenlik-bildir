package com.enesky.guvenlikbildir.database.repo

import com.enesky.guvenlikbildir.database.dao.ContactDao
import com.enesky.guvenlikbildir.database.entity.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.*
import kotlin.Comparator

/**
 * Created by Enes Kamil YILMAZ on 16.05.2020
 */

class ContactRepository(private val contactDao: ContactDao) {

    fun refreshContacts(contacts: List<Contact>) {
        GlobalScope.launch(Dispatchers.Default) {
            val turkishLocale = Locale("tr", "TR")
            contacts.toMutableList().sortWith(Comparator { o1, o2 ->
                Collator.getInstance(turkishLocale).compare(o1.name, o2.name)
            })

            val contactList = contactDao.getAllContacts()

            if (contactList.isNullOrEmpty()) {
                contactDao.insertAll(contacts)
            } else {
                for (contact in contacts)
                    if (!contactList.contains(contact))
                        contactDao.insert(contact)

                for (contact in contactList)
                    if (!contacts.contains(contact))
                        contactDao.delete(contact)
            }

        }
    }

    fun unselectContact(contact: Contact) {
        GlobalScope.launch(Dispatchers.Default) {
            contact.isSelected = false
            contactDao.update(contact)
        }
    }

    fun selectContacts(contacts: List<Contact>) {
        GlobalScope.launch(Dispatchers.Default) {
            for (contact in contacts) {
                contact.isSelected = true
                contactDao.update(contact)
            }
        }
    }

}
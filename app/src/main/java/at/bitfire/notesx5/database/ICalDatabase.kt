/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.bitfire.notesx5.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.database.views.ICal4List


/**
 * A database that stores vJournal information.
 * And a global method to get access to the database.
 */

@Database(entities = [
    Attendee::class,
    Category::class,
    Comment::class,
    ICalCollection::class,
    Contact::class,
    ICalObject::class,
    Organizer::class,
    Relatedto::class,
    Resource::class],
        views = [ICal4List::class],
        version = 66,
        exportSchema = false)
//@TypeConverters(Converters::class)
abstract class ICalDatabase : RoomDatabase() {

    /**
     * Connects the database to the DAO.
     */
    abstract val iCalDatabaseDao: ICalDatabaseDao

    /**
     * Define a companion object, this allows us to add functions on the SleepDatabase class.
     */
    companion object {
        /**
         * INSTANCE will keep a reference to any database returned via getInstance.
         *
         * This will help us avoid repeatedly initializing the database, which is expensive.
         *
         *  The value of a volatile variable will never be cached, and all writes and
         *  reads will be done to and from the main memory. It means that changes made by one
         *  thread to shared data are visible to other threads.
         */
        @Volatile
        private var INSTANCE: ICalDatabase? = null

        /**
         * Helper function to get the database.
         *
         * If a database has already been retrieved, the previous database will be returned.
         * Otherwise, create a new database.
         *
         * This function is threadsafe, and callers should cache the result for multiple database
         * calls to avoid overhead.
         *
         * This is an example of a simple Singleton pattern that takes another Singleton as an
         * argument in Kotlin.
         *
         * To learn more about Singleton read the wikipedia article:
         * https://en.wikipedia.org/wiki/Singleton_pattern
         *
         * @param context The application context Singleton, used to get access to the filesystem.
         */
        fun getInstance(context: Context): ICalDatabase {
            // Multiple threads can ask for the database at the same time, ensure we only initialize
            // it once by using synchronized. Only one thread may enter a synchronized block at a
            // time.
            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                var instance = INSTANCE
                // If instance is `null` make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            ICalDatabase::class.java,
                            "notesx5_database"
                    )
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this lesson. You can learn more about
                            // migration with Room in this blog post:
                            // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                            .fallbackToDestructiveMigration()
                            .build()
                    // Assign INSTANCE to the newly created database.
                    INSTANCE = instance
                }
                // Return instance; smart cast to be non-null.
                return instance
            }
        }


        /**
         * Switches the internal implementation with an empty in-memory database.
         *
         * @param context The context.
         */
        @VisibleForTesting
        fun switchToInMemory(context: Context) {
            INSTANCE = Room.inMemoryDatabaseBuilder(context.applicationContext,
                    ICalDatabase::class.java).build()
        }

        /**
         * returns an empty in-memory database.
         *
         * @param context The context.
         * @return ICalDatabase as an in-memory database
         */
        @VisibleForTesting
        fun getInMemoryDB(context: Context): ICalDatabase {

                return Room.inMemoryDatabaseBuilder(context, ICalDatabase::class.java)
                        .allowMainThreadQueries()
                        .build()
        }

        /**
         * Inserts the dummy data into the database if it is currently empty.
         */
        suspend fun populateInitialTestData(database: ICalDatabaseDao) {


/*
        val lipsumSummary = "Lorem ipsum dolor sit amet"
        val lipsumDescription = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
 */
            val rfcSummary = "Staff meeting minutes"
            val rfcDesc = "1. Staff meeting: Participants include Joe, Lisa, and Bob. Aurora project plans were reviewed. There is currently no budget reserves for this project. Lisa will escalate to management. Next meeting on Tuesday.\n\n" +
                    "2. Telephone Conference: ABC Corp. sales representative called to discuss new printer. Promised to get us a demo by Friday.\n\n" +
                    "3. Henry Miller (Handsoff Insurance): Car was totaled by tree. Is looking into a loaner car. 555-2323 (tel)."

            /*
            val jSummary = "Project JF"
            val jDesc = "1. Steering PPT presented and discussed\n\n" +
                    "2. Testphase will be initiated on Thursday\n\n" +
                    "3. Management presentation in progress"


             */
            val noteSummary = "Notes for the next JF"
            val noteDesc = "Get a proper pen\nOffer free coffee for everyone"
/*
        val noteSummary2 = "Shopping list"
        val noteDesc2 = "Present for Tom\nProper Pen\nCoffee"


 */

            //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
            //database.insert(vJournalItem(0L, lipsumSummary, lipsumDescription, System.currentTimeMillis(), "Organizer",  "#category1, #category2", "FINAL","PUBLIC", "", "uid", System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), 0))
            //database.insert(vJournalItem(summary=lipsumSummary, description=lipsumDescription, organizer="Organizer", categories="JourFixe, BestProject"))

            //onConflict strategy = IGNORE!
            database.upsertCollection(ICalCollection(collectionId = 1L, url = "https://localhost", displayName = "Local Collection"))

            val newEntry = database.insertICalObject(ICalObject(collectionId = 1L, component = Component.JOURNAL.name, summary = rfcSummary, description = rfcDesc, dtstart = System.currentTimeMillis()))
            database.insertAttendee(Attendee(caladdress = "test@test.de", icalObjectId = newEntry))
            database.insertCategory(Category(text = "cat", icalObjectId = newEntry))
            database.insertCategory(Category(text = "cat", icalObjectId = newEntry))

            database.insertComment(Comment(text = "comment", icalObjectId = newEntry))
            database.insertOrganizer(Organizer(caladdress = "organizer", icalObjectId = newEntry))
            //database.insertRelatedto(Relatedto(text = "related to", icalObjectId = newEntry))


            //database.insert(vJournalItem(component="JOURNAL", summary=jSummary, description=jDesc, organizer="LOCAL", categories="Appointment, Education"))

            //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary, description=noteDesc, organizer="LOCAL", categories="JourFixe, BestProject"))
            //database.insert(vJournalItem(component="NOTE", dtstart=0L, summary=noteSummary2, description=noteDesc2, organizer="LOCAL", categories="Shopping"))

            val newEntry2 = database.insertICalObject(ICalObject(collectionId = 1L, component = Component.NOTE.name, summary = noteSummary, description = noteDesc))
            database.insertAttendee(Attendee(caladdress = "test@test.de", icalObjectId = newEntry2))
            database.insertCategory(Category(text = "cat", icalObjectId = newEntry2))
            database.insertCategory(Category(text = "cat", icalObjectId = newEntry2))

            database.insertComment(Comment(text = "comment", icalObjectId = newEntry2))
            database.insertOrganizer(Organizer(caladdress = "organizer", icalObjectId = newEntry2))
            // database.insertRelatedto(Relatedto(text = "related to", icalObjectId = newEntry2))

        }


    }
}

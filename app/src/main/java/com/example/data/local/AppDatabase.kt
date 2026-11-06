package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.BusinessLead
import com.example.data.model.EmailDraft
import com.example.data.model.OutreachAlert
import com.example.data.model.OutreachTemplate
import com.example.data.model.TemplateVariationExperiment

@Database(
    entities = [
        BusinessLead::class,
        EmailDraft::class,
        OutreachAlert::class,
        OutreachTemplate::class,
        TemplateVariationExperiment::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun draftDao(): DraftDao
    abstract fun alertDao(): AlertDao
    abstract fun templateDao(): TemplateDao
    abstract fun experimentDao(): ExperimentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "outreach_agent_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

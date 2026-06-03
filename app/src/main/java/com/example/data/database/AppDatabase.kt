package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FamilyCareDao
import com.example.data.dao.MedicalFacilityDao
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        EmergencyLog::class,
        VoiceMessage::class,
        Medication::class,
        MedicationLog::class,
        FamilyGroup::class,
        FamilyMember::class,
        JoinRequest::class,
        TrustedDevice::class,
        WatchHealthData::class,
        HospitalAppointment::class,
        MedicalDocument::class,
        EmergencyProfile::class,
        MedicalFacilityEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun familyCareDao(): FamilyCareDao
    abstract fun medicalFacilityDao(): MedicalFacilityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "family_care_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

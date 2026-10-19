package com.example.peairbrakes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PeAirbrakes.db"
        private const val DATABASE_VERSION = 5

        // User table
        private const val TABLE_USERS = "users"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_ROLE = "role"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_COMPANY_NAME = "company_name"
        private const val COLUMN_COMPANY_ADDRESS = "company_address"
        private const val COLUMN_SPECIALTY = "specialty"

        // Diesel Slips table
        private const val TABLE_DIESEL_SLIPS = "diesel_slips"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_EMAIL = "user_email"
        private const val COLUMN_PHOTO_PATH = "photo_path"
        private const val COLUMN_TIMESTAMP = "timestamp"

        // Assistance Requests table
        private const val TABLE_REQUESTS = "assistance_requests"
        private const val COLUMN_REQ_ID = "req_id"
        private const val COLUMN_TRUCK_REG = "truck_registration"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_ASSIGNED_TO = "assigned_to"

        // Job Cards table
        private const val TABLE_JOB_CARDS = "job_cards"
        private const val COLUMN_JOB_ID = "job_id"
        private const val COLUMN_REF_REQ_ID = "req_id_ref"
        private const val COLUMN_TRUCK_MAKE = "truck_make"
        private const val COLUMN_KILOMETERS = "kilometers"
        private const val COLUMN_PARTS_USED = "parts_used"
        private const val COLUMN_REFUSED_REPAIRS = "refused_repairs"
        private const val COLUMN_REFUSED_PHOTO = "refused_photo"
        private const val COLUMN_SIGNATURE_PATH = "signature_path"
        private const val COLUMN_START_TIME = "start_time"
        private const val COLUMN_END_TIME = "end_time"
        private const val COLUMN_TRAVEL_KM = "travel_km"
        private const val COLUMN_STATUS = "status" // "Not Started", "Traveling", "On Site", "Completed"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USERS ($COLUMN_EMAIL TEXT PRIMARY KEY, $COLUMN_PASSWORD TEXT, $COLUMN_NAME TEXT, $COLUMN_ROLE TEXT, $COLUMN_PHONE TEXT, $COLUMN_COMPANY_NAME TEXT, $COLUMN_COMPANY_ADDRESS TEXT, $COLUMN_SPECIALTY TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_DIESEL_SLIPS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USER_EMAIL TEXT, $COLUMN_PHOTO_PATH TEXT, $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)")
        db?.execSQL("CREATE TABLE $TABLE_REQUESTS ($COLUMN_REQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USER_EMAIL TEXT, $COLUMN_TRUCK_REG TEXT, $COLUMN_DESCRIPTION TEXT, $COLUMN_PHOTO_PATH TEXT, $COLUMN_LATITUDE REAL, $COLUMN_LONGITUDE REAL, $COLUMN_ASSIGNED_TO TEXT, $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)")
        
        db?.execSQL("CREATE TABLE $TABLE_JOB_CARDS (" +
                "$COLUMN_JOB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_REF_REQ_ID INTEGER, " +
                "$COLUMN_TRUCK_MAKE TEXT, " +
                "$COLUMN_KILOMETERS REAL, " +
                "$COLUMN_PARTS_USED TEXT, " +
                "$COLUMN_REFUSED_REPAIRS TEXT, " +
                "$COLUMN_REFUSED_PHOTO TEXT, " +
                "$COLUMN_SIGNATURE_PATH TEXT, " +
                "$COLUMN_START_TIME TEXT, " +
                "$COLUMN_END_TIME TEXT, " +
                "$COLUMN_TRAVEL_KM REAL, " +
                "$COLUMN_STATUS TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_DIESEL_SLIPS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USER_EMAIL TEXT, $COLUMN_PHOTO_PATH TEXT, $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)")
        }
        if (oldVersion < 3) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_REQUESTS ($COLUMN_REQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USER_EMAIL TEXT, $COLUMN_TRUCK_REG TEXT, $COLUMN_DESCRIPTION TEXT, $COLUMN_PHOTO_PATH TEXT, $COLUMN_LATITUDE REAL, $COLUMN_LONGITUDE REAL, $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)")
        }
        if (oldVersion < 4) {
            try {
                db?.execSQL("ALTER TABLE $TABLE_REQUESTS ADD COLUMN $COLUMN_ASSIGNED_TO TEXT")
            } catch (e: Exception) {
                // Column might already exist or table was just created with this column
            }
        }
        if (oldVersion < 5) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_JOB_CARDS (" +
                    "$COLUMN_JOB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_REF_REQ_ID INTEGER, " +
                    "$COLUMN_TRUCK_MAKE TEXT, " +
                    "$COLUMN_KILOMETERS REAL, " +
                    "$COLUMN_PARTS_USED TEXT, " +
                    "$COLUMN_REFUSED_REPAIRS TEXT, " +
                    "$COLUMN_REFUSED_PHOTO TEXT, " +
                    "$COLUMN_SIGNATURE_PATH TEXT, " +
                    "$COLUMN_START_TIME TEXT, " +
                    "$COLUMN_END_TIME TEXT, " +
                    "$COLUMN_TRAVEL_KM REAL, " +
                    "$COLUMN_STATUS TEXT)")
        }
    }

    fun addUser(user: User): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAME, user.name)
            put(COLUMN_ROLE, user.role)
            put(COLUMN_PHONE, user.phone)
            put(COLUMN_COMPANY_NAME, user.companyName)
            put(COLUMN_COMPANY_ADDRESS, user.companyAddress)
            put(COLUMN_SPECIALTY, user.specialty)
        }
        return db.insert(TABLE_USERS, null, values) != -1L
    }

    fun getUser(email: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS, null, "$COLUMN_EMAIL=?", arrayOf(email), null, null, null)
        if (cursor.moveToFirst()) {
            val user = User(
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                companyName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_NAME)),
                companyAddress = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_ADDRESS)),
                specialty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIALTY))
            )
            cursor.close()
            return user
        }
        cursor.close()
        return null
    }

    fun addDieselSlip(email: String, photoPath: String): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_PHOTO_PATH, photoPath)
        }
        return this.writableDatabase.insert(TABLE_DIESEL_SLIPS, null, values) != -1L
    }

    fun getDieselSlips(email: String): List<DieselSlip> {
        val list = mutableListOf<DieselSlip>()
        val cursor = this.readableDatabase.query(TABLE_DIESEL_SLIPS, null, "$COLUMN_USER_EMAIL=?", arrayOf(email), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(DieselSlip(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PATH)),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addAssistanceRequest(request: AssistanceRequest): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, request.userEmail)
            put(COLUMN_TRUCK_REG, request.truckReg)
            put(COLUMN_DESCRIPTION, request.description)
            put(COLUMN_PHOTO_PATH, request.photoPath)
            put(COLUMN_LATITUDE, request.latitude)
            put(COLUMN_LONGITUDE, request.longitude)
            put(COLUMN_ASSIGNED_TO, request.assignedTo)
        }
        return this.writableDatabase.insert(TABLE_REQUESTS, null, values) != -1L
    }

    fun getAssignedJobs(techEmail: String): List<AssistanceRequest> {
        val list = mutableListOf<AssistanceRequest>()
        val cursor = this.readableDatabase.query(TABLE_REQUESTS, null, "$COLUMN_ASSIGNED_TO=?", arrayOf(techEmail), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(AssistanceRequest(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_ID)),
                    userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    truckReg = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRUCK_REG)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PATH)),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                    assignedTo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ASSIGNED_TO)),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateJobCard(jobCard: JobCard): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_REF_REQ_ID, jobCard.reqId)
            put(COLUMN_TRUCK_MAKE, jobCard.truckMake)
            put(COLUMN_KILOMETERS, jobCard.kilometers)
            put(COLUMN_PARTS_USED, jobCard.partsUsed)
            put(COLUMN_REFUSED_REPAIRS, jobCard.refusedRepairs)
            put(COLUMN_REFUSED_PHOTO, jobCard.refusedPhotoPath)
            put(COLUMN_SIGNATURE_PATH, jobCard.signaturePath)
            put(COLUMN_START_TIME, jobCard.startTime)
            put(COLUMN_END_TIME, jobCard.endTime)
            put(COLUMN_TRAVEL_KM, jobCard.travelKm)
            put(COLUMN_STATUS, jobCard.status)
        }
        
        val count = db.query(TABLE_JOB_CARDS, arrayOf(COLUMN_JOB_ID), "$COLUMN_REF_REQ_ID=?", arrayOf(jobCard.reqId.toString()), null, null, null).count
        return if (count > 0) {
            db.update(TABLE_JOB_CARDS, values, "$COLUMN_REF_REQ_ID=?", arrayOf(jobCard.reqId.toString())) > 0
        } else {
            db.insert(TABLE_JOB_CARDS, null, values) != -1L
        }
    }

    fun getJobCard(reqId: Int): JobCard? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_JOB_CARDS, null, "$COLUMN_REF_REQ_ID=?", arrayOf(reqId.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val card = JobCard(
                reqId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REF_REQ_ID)),
                truckMake = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRUCK_MAKE)),
                kilometers = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_KILOMETERS)),
                partsUsed = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTS_USED)),
                refusedRepairs = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFUSED_REPAIRS)),
                refusedPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFUSED_PHOTO)),
                signaturePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIGNATURE_PATH)),
                startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)),
                travelKm = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRAVEL_KM)),
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
            )
            cursor.close()
            return card
        }
        cursor.close()
        return null
    }
}

data class User(val email: String, val password: String, val name: String, val role: String, val phone: String = "", val companyName: String = "", val companyAddress: String = "", val specialty: String = "")
data class DieselSlip(val id: Int, val userEmail: String, val photoPath: String, val timestamp: String)
data class AssistanceRequest(val id: Int = 0, val userEmail: String, val truckReg: String, val description: String, val photoPath: String = "", val latitude: Double = 0.0, val longitude: Double = 0.0, val assignedTo: String = "", val timestamp: String = "")
data class JobCard(val reqId: Int, var truckMake: String = "", var kilometers: Double = 0.0, var partsUsed: String = "", var refusedRepairs: String = "", var refusedPhotoPath: String = "", var signaturePath: String = "", var startTime: String = "", var endTime: String = "", var travelKm: Double = 0.0, var status: String = "Not Started")
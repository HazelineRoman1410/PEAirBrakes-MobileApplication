package com.example.peairbrakes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PeAirbrakes.db"
        private const val DATABASE_VERSION = 7

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_ROLE = "role" // "Technician", "Customer", "Administrator"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_COMPANY_NAME = "company_name"
        private const val COLUMN_COMPANY_ADDRESS = "company_address"
        private const val COLUMN_SPECIALTY = "specialty"
        private const val COLUMN_QUALIFICATIONS = "qualifications"
        private const val COLUMN_EXPERIENCE_YEARS = "experience_years"
        private const val COLUMN_AVAILABILITY_STATUS = "availability_status" // "Available", "Busy", "Offline"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_TOOLS_EQUIPMENT = "tools_equipment"

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
        private const val COLUMN_VIN = "vin"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_SPECIALTY_REQUIRED = "specialty_required"
        private const val COLUMN_URGENCY = "urgency" // "Low", "Medium", "High", "Emergency"
        private const val COLUMN_ASSIGNED_TO = "assigned_to"
        private const val COLUMN_SECONDARY_TECHS = "secondary_techs"
        private const val COLUMN_MANUAL_OVERRIDE = "manual_override"
        private const val COLUMN_OVERRIDE_REASON = "override_reason"
        private const val COLUMN_REJECTION_REASON = "rejection_reason"
        private const val COLUMN_ETA = "eta"
        private const val COLUMN_STATUS = "status"

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
        private const val COLUMN_ACTUAL_LABOUR_HOURS = "actual_labour_hours"
        private const val COLUMN_WORK_PERFORMED = "work_performed"
        private const val COLUMN_FINAL_TOTAL_COST = "final_total_cost"
        private const val COLUMN_VERIFIED_BY_ADMIN = "verified_by_admin"
        private const val COLUMN_INVOICED = "invoiced"
        private const val COLUMN_SYNC_STATUS = "sync_status"

        // Breakdown Assessments table
        private const val TABLE_ASSESSMENTS = "breakdown_assessments"
        private const val COLUMN_ASSESSMENT_ID = "assessment_id"
        private const val COLUMN_TRUCK_MAKE_MODEL = "truck_make_model"
        private const val COLUMN_ODOMETER = "odometer"
        private const val COLUMN_ENGINE_INFO = "engine_info"
        private const val COLUMN_COMPLAINT = "complaint"
        private const val COLUMN_OBSERVATIONS = "observations"
        private const val COLUMN_FAULT_IDENTIFIED = "fault_identified"
        private const val COLUMN_CAUSE = "cause"
        private const val COLUMN_DIAGNOSTIC_RESULTS = "diagnostic_results"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_RECOMMENDATIONS = "recommendations"
        private const val COLUMN_ASSESSMENT_PHOTOS = "assessment_photos"
        private const val COLUMN_ESTIMATED_TECHS = "estimated_techs"
        private const val COLUMN_ESTIMATED_HOURS = "estimated_hours"
        private const val COLUMN_LABOUR_RATE = "labour_rate"
        private const val COLUMN_ESTIMATED_PARTS_COST = "estimated_parts_cost"
        private const val COLUMN_TRAVEL_EXPENSE = "travel_expense"
        private const val COLUMN_CALLOUT_FEE = "callout_fee"
        private const val COLUMN_TOLLS = "tolls"
        private const val COLUMN_TOTAL_ESTIMATED_COST = "total_estimated_cost"
        private const val COLUMN_QUOTATION_STATUS = "quotation_status"
        private const val COLUMN_ADMIN_DECISION_NOTES = "admin_decision_notes"
        private const val COLUMN_ADMIN_DECIDED_BY = "admin_decided_by"
        private const val COLUMN_ADMIN_DECIDED_AT = "admin_decided_at"

        // Safety Inspections table
        private const val TABLE_SAFETY_INSPECTIONS = "safety_inspections"
        private const val COLUMN_SAFETY_ID = "safety_id"
        private const val COLUMN_TECH_EMAIL = "tech_email"
        private const val COLUMN_AREA_SAFE = "area_safe" // "Safe", "Unsafe", "N/A"
        private const val COLUMN_AREA_NOTES = "area_notes"
        private const val COLUMN_AREA_PHOTO = "area_photo"
        private const val COLUMN_PPE_OK = "ppe_ok"
        private const val COLUMN_VEHICLE_SECURED = "vehicle_secured"
        private const val COLUMN_JOB_SAFETY_OK = "job_safety_ok"
        private const val COLUMN_SAFETY_STATUS = "safety_status" // "Passed", "Escalated"

        // Additional Work Requests table
        private const val TABLE_ADDITIONAL_WORK = "additional_work_requests"
        private const val COLUMN_WORK_REQ_ID = "work_req_id"
        private const val COLUMN_ADDITIONAL_COST = "additional_cost"
        private const val COLUMN_ADDITIONAL_STATUS = "additional_status" // "Pending", "Approved", "Rejected"

        // Complaints & Return Jobs table
        private const val TABLE_COMPLAINTS_RETURNS = "complaints_returns"
        private const val COLUMN_COMPLAINT_ID = "complaint_id"
        private const val COLUMN_COMPLAINT_REF = "complaint_ref"
        private const val COLUMN_ORIGINAL_REQ_ID = "original_req_id"
        private const val COLUMN_RETURN_REQ_ID = "return_req_id"
        private const val COLUMN_CUSTOMER_EMAIL = "customer_email"
        private const val COLUMN_FLAG_REASON = "flag_reason"
        private const val COLUMN_INVESTIGATION_NOTES = "investigation_notes"
        private const val COLUMN_RESOLUTION = "resolution"

        // Job Messages table
        private const val TABLE_MESSAGES = "job_messages"
        private const val COLUMN_MSG_ID = "msg_id"
        private const val COLUMN_SENDER_EMAIL = "sender_email"
        private const val COLUMN_SENDER_NAME = "sender_name"
        private const val COLUMN_RECEIVER_EMAIL = "receiver_email"
        private const val COLUMN_MESSAGE_TEXT = "message_text"
        private const val COLUMN_MEDIA_PATH = "media_path"

        // Notifications table
        private const val TABLE_NOTIFICATIONS = "notifications"
        private const val COLUMN_NOTIF_ID = "notif_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_MESSAGE = "message"
        private const val COLUMN_NOTIF_TYPE = "notif_type"
        private const val COLUMN_READ_STATUS = "read_status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_USERS (" +
                    "$COLUMN_EMAIL TEXT PRIMARY KEY, " +
                    "$COLUMN_PASSWORD TEXT, " +
                    "$COLUMN_NAME TEXT, " +
                    "$COLUMN_ROLE TEXT, " +
                    "$COLUMN_PHONE TEXT, " +
                    "$COLUMN_COMPANY_NAME TEXT, " +
                    "$COLUMN_COMPANY_ADDRESS TEXT, " +
                    "$COLUMN_SPECIALTY TEXT, " +
                    "$COLUMN_QUALIFICATIONS TEXT, " +
                    "$COLUMN_EXPERIENCE_YEARS INTEGER DEFAULT 3, " +
                    "$COLUMN_AVAILABILITY_STATUS TEXT DEFAULT 'Available', " +
                    "$COLUMN_LATITUDE REAL DEFAULT -33.9608, " +
                    "$COLUMN_LONGITUDE REAL DEFAULT 25.6022, " +
                    "$COLUMN_TOOLS_EQUIPMENT TEXT)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_DIESEL_SLIPS (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_USER_EMAIL TEXT, " +
                    "$COLUMN_PHOTO_PATH TEXT, " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_REQUESTS (" +
                    "$COLUMN_REQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_USER_EMAIL TEXT, " +
                    "$COLUMN_TRUCK_REG TEXT, " +
                    "$COLUMN_VIN TEXT, " +
                    "$COLUMN_DESCRIPTION TEXT, " +
                    "$COLUMN_SPECIALTY_REQUIRED TEXT, " +
                    "$COLUMN_URGENCY TEXT DEFAULT 'Medium', " +
                    "$COLUMN_PHOTO_PATH TEXT, " +
                    "$COLUMN_LATITUDE REAL, " +
                    "$COLUMN_LONGITUDE REAL, " +
                    "$COLUMN_ASSIGNED_TO TEXT, " +
                    "$COLUMN_SECONDARY_TECHS TEXT, " +
                    "$COLUMN_MANUAL_OVERRIDE INTEGER DEFAULT 0, " +
                    "$COLUMN_OVERRIDE_REASON TEXT, " +
                    "$COLUMN_REJECTION_REASON TEXT, " +
                    "$COLUMN_ETA TEXT, " +
                    "$COLUMN_STATUS TEXT DEFAULT 'New Job', " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_JOB_CARDS (" +
                    "$COLUMN_JOB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_REF_REQ_ID INTEGER UNIQUE, " +
                    "$COLUMN_TRUCK_MAKE TEXT, " +
                    "$COLUMN_KILOMETERS REAL, " +
                    "$COLUMN_PARTS_USED TEXT, " +
                    "$COLUMN_REFUSED_REPAIRS TEXT, " +
                    "$COLUMN_REFUSED_PHOTO TEXT, " +
                    "$COLUMN_SIGNATURE_PATH TEXT, " +
                    "$COLUMN_START_TIME TEXT, " +
                    "$COLUMN_END_TIME TEXT, " +
                    "$COLUMN_TRAVEL_KM REAL, " +
                    "$COLUMN_ACTUAL_LABOUR_HOURS REAL DEFAULT 0.0, " +
                    "$COLUMN_WORK_PERFORMED TEXT, " +
                    "$COLUMN_FINAL_TOTAL_COST REAL DEFAULT 0.0, " +
                    "$COLUMN_VERIFIED_BY_ADMIN INTEGER DEFAULT 0, " +
                    "$COLUMN_INVOICED INTEGER DEFAULT 0, " +
                    "$COLUMN_SYNC_STATUS TEXT DEFAULT 'Synced', " +
                    "$COLUMN_STATUS TEXT DEFAULT 'Not Started')"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_ASSESSMENTS (" +
                    "$COLUMN_ASSESSMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_REF_REQ_ID INTEGER UNIQUE, " +
                    "$COLUMN_TRUCK_MAKE_MODEL TEXT, " +
                    "$COLUMN_VIN TEXT, " +
                    "$COLUMN_ODOMETER REAL, " +
                    "$COLUMN_ENGINE_INFO TEXT, " +
                    "$COLUMN_COMPLAINT TEXT, " +
                    "$COLUMN_OBSERVATIONS TEXT, " +
                    "$COLUMN_FAULT_IDENTIFIED TEXT, " +
                    "$COLUMN_CAUSE TEXT, " +
                    "$COLUMN_DIAGNOSTIC_RESULTS TEXT, " +
                    "$COLUMN_CATEGORY TEXT, " +
                    "$COLUMN_RECOMMENDATIONS TEXT, " +
                    "$COLUMN_ASSESSMENT_PHOTOS TEXT, " +
                    "$COLUMN_ESTIMATED_TECHS INTEGER DEFAULT 1, " +
                    "$COLUMN_ESTIMATED_HOURS REAL DEFAULT 1.0, " +
                    "$COLUMN_LABOUR_RATE REAL DEFAULT 450.0, " +
                    "$COLUMN_ESTIMATED_PARTS_COST REAL DEFAULT 0.0, " +
                    "$COLUMN_TRAVEL_EXPENSE REAL DEFAULT 250.0, " +
                    "$COLUMN_CALLOUT_FEE REAL DEFAULT 500.0, " +
                    "$COLUMN_TOLLS REAL DEFAULT 0.0, " +
                    "$COLUMN_TOTAL_ESTIMATED_COST REAL DEFAULT 0.0, " +
                    "$COLUMN_QUOTATION_STATUS TEXT DEFAULT 'Draft', " +
                    "$COLUMN_ADMIN_DECISION_NOTES TEXT, " +
                    "$COLUMN_ADMIN_DECIDED_BY TEXT, " +
                    "$COLUMN_ADMIN_DECIDED_AT TEXT)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_SAFETY_INSPECTIONS (" +
                    "$COLUMN_SAFETY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_REF_REQ_ID INTEGER, " +
                    "$COLUMN_TECH_EMAIL TEXT, " +
                    "$COLUMN_AREA_SAFE TEXT, " +
                    "$COLUMN_AREA_NOTES TEXT, " +
                    "$COLUMN_AREA_PHOTO TEXT, " +
                    "$COLUMN_PPE_OK INTEGER, " +
                    "$COLUMN_VEHICLE_SECURED INTEGER, " +
                    "$COLUMN_JOB_SAFETY_OK INTEGER, " +
                    "$COLUMN_SAFETY_STATUS TEXT, " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_ADDITIONAL_WORK (" +
                    "$COLUMN_WORK_REQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_REF_REQ_ID INTEGER, " +
                    "$COLUMN_TECH_EMAIL TEXT, " +
                    "$COLUMN_DESCRIPTION TEXT, " +
                    "$COLUMN_ADDITIONAL_COST REAL, " +
                    "$COLUMN_ADDITIONAL_STATUS TEXT DEFAULT 'Pending', " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_COMPLAINTS_RETURNS (" +
                    "$COLUMN_COMPLAINT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_COMPLAINT_REF TEXT, " +
                    "$COLUMN_ORIGINAL_REQ_ID INTEGER, " +
                    "$COLUMN_RETURN_REQ_ID INTEGER, " +
                    "$COLUMN_CUSTOMER_EMAIL TEXT, " +
                    "$COLUMN_DESCRIPTION TEXT, " +
                    "$COLUMN_FLAG_REASON TEXT, " +
                    "$COLUMN_STATUS TEXT DEFAULT 'Open', " +
                    "$COLUMN_INVESTIGATION_NOTES TEXT, " +
                    "$COLUMN_RESOLUTION TEXT, " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_MESSAGES (" +
                    "$COLUMN_MSG_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_REF_REQ_ID INTEGER, " +
                    "$COLUMN_SENDER_EMAIL TEXT, " +
                    "$COLUMN_SENDER_NAME TEXT, " +
                    "$COLUMN_RECEIVER_EMAIL TEXT, " +
                    "$COLUMN_MESSAGE_TEXT TEXT, " +
                    "$COLUMN_MEDIA_PATH TEXT, " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        db?.execSQL(
            "CREATE TABLE $TABLE_NOTIFICATIONS (" +
                    "$COLUMN_NOTIF_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_USER_EMAIL TEXT, " +
                    "$COLUMN_TITLE TEXT, " +
                    "$COLUMN_MESSAGE TEXT, " +
                    "$COLUMN_REF_REQ_ID INTEGER, " +
                    "$COLUMN_NOTIF_TYPE TEXT, " +
                    "$COLUMN_READ_STATUS INTEGER DEFAULT 0, " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        if (db != null) {
            seedDefaultData(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db == null) return
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DIESEL_SLIPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REQUESTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_JOB_CARDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ASSESSMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAFETY_INSPECTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ADDITIONAL_WORK")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPLAINTS_RETURNS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        onCreate(db)
    }

    private fun seedDefaultData(db: SQLiteDatabase) {
        // Seed Administrator
        val adminVal = ContentValues().apply {
            put(COLUMN_EMAIL, "admin@peairbrakes.co.za")
            put(COLUMN_PASSWORD, "Admin123!")
            put(COLUMN_NAME, "Dispatch Administrator")
            put(COLUMN_ROLE, "Administrator")
            put(COLUMN_PHONE, "+27415550100")
            put(COLUMN_COMPANY_NAME, "PE Airbrakes Head Office")
            put(COLUMN_COMPANY_ADDRESS, "Port Elizabeth Industrial Park, Gqeberha")
            put(COLUMN_SPECIALTY, "Fleet & Dispatch Operations")
            put(COLUMN_AVAILABILITY_STATUS, "Available")
        }
        db.insert(TABLE_USERS, null, adminVal)

        // Seed Technicians
        val techs = listOf(
            User(
                email = "tech1@peairbrakes.co.za",
                password = "Tech123!",
                name = "Sipho Mabena",
                role = "Technician",
                phone = "+27821112233",
                companyName = "PE Airbrakes Tech Team",
                companyAddress = "Gqeberha Depot",
                specialty = "Air Brake Specialist",
                qualifications = "Certified WABCO & Knorr-Bremse Air Brakes Master",
                experienceYears = 8,
                availabilityStatus = "Available",
                latitude = -33.9608,
                longitude = 25.6022,
                toolsEquipment = "Diagnostic Scanner, Pneumatic Test Kit, Pressure Gauges"
            ),
            User(
                email = "tech2@peairbrakes.co.za",
                password = "Tech123!",
                name = "Johan van der Merwe",
                role = "Technician",
                phone = "+27834445566",
                companyName = "PE Airbrakes Tech Team",
                companyAddress = "Coega Depot",
                specialty = "Truck Engine Mechanic",
                qualifications = "Certified Diesel Master Mechanic (Cummins/Volvo)",
                experienceYears = 10,
                availabilityStatus = "Available",
                latitude = -33.7744,
                longitude = 25.6692,
                toolsEquipment = "Engine OBD Scanner, Heavy Torque Wrench Set, Fuel Injector Tester"
            ),
            User(
                email = "tech3@peairbrakes.co.za",
                password = "Tech123!",
                name = "Thabo Mokoena",
                role = "Technician",
                phone = "+27847778899",
                companyName = "PE Airbrakes Tech Team",
                companyAddress = "Uitenhage Depot",
                specialty = "Truck Electrical Specialist",
                qualifications = "Auto Electrical Specialist (CAN bus & Telematics)",
                experienceYears = 6,
                availabilityStatus = "Available",
                latitude = -33.7667,
                longitude = 25.4000,
                toolsEquipment = "Oscilloscope, Multimeter, Telematics Programmer"
            )
        )

        for (tech in techs) {
            val valTech = ContentValues().apply {
                put(COLUMN_EMAIL, tech.email)
                put(COLUMN_PASSWORD, tech.password)
                put(COLUMN_NAME, tech.name)
                put(COLUMN_ROLE, tech.role)
                put(COLUMN_PHONE, tech.phone)
                put(COLUMN_COMPANY_NAME, tech.companyName)
                put(COLUMN_COMPANY_ADDRESS, tech.companyAddress)
                put(COLUMN_SPECIALTY, tech.specialty)
                put(COLUMN_QUALIFICATIONS, tech.qualifications)
                put(COLUMN_EXPERIENCE_YEARS, tech.experienceYears)
                put(COLUMN_AVAILABILITY_STATUS, tech.availabilityStatus)
                put(COLUMN_LATITUDE, tech.latitude)
                put(COLUMN_LONGITUDE, tech.longitude)
                put(COLUMN_TOOLS_EQUIPMENT, tech.toolsEquipment)
            }
            db.insert(TABLE_USERS, null, valTech)
        }
    }

    // --- User Operations ---
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
            put(COLUMN_QUALIFICATIONS, user.qualifications)
            put(COLUMN_EXPERIENCE_YEARS, user.experienceYears)
            put(COLUMN_AVAILABILITY_STATUS, user.availabilityStatus)
            put(COLUMN_LATITUDE, user.latitude)
            put(COLUMN_LONGITUDE, user.longitude)
            put(COLUMN_TOOLS_EQUIPMENT, user.toolsEquipment)
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
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: "",
                companyName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_NAME)) ?: "",
                companyAddress = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_ADDRESS)) ?: "",
                specialty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIALTY)) ?: "",
                qualifications = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUALIFICATIONS)) ?: "",
                experienceYears = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPERIENCE_YEARS)),
                availabilityStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVAILABILITY_STATUS)) ?: "Available",
                latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                toolsEquipment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOOLS_EQUIPMENT)) ?: ""
            )
            cursor.close()
            return user
        }
        cursor.close()
        return null
    }

    fun getAllTechnicians(): List<User> {
        val list = mutableListOf<User>()
        val cursor = readableDatabase.query(TABLE_USERS, null, "$COLUMN_ROLE=?", arrayOf("Technician"), null, null, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    User(
                        email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: "",
                        companyName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_NAME)) ?: "",
                        companyAddress = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_ADDRESS)) ?: "",
                        specialty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIALTY)) ?: "",
                        qualifications = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUALIFICATIONS)) ?: "",
                        experienceYears = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPERIENCE_YEARS)),
                        availabilityStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVAILABILITY_STATUS)) ?: "Available",
                        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                        toolsEquipment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOOLS_EQUIPMENT)) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateUserStatusAndLocation(email: String, status: String, lat: Double, lon: Double): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_AVAILABILITY_STATUS, status)
            put(COLUMN_LATITUDE, lat)
            put(COLUMN_LONGITUDE, lon)
        }
        return writableDatabase.update(TABLE_USERS, values, "$COLUMN_EMAIL=?", arrayOf(email)) > 0
    }

    // --- Assistance Request & Intelligent Allocation Operations ---
    fun addAssistanceRequest(request: AssistanceRequest): Long {
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, request.userEmail)
            put(COLUMN_TRUCK_REG, request.truckReg)
            put(COLUMN_VIN, request.vin)
            put(COLUMN_DESCRIPTION, request.description)
            put(COLUMN_SPECIALTY_REQUIRED, request.specialtyRequired)
            put(COLUMN_URGENCY, request.urgency)
            put(COLUMN_PHOTO_PATH, request.photoPath)
            put(COLUMN_LATITUDE, request.latitude)
            put(COLUMN_LONGITUDE, request.longitude)
            put(COLUMN_ASSIGNED_TO, request.assignedTo)
            put(COLUMN_SECONDARY_TECHS, request.secondaryTechs)
            put(COLUMN_MANUAL_OVERRIDE, if (request.manualOverride) 1 else 0)
            put(COLUMN_OVERRIDE_REASON, request.overrideReason)
            put(COLUMN_REJECTION_REASON, request.rejectionReason)
            put(COLUMN_ETA, request.eta)
            put(COLUMN_STATUS, request.status)
        }
        return this.writableDatabase.insert(TABLE_REQUESTS, null, values)
    }

    fun getAssistanceRequest(reqId: Int): AssistanceRequest? {
        val cursor = readableDatabase.query(TABLE_REQUESTS, null, "$COLUMN_REQ_ID=?", arrayOf(reqId.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val req = cursorToRequest(cursor)
            cursor.close()
            return req
        }
        cursor.close()
        return null
    }

    fun getAllRequests(): List<AssistanceRequest> {
        val list = mutableListOf<AssistanceRequest>()
        val cursor = readableDatabase.query(TABLE_REQUESTS, null, null, null, null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToRequest(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAssignedJobs(techEmail: String): List<AssistanceRequest> {
        val list = mutableListOf<AssistanceRequest>()
        val selection = "$COLUMN_ASSIGNED_TO = ? OR $COLUMN_SECONDARY_TECHS LIKE ?"
        val cursor = readableDatabase.query(TABLE_REQUESTS, null, selection, arrayOf(techEmail, "%$techEmail%"), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToRequest(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getCustomerRequests(customerEmail: String): List<AssistanceRequest> {
        val list = mutableListOf<AssistanceRequest>()
        val cursor = readableDatabase.query(TABLE_REQUESTS, null, "$COLUMN_USER_EMAIL=?", arrayOf(customerEmail), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToRequest(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateRequestStatus(reqId: Int, newStatus: String, eta: String = ""): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_STATUS, newStatus)
            if (eta.isNotEmpty()) put(COLUMN_ETA, eta)
        }
        return writableDatabase.update(TABLE_REQUESTS, values, "$COLUMN_REQ_ID=?", arrayOf(reqId.toString())) > 0
    }

    fun updateRequestAssignment(reqId: Int, techEmail: String, isManual: Boolean, overrideReason: String = "", secondaryTechs: String = ""): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_ASSIGNED_TO, techEmail)
            put(COLUMN_SECONDARY_TECHS, secondaryTechs)
            put(COLUMN_MANUAL_OVERRIDE, if (isManual) 1 else 0)
            put(COLUMN_OVERRIDE_REASON, overrideReason)
            put(COLUMN_STATUS, "Technician Assigned")
        }
        return writableDatabase.update(TABLE_REQUESTS, values, "$COLUMN_REQ_ID=?", arrayOf(reqId.toString())) > 0
    }

    fun rejectAssignment(reqId: Int, techEmail: String, reason: String): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_ASSIGNED_TO, "")
            put(COLUMN_REJECTION_REASON, "Rejected by $techEmail: $reason")
            put(COLUMN_STATUS, "Awaiting Technician Allocation")
        }
        return writableDatabase.update(TABLE_REQUESTS, values, "$COLUMN_REQ_ID=?", arrayOf(reqId.toString())) > 0
    }

    private fun cursorToRequest(cursor: android.database.Cursor): AssistanceRequest {
        return AssistanceRequest(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_ID)),
            userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
            truckReg = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRUCK_REG)),
            vin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIN)) ?: "",
            description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            specialtyRequired = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIALTY_REQUIRED)) ?: "General Truck Mechanic",
            urgency = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URGENCY)) ?: "Medium",
            photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PATH)) ?: "",
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
            assignedTo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ASSIGNED_TO)) ?: "",
            secondaryTechs = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SECONDARY_TECHS)) ?: "",
            manualOverride = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MANUAL_OVERRIDE)) == 1,
            overrideReason = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OVERRIDE_REASON)) ?: "",
            rejectionReason = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REJECTION_REASON)) ?: "",
            eta = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ETA)) ?: "",
            status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)) ?: "New Job",
            timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
        )
    }

    // --- Job Card Operations ---
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
            put(COLUMN_ACTUAL_LABOUR_HOURS, jobCard.actualLabourHours)
            put(COLUMN_WORK_PERFORMED, jobCard.workPerformed)
            put(COLUMN_FINAL_TOTAL_COST, jobCard.finalTotalCost)
            put(COLUMN_VERIFIED_BY_ADMIN, if (jobCard.verifiedByAdmin) 1 else 0)
            put(COLUMN_INVOICED, if (jobCard.invoiced) 1 else 0)
            put(COLUMN_SYNC_STATUS, jobCard.syncStatus)
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
                truckMake = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRUCK_MAKE)) ?: "",
                kilometers = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_KILOMETERS)),
                partsUsed = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARTS_USED)) ?: "",
                refusedRepairs = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFUSED_REPAIRS)) ?: "",
                refusedPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REFUSED_PHOTO)) ?: "",
                signaturePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIGNATURE_PATH)) ?: "",
                startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)) ?: "",
                endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)) ?: "",
                travelKm = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRAVEL_KM)),
                actualLabourHours = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ACTUAL_LABOUR_HOURS)),
                workPerformed = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORK_PERFORMED)) ?: "",
                finalTotalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FINAL_TOTAL_COST)),
                verifiedByAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VERIFIED_BY_ADMIN)) == 1,
                invoiced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INVOICED)) == 1,
                syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SYNC_STATUS)) ?: "Synced",
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)) ?: "Not Started"
            )
            cursor.close()
            return card
        }
        cursor.close()
        return null
    }

    // --- Breakdown Assessment Operations ---
    fun saveAssessment(assessment: BreakdownAssessment): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_REF_REQ_ID, assessment.reqIdRef)
            put(COLUMN_TRUCK_MAKE_MODEL, assessment.truckMakeModel)
            put(COLUMN_VIN, assessment.vin)
            put(COLUMN_ODOMETER, assessment.odometer)
            put(COLUMN_ENGINE_INFO, assessment.engineInfo)
            put(COLUMN_COMPLAINT, assessment.complaint)
            put(COLUMN_OBSERVATIONS, assessment.observations)
            put(COLUMN_FAULT_IDENTIFIED, assessment.faultIdentified)
            put(COLUMN_CAUSE, assessment.cause)
            put(COLUMN_DIAGNOSTIC_RESULTS, assessment.diagnosticResults)
            put(COLUMN_CATEGORY, assessment.category)
            put(COLUMN_RECOMMENDATIONS, assessment.recommendations)
            put(COLUMN_ASSESSMENT_PHOTOS, assessment.photos)
            put(COLUMN_ESTIMATED_TECHS, assessment.estimatedTechs)
            put(COLUMN_ESTIMATED_HOURS, assessment.estimatedHours)
            put(COLUMN_LABOUR_RATE, assessment.labourRate)
            put(COLUMN_ESTIMATED_PARTS_COST, assessment.estimatedPartsCost)
            put(COLUMN_TRAVEL_EXPENSE, assessment.travelExpense)
            put(COLUMN_CALLOUT_FEE, assessment.calloutFee)
            put(COLUMN_TOLLS, assessment.tolls)
            put(COLUMN_TOTAL_ESTIMATED_COST, assessment.totalEstimatedCost)
            put(COLUMN_QUOTATION_STATUS, assessment.quotationStatus)
            put(COLUMN_ADMIN_DECISION_NOTES, assessment.adminDecisionNotes)
            put(COLUMN_ADMIN_DECIDED_BY, assessment.adminDecidedBy)
            put(COLUMN_ADMIN_DECIDED_AT, assessment.adminDecidedAt)
        }
        val count = db.query(TABLE_ASSESSMENTS, arrayOf(COLUMN_ASSESSMENT_ID), "$COLUMN_REF_REQ_ID=?", arrayOf(assessment.reqIdRef.toString()), null, null, null).count
        return if (count > 0) {
            db.update(TABLE_ASSESSMENTS, values, "$COLUMN_REF_REQ_ID=?", arrayOf(assessment.reqIdRef.toString())) > 0
        } else {
            db.insert(TABLE_ASSESSMENTS, null, values) != -1L
        }
    }

    fun getAssessment(reqId: Int): BreakdownAssessment? {
        val cursor = readableDatabase.query(TABLE_ASSESSMENTS, null, "$COLUMN_REF_REQ_ID=?", arrayOf(reqId.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val a = BreakdownAssessment(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ASSESSMENT_ID)),
                reqIdRef = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REF_REQ_ID)),
                truckMakeModel = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRUCK_MAKE_MODEL)) ?: "",
                vin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIN)) ?: "",
                odometer = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ODOMETER)),
                engineInfo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGINE_INFO)) ?: "",
                complaint = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT)) ?: "",
                observations = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBSERVATIONS)) ?: "",
                faultIdentified = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAULT_IDENTIFIED)) ?: "",
                cause = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAUSE)) ?: "",
                diagnosticResults = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIAGNOSTIC_RESULTS)) ?: "",
                category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)) ?: "",
                recommendations = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECOMMENDATIONS)) ?: "",
                photos = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ASSESSMENT_PHOTOS)) ?: "",
                estimatedTechs = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ESTIMATED_TECHS)),
                estimatedHours = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ESTIMATED_HOURS)),
                labourRate = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LABOUR_RATE)),
                estimatedPartsCost = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ESTIMATED_PARTS_COST)),
                travelExpense = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TRAVEL_EXPENSE)),
                calloutFee = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALLOUT_FEE)),
                tolls = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOLLS)),
                totalEstimatedCost = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_ESTIMATED_COST)),
                quotationStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUOTATION_STATUS)) ?: "Draft",
                adminDecisionNotes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADMIN_DECISION_NOTES)) ?: "",
                adminDecidedBy = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADMIN_DECIDED_BY)) ?: "",
                adminDecidedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADMIN_DECIDED_AT)) ?: ""
            )
            cursor.close()
            return a
        }
        cursor.close()
        return null
    }

    // --- Safety Inspection Operations ---
    fun saveSafetyInspection(inspection: SafetyInspection): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_REF_REQ_ID, inspection.reqIdRef)
            put(COLUMN_TECH_EMAIL, inspection.techEmail)
            put(COLUMN_AREA_SAFE, inspection.areaSafe)
            put(COLUMN_AREA_NOTES, inspection.areaNotes)
            put(COLUMN_AREA_PHOTO, inspection.areaPhoto)
            put(COLUMN_PPE_OK, if (inspection.ppeOk) 1 else 0)
            put(COLUMN_VEHICLE_SECURED, if (inspection.vehicleSecured) 1 else 0)
            put(COLUMN_JOB_SAFETY_OK, if (inspection.jobSafetyOk) 1 else 0)
            put(COLUMN_SAFETY_STATUS, inspection.status)
        }
        return writableDatabase.insert(TABLE_SAFETY_INSPECTIONS, null, values) != -1L
    }

    fun getSafetyInspection(reqId: Int): SafetyInspection? {
        val cursor = readableDatabase.query(TABLE_SAFETY_INSPECTIONS, null, "$COLUMN_REF_REQ_ID=?", arrayOf(reqId.toString()), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            val s = SafetyInspection(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SAFETY_ID)),
                reqIdRef = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REF_REQ_ID)),
                techEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TECH_EMAIL)) ?: "",
                areaSafe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AREA_SAFE)) ?: "Safe",
                areaNotes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AREA_NOTES)) ?: "",
                areaPhoto = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AREA_PHOTO)) ?: "",
                ppeOk = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PPE_OK)) == 1,
                vehicleSecured = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_SECURED)) == 1,
                jobSafetyOk = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JOB_SAFETY_OK)) == 1,
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAFETY_STATUS)) ?: "Passed",
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            )
            cursor.close()
            return s
        }
        cursor.close()
        return null
    }

    // --- Additional Work Requests Operations ---
    fun addAdditionalWorkRequest(reqId: Int, techEmail: String, description: String, cost: Double): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_REF_REQ_ID, reqId)
            put(COLUMN_TECH_EMAIL, techEmail)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_ADDITIONAL_COST, cost)
            put(COLUMN_ADDITIONAL_STATUS, "Pending")
        }
        return writableDatabase.insert(TABLE_ADDITIONAL_WORK, null, values) != -1L
    }

    fun getAdditionalWorkRequests(reqId: Int): List<AdditionalWorkRequest> {
        val list = mutableListOf<AdditionalWorkRequest>()
        val cursor = readableDatabase.query(TABLE_ADDITIONAL_WORK, null, "$COLUMN_REF_REQ_ID=?", arrayOf(reqId.toString()), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    AdditionalWorkRequest(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORK_REQ_ID)),
                        reqIdRef = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REF_REQ_ID)),
                        techEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TECH_EMAIL)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        additionalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ADDITIONAL_COST)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDITIONAL_STATUS)),
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateAdditionalWorkStatus(workReqId: Int, status: String): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_ADDITIONAL_STATUS, status)
        }
        return writableDatabase.update(TABLE_ADDITIONAL_WORK, values, "$COLUMN_WORK_REQ_ID=?", arrayOf(workReqId.toString())) > 0
    }

    // --- Complaints & Return Jobs Operations ---
    fun createComplaintReturnJob(complaint: ComplaintReturnJob): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_COMPLAINT_REF, complaint.complaintRef)
            put(COLUMN_ORIGINAL_REQ_ID, complaint.originalReqId)
            put(COLUMN_RETURN_REQ_ID, complaint.returnReqId)
            put(COLUMN_CUSTOMER_EMAIL, complaint.customerEmail)
            put(COLUMN_DESCRIPTION, complaint.description)
            put(COLUMN_FLAG_REASON, complaint.flagReason)
            put(COLUMN_STATUS, complaint.status)
            put(COLUMN_INVESTIGATION_NOTES, complaint.investigationNotes)
            put(COLUMN_RESOLUTION, complaint.resolution)
        }
        return writableDatabase.insert(TABLE_COMPLAINTS_RETURNS, null, values) != -1L
    }

    fun getAllComplaints(): List<ComplaintReturnJob> {
        val list = mutableListOf<ComplaintReturnJob>()
        val cursor = readableDatabase.query(TABLE_COMPLAINTS_RETURNS, null, null, null, null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    ComplaintReturnJob(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_ID)),
                        complaintRef = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINT_REF)),
                        originalReqId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORIGINAL_REQ_ID)),
                        returnReqId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RETURN_REQ_ID)),
                        customerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_EMAIL)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        flagReason = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLAG_REASON)),
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                        investigationNotes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INVESTIGATION_NOTES)) ?: "",
                        resolution = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RESOLUTION)) ?: "",
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- In-App Messages Operations ---
    fun addMessage(msg: JobMessage): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_REF_REQ_ID, msg.reqIdRef)
            put(COLUMN_SENDER_EMAIL, msg.senderEmail)
            put(COLUMN_SENDER_NAME, msg.senderName)
            put(COLUMN_RECEIVER_EMAIL, msg.receiverEmail)
            put(COLUMN_MESSAGE_TEXT, msg.messageText)
            put(COLUMN_MEDIA_PATH, msg.mediaPath)
        }
        return writableDatabase.insert(TABLE_MESSAGES, null, values) != -1L
    }

    fun getMessagesForJob(reqId: Int): List<JobMessage> {
        val list = mutableListOf<JobMessage>()
        val cursor = readableDatabase.query(TABLE_MESSAGES, null, "$COLUMN_REF_REQ_ID=?", arrayOf(reqId.toString()), null, null, "$COLUMN_TIMESTAMP ASC")
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    JobMessage(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MSG_ID)),
                        reqIdRef = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REF_REQ_ID)),
                        senderEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_EMAIL)),
                        senderName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_NAME)),
                        receiverEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECEIVER_EMAIL)),
                        messageText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TEXT)),
                        mediaPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_PATH)) ?: "",
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- Notification Operations ---
    fun addNotification(notif: AppNotification): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, notif.userEmail)
            put(COLUMN_TITLE, notif.title)
            put(COLUMN_MESSAGE, notif.message)
            put(COLUMN_REF_REQ_ID, notif.reqIdRef)
            put(COLUMN_NOTIF_TYPE, notif.type)
            put(COLUMN_READ_STATUS, if (notif.readStatus) 1 else 0)
        }
        return writableDatabase.insert(TABLE_NOTIFICATIONS, null, values) != -1L
    }

    fun getNotifications(userEmail: String): List<AppNotification> {
        val list = mutableListOf<AppNotification>()
        val cursor = readableDatabase.query(TABLE_NOTIFICATIONS, null, "$COLUMN_USER_EMAIL=? OR $COLUMN_USER_EMAIL='ALL'", arrayOf(userEmail), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    AppNotification(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_ID)),
                        userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                        title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)),
                        reqIdRef = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REF_REQ_ID)),
                        type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIF_TYPE)),
                        readStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_READ_STATUS)) == 1,
                        timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- Diesel Slips Operations ---
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

    // --- Truck Vehicle History Query ---
    fun getVehicleHistory(truckReg: String): List<AssistanceRequest> {
        val list = mutableListOf<AssistanceRequest>()
        val cursor = readableDatabase.query(TABLE_REQUESTS, null, "$COLUMN_TRUCK_REG=?", arrayOf(truckReg), null, null, "$COLUMN_TIMESTAMP DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToRequest(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}

// Data models
data class User(
    val email: String,
    val password: String,
    val name: String,
    val role: String, // "Technician", "Customer", "Administrator"
    val phone: String = "",
    val companyName: String = "",
    val companyAddress: String = "",
    val specialty: String = "",
    val qualifications: String = "",
    val experienceYears: Int = 3,
    val availabilityStatus: String = "Available", // "Available", "Busy", "Offline"
    val latitude: Double = -33.9608,
    val longitude: Double = 25.6022,
    val toolsEquipment: String = ""
)

data class AssistanceRequest(
    val id: Int = 0,
    val userEmail: String,
    val truckReg: String,
    val vin: String = "",
    val description: String,
    val specialtyRequired: String = "General Truck Mechanic",
    val urgency: String = "Medium", // "Low", "Medium", "High", "Emergency"
    val photoPath: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val assignedTo: String = "",
    val secondaryTechs: String = "",
    val manualOverride: Boolean = false,
    val overrideReason: String = "",
    val rejectionReason: String = "",
    val eta: String = "",
    val status: String = "New Job", // Workflow stages 1-20
    val timestamp: String = ""
)

data class JobCard(
    val reqId: Int,
    var truckMake: String = "",
    var kilometers: Double = 0.0,
    var partsUsed: String = "",
    var refusedRepairs: String = "",
    var refusedPhotoPath: String = "",
    var signaturePath: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var travelKm: Double = 0.0,
    var actualLabourHours: Double = 0.0,
    var workPerformed: String = "",
    var finalTotalCost: Double = 0.0,
    var verifiedByAdmin: Boolean = false,
    var invoiced: Boolean = false,
    var syncStatus: String = "Synced",
    var status: String = "Not Started"
)

data class BreakdownAssessment(
    val id: Int = 0,
    val reqIdRef: Int,
    var truckMakeModel: String = "",
    var vin: String = "",
    var odometer: Double = 0.0,
    var engineInfo: String = "",
    var complaint: String = "",
    var observations: String = "",
    var faultIdentified: String = "",
    var cause: String = "",
    var diagnosticResults: String = "",
    var category: String = "",
    var recommendations: String = "",
    var photos: String = "",
    var estimatedTechs: Int = 1,
    var estimatedHours: Double = 1.0,
    var labourRate: Double = 450.0,
    var estimatedPartsCost: Double = 0.0,
    var travelExpense: Double = 250.0,
    var calloutFee: Double = 500.0,
    var tolls: Double = 0.0,
    var totalEstimatedCost: Double = 0.0,
    var quotationStatus: String = "Draft", // "Draft", "Submitted", "Approved", "Rejected", "Revision Requested"
    var adminDecisionNotes: String = "",
    var adminDecidedBy: String = "",
    var adminDecidedAt: String = ""
)

data class SafetyInspection(
    val id: Int = 0,
    val reqIdRef: Int,
    val techEmail: String,
    val areaSafe: String = "Safe", // "Safe", "Unsafe", "N/A"
    val areaNotes: String = "",
    val areaPhoto: String = "",
    val ppeOk: Boolean = true,
    val vehicleSecured: Boolean = true,
    val jobSafetyOk: Boolean = true,
    val status: String = "Passed", // "Passed", "Escalated"
    val timestamp: String = ""
)

data class AdditionalWorkRequest(
    val id: Int = 0,
    val reqIdRef: Int,
    val techEmail: String,
    val description: String,
    val additionalCost: Double,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val timestamp: String = ""
)

data class ComplaintReturnJob(
    val id: Int = 0,
    val complaintRef: String,
    val originalReqId: Int,
    val returnReqId: Int = 0,
    val customerEmail: String,
    val description: String,
    val flagReason: String, // "Repair Unsuccessful", "Specialist Required", "Parts Unavailable", "Workmanship Concern", "Warranty", "Other"
    val status: String = "Open",
    val investigationNotes: String = "",
    val resolution: String = "",
    val timestamp: String = ""
)

data class JobMessage(
    val id: Int = 0,
    val reqIdRef: Int,
    val senderEmail: String,
    val senderName: String,
    val receiverEmail: String,
    val messageText: String,
    val mediaPath: String = "",
    val timestamp: String = ""
)

data class AppNotification(
    val id: Int = 0,
    val userEmail: String,
    val title: String,
    val message: String,
    val reqIdRef: Int = 0,
    val type: String = "General",
    val readStatus: Boolean = false,
    val timestamp: String = ""
)

data class DieselSlip(
    val id: Int,
    val userEmail: String,
    val photoPath: String,
    val timestamp: String
)

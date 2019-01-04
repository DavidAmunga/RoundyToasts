package miles.identigate.soja.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.IncidentModel;
import miles.identigate.soja.Models.PremiseResident;
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.ServiceProviderModel;
import miles.identigate.soja.Models.TypeObject;
/**
 * Created by myles on 10/23/15.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    public static final String DB_NAME="SOJADB";

    public static final String TABLE_DRIVE_IN="driveIn";
    public static final String TABLE_SERVICE_PROVIDERS="serviceProviders";
    public static final String TABLE_RESIDENTS="residents";
    public static final String TABLE_INCIDENTS="incidents";
    public static final String TABLE_INCIDENT_TYPES="incidentTypes";
    public static final String TABLE_VISITOR_TYPES="visitorTypes";
    public static final String TABLE_SERVICE_PROVIDERS_TYPES="serviceProviderTypes";
    public static final String TABLE_HOUSES="houses";
    public static final String TABLE_PREMISE_RESIDENTS = "premiseResidents";


    public static final String VEHICLE_COLORS="vehicleColors";
    public static final String VEHICLE_MODELS="vehicleModels";
    public static final String VEHICLE_TYPES="vehicleTypes";
    //The table columns
    public static final String name="name";
    public static final String national_id="nationalid";
    public static final String visitorType="visitorType";
    public static final String carNumber="carNumber";
    public static final String image="image";
    public static final String entryTime="entryTime";
    public static final String status="status";///Whether still IN or OUT
    public static final String exitTime="exitTime";
    public static final String recordType="recordType";
    public static final String company="company";
    public  static final String house="house";
    public  static final String category="category";
    public  static final String description="description";
    public static final String hostId="hostId";

    //Premise residents
    public static final String FIRSTNAME="firstName";
    public static final String LASTNAME="lastName";
    public static final String IDNUMBER="idNumber";
    public static final String FINGERPRINT = "fingerprint";
    public static final String FINGERPRINTLEN = "fingerprintLen";

    public static final String otherNames="othernames";
    public static final String dob="dob";
    public static final String sex="sex";
    public static final String idType="idType";

    public static final String id="id";

    public static final String synced="synced";
    private static final String houseID="houseId";

    public static final int DB_VERSION=13;

    public final String CREATE_TABLE_DRIVE_IN = "CREATE TABLE "
            + TABLE_DRIVE_IN + "(" + name + " TEXT, "+ national_id + " TEXT, " + visitorType + " TEXT, " + carNumber + " TEXT, " + image + " TEXT, " + entryTime + " TEXT, "
            +status + " TEXT, " +exitTime + " TEXT, "+recordType +" TEXT, "+otherNames+" TEXT, "+dob+" TEXT, "+sex+" TEXT, "+idType+" TEXT, "+synced+" INTEGER, "+houseID+" TEXT "
            +");";
    public final String CREATE_TABLE_SERVICE_PROVIDERS = "CREATE TABLE "
            + TABLE_SERVICE_PROVIDERS + "(" + name + " TEXT, "+ national_id + " TEXT, " + company + " TEXT, " + entryTime + " TEXT, " + exitTime + " TEXT, " + image + " TEXT, "+otherNames+" TEXT, "+dob+" TEXT, "+sex+" TEXT, "+idType+" TEXT, "+synced+" INTEGER "
            +");";
    public final String CREATE_TABLE_RESIDENTS = "CREATE TABLE "
            + TABLE_RESIDENTS + "(" + name + " TEXT, "+ national_id + " TEXT, " + house + " TEXT, " + entryTime + " TEXT, " + exitTime + " TEXT, " + image + " TEXT, "+otherNames+" TEXT, "+dob+" TEXT, "+sex+" TEXT, "+idType+" TEXT, "+synced+" INTEGER "
            +");";
    public final String CREATE_TABLE_INCIDENT_TYPES = "CREATE TABLE "
            + TABLE_INCIDENT_TYPES + "(" + id + " TEXT, "+ description + " TEXT " +");";
    public final String CREATE_TABLE_VISITOR_TYPES = "CREATE TABLE "
            + TABLE_VISITOR_TYPES + "(" + id + " TEXT, "+ name + " TEXT " +");";
    public final String CREATE_TABLE_SERVICE_PROVIDERS_TYPES = "CREATE TABLE "
            + TABLE_SERVICE_PROVIDERS_TYPES + "(" + id + " TEXT, "+ name + " TEXT, " + description + " TEXT "+");";
    public final String CREATE_TABLE_INCIDENTS = "CREATE TABLE "
            + TABLE_INCIDENTS + "(" + name + " TEXT, "+ national_id + " TEXT, " + category + " TEXT, " + description + " TEXT, " + entryTime + " TEXT, "+otherNames+" TEXT, "+dob+" TEXT, "+sex+" TEXT, "+idType+" TEXT, "+synced+" INTEGER "
            +");";
    public final String CREATE_TABLE_HOUSES = "CREATE TABLE "
            + TABLE_HOUSES + "(" + id + " TEXT, "+ description + " TEXT, " + hostId + " TEXT " +");";

    public final String CREATE_PREMISE_RESIDENTS_TABLE = "CREATE TABLE "
            + TABLE_PREMISE_RESIDENTS + "(" + IDNUMBER + " TEXT, "+ FIRSTNAME + " TEXT, " + LASTNAME + " TEXT, " + FINGERPRINT + " TEXT, " + FINGERPRINTLEN + " INTEGER, " + id + " TEXT, " + house + " TEXT, " + hostId + " TEXT "
            +");";

    public DatabaseHandler(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_DRIVE_IN);
            db.execSQL(CREATE_TABLE_SERVICE_PROVIDERS);
            db.execSQL(CREATE_TABLE_RESIDENTS);
            db.execSQL(CREATE_TABLE_INCIDENTS);
            db.execSQL(CREATE_TABLE_INCIDENT_TYPES);
            db.execSQL(CREATE_TABLE_VISITOR_TYPES);
            db.execSQL(CREATE_TABLE_SERVICE_PROVIDERS_TYPES);
            db.execSQL(CREATE_TABLE_HOUSES);
            db.execSQL(CREATE_PREMISE_RESIDENTS_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop older tables if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRIVE_IN);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENT_TYPES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE_PROVIDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESIDENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VISITOR_TYPES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE_PROVIDERS_TYPES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUSES);

            db.execSQL("DROP TABLE IF EXISTS " + VEHICLE_MODELS);
            db.execSQL("DROP TABLE IF EXISTS " + VEHICLE_TYPES);
            db.execSQL("DROP TABLE IF EXISTS " + VEHICLE_COLORS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREMISE_RESIDENTS);

            onCreate(db);

        }
        public void insertPremiseVisitor(String _id,String idNumber, String firstName, String lastName, String fingerprint, int fingerprintLen, String _house, String _hostId){
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(id, _id);
            values.put(FINGERPRINT, fingerprint);
            values.put(FIRSTNAME, firstName);
            values.put(LASTNAME, lastName);
            values.put(IDNUMBER, idNumber);
            values.put(FINGERPRINTLEN, fingerprintLen);
            values.put(house, _house);
            values.put(hostId, _hostId);
            db.insert(TABLE_PREMISE_RESIDENTS, null, values);
            db.close();
        }

        public ArrayList<PremiseResident> getPremiseResidents(){
            ArrayList<PremiseResident> premiseResidents = new ArrayList<PremiseResident>();
            String selectQuery = "SELECT  * FROM " + TABLE_PREMISE_RESIDENTS;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    PremiseResident premiseResident = new PremiseResident();
                    premiseResident.setIdNumber(cursor.getString(0));
                    premiseResident.setFirstName(cursor.getString(1));
                    premiseResident.setLastName(cursor.getString(2));
                    premiseResident.setFingerPrint(cursor.getString(3));
                    premiseResident.setFingerPrintLen(cursor.getInt(4));
                    premiseResident.setId(cursor.getString(5));
                    premiseResident.setHouse(cursor.getString(6));
                    premiseResident.setHostId(cursor.getString(7));

                    premiseResidents.add(premiseResident);
                } while (cursor.moveToNext());
            }
            db.close();
            return premiseResidents;
        }
        public void updatePremiseResident(PremiseResident premiseResident){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(FINGERPRINT, premiseResident.getFingerPrint());
            values.put(FINGERPRINTLEN, premiseResident.getFingerPrintLen());

            String where = id + "=?";
            String[] whereArgs = new String[] {String.valueOf(premiseResident.getId())};

            db.update(TABLE_PREMISE_RESIDENTS, values, where, whereArgs);
            db.close();
        }

        public void insertDriveIn(DriveIn driveIn) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(name, driveIn.getName());
            values.put(national_id, driveIn.getNationalId());
            values.put(visitorType, driveIn.getVisitorType());
            values.put(carNumber, driveIn.getCarNumber());
            values.put(image, driveIn.getImage());
            values.put(entryTime, driveIn.getEntryTime());
            values.put(status, driveIn.getStatus());
            values.put(exitTime, driveIn.getExitTime());
            values.put(recordType, driveIn.getRecordType());
            values.put(otherNames,driveIn.getOtherNames());
            values.put(dob,driveIn.getDob());
            values.put(sex,driveIn.getSex());
            values.put(idType,driveIn.getIdType());
            values.put(synced,(driveIn.isSynced()==true?0:1));
            values.put(houseID,driveIn.getHouseID());
            db.insert(TABLE_DRIVE_IN, null, values);
            db.close();
        }
        public void insertServiceProvider(ServiceProviderModel model){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(name, model.getProviderName());
            values.put(national_id, model.getNationalId());
            values.put(company, model.getCompanyName());
            values.put(entryTime, model.getEntryTime());
            values.put(image, model.getProviderImage());
            values.put(exitTime, model.getExitTime());
            values.put(otherNames,model.getOtherNames());
            values.put(dob,model.getDob());
            values.put(sex,model.getSex());
            values.put(idType,model.getIdType());
            values.put(synced,0);
            db.insert(TABLE_SERVICE_PROVIDERS, null, values);
            db.close();
        }
        public void insertResident(Resident model){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(name, model.getName());
            values.put(national_id, model.getNationalId());
            values.put(house, model.getHouse());
            values.put(entryTime, model.getEntryTime());
            values.put(image, model.getImage());
            values.put(exitTime, model.getExitTime());
            values.put(otherNames,model.getOtherNames());
            values.put(dob,model.getDob());
            values.put(sex,model.getSex());
            values.put(idType,model.getIdType());
            values.put(synced,0);
            db.insert(TABLE_RESIDENTS, null, values);
            db.close();
        }
        public void insertIncident(IncidentModel model){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(name,model.getName());
            values.put(national_id,model.getNationalId());
            values.put(category, model.getCategory());
            values.put(description, model.getDescription());
            values.put(entryTime, model.getDate());
            values.put(otherNames,model.getOtherNames());
            values.put(dob,model.getDob());
            values.put(sex,model.getSex());
            values.put(idType,model.getIdType());
            values.put(synced,0);
            db.insert(TABLE_INCIDENTS, null, values);
            db.close();
        }
        public void updateExitTime(String type,String time,String id) {
            SQLiteDatabase db = this.getWritableDatabase();
            /*if(type.equals("DRIVE")){
                String sql1 = "UPDATE "+TABLE_DRIVE_IN +" SET " + exitTime+ " = '"+time+"' WHERE "+national_id+ " = "+id;
                db.execSQL(sql1);
            }*/
            if (type.equals("WALK") || type.equals("DRIVE")) {
                String sql = "UPDATE " + TABLE_DRIVE_IN + " SET " + exitTime + " = '" + time + "' WHERE " + national_id + " = '" + id + "';";
                db.execSQL(sql);

            } else if (type.equals("PROVIDER")) {
                String sql = "UPDATE " + TABLE_SERVICE_PROVIDERS + " SET " + exitTime + " = '" + time + "' WHERE " + national_id + " = '" + id + "';";
                db.execSQL(sql);

            } else if (type.equals("RESIDENT")) {
                String sql = "UPDATE " + TABLE_RESIDENTS + " SET " + exitTime + " = '" + time + "' WHERE " + national_id + " = '" + id + "';";
                db.execSQL(sql);

            }
            db.close();

        }
        /*
        * type-If walk in or drive in
        * 0=DRIVE IN
        * 1=WALK IN
        *
        * */

        public ArrayList<DriveIn> getUnSyncedDriveIns(int type){
            ArrayList<DriveIn> items = new ArrayList<DriveIn>();
            String selectQuery = "SELECT  * FROM " + TABLE_DRIVE_IN+" WHERE "+synced+" = '1';";
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    DriveIn item = new DriveIn();
                    item.setName(cursor.getString(0));
                    item.setNationalId(cursor.getString(1));
                    item.setVisitorType(cursor.getString(2));
                    item.setCarNumber(cursor.getString(3));
                    item.setImage(cursor.getString(4));
                    item.setEntryTime(cursor.getString(5));
                    item.setStatus(cursor.getString(6));
                    item.setExitTime(cursor.getString(7));
                    item.setRecordType(cursor.getString(8));
                    item.setOtherNames(cursor.getString(9));
                    item.setDob(cursor.getString(10));
                    item.setSex(cursor.getString(11));
                    item.setIdType(cursor.getString(12));
                    item.setHouseID(cursor.getString(14));
                    if(type==0){
                        //Select those whose exit time is NULL
                        if(item.getRecordType().equals("DRIVE")){
                            items.add(item);
                        }
                    }else{
                        if(item.getRecordType().equals("WALK")){
                            items.add(item);
                        }

                    }
                } while (cursor.moveToNext());
            }
            db.close();
            return items;
        }
        /*
        *
        * Type can be check out or logs
        * 0=Check out
        * 1=Logs
        * */
        public ArrayList<DriveIn> getDriveIns(int type) {
            ArrayList<DriveIn> items = new ArrayList<DriveIn>();
            String selectQuery = "SELECT  * FROM " + TABLE_DRIVE_IN;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    DriveIn item = new DriveIn();
                    item.setName(cursor.getString(0));
                    item.setNationalId(cursor.getString(1));
                    item.setVisitorType(cursor.getString(2));
                    item.setCarNumber(cursor.getString(3));
                    item.setImage(cursor.getString(4));
                    item.setEntryTime(cursor.getString(5));
                    item.setStatus(cursor.getString(6));
                    item.setExitTime(cursor.getString(7));
                    item.setRecordType(cursor.getString(8));
                    item.setOtherNames(cursor.getString(9));
                    item.setDob(cursor.getString(10));
                    item.setSex(cursor.getString(11));
                    item.setIdType(cursor.getString(12));
                    if(type==0){
                        //Select those whose exit time is NULL
                        if(item.getExitTime().equals("NULL")&&item.getRecordType().equals("DRIVE")){
                            items.add(item);
                        }
                    }else{
                        //Add all of them
                        items.add(item);

                    }
                } while (cursor.moveToNext());
            }
            db.close();
            return items;
        }
        public ArrayList<DriveIn> getWalk(int type) {
            ArrayList<DriveIn> items = new ArrayList<DriveIn>();
            String selectQuery = "SELECT  * FROM " + TABLE_DRIVE_IN;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    DriveIn item = new DriveIn();
                    item.setName(cursor.getString(0));
                    item.setNationalId(cursor.getString(1));
                    item.setVisitorType(cursor.getString(2));
                    item.setCarNumber(cursor.getString(3));
                    item.setImage(cursor.getString(4));
                    item.setEntryTime(cursor.getString(5));
                    item.setStatus(cursor.getString(6));
                    item.setExitTime(cursor.getString(7));
                    item.setRecordType(cursor.getString(8));
                    item.setOtherNames(cursor.getString(9));
                    item.setDob(cursor.getString(10));
                    item.setSex(cursor.getString(11));
                    item.setIdType(cursor.getString(12));
                    if(type==0){
                        //Select those whose exit time is NULL
                        if(item.getExitTime().equals("NULL")&&item.getRecordType().equals("WALK")){
                            items.add(item);
                        }
                    }else{
                        //Add all of them
                        items.add(item);

                    }
                } while (cursor.moveToNext());
            }
            db.close();
            return items;
        }
        public ArrayList<ServiceProviderModel> getProviders(int type) {
            ArrayList<ServiceProviderModel> items = new ArrayList<ServiceProviderModel>();
            String selectQuery = "SELECT  * FROM " + TABLE_SERVICE_PROVIDERS;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    ServiceProviderModel item = new ServiceProviderModel();
                    item.setProviderName(cursor.getString(0));
                    item.setNationalId(cursor.getString(1));
                    item.setCompanyName(cursor.getString(2));
                    item.setProviderImage(cursor.getString(5));
                    item.setEntryTime(cursor.getString(3));
                    item.setExitTime(cursor.getString(4));
                    item.setOtherNames(cursor.getString(5));
                    item.setDob(cursor.getString(6));
                    item.setSex(cursor.getString(7));
                    item.setIdType(cursor.getString(8));
                    if(type==0){
                        if(item.getExitTime().equals("NULL")){
                            items.add(item);
                        }
                    }else{
                        items.add(item);
                    }
                } while (cursor.moveToNext());
            }
            db.close();
            return items;
        }
        public ArrayList<Resident> getResidents(int type) {
            ArrayList<Resident> items = new ArrayList<Resident>();
            String selectQuery = "SELECT  * FROM " + TABLE_RESIDENTS;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Resident item = new Resident();
                    item.setName(cursor.getString(0));
                    item.setNationalId(cursor.getString(1));
                    item.setHouse(cursor.getString(2));
                    item.setEntryTime(cursor.getString(3));
                    item.setExitTime(cursor.getString(4));
                    item.setImage(cursor.getString(5));
                    item.setOtherNames(cursor.getString(6));
                    item.setDob(cursor.getString(7));
                    item.setSex(cursor.getString(8));
                    item.setIdType(cursor.getString(9));
                    if(type==0){
                        if(item.getExitTime().equals("NULL")){
                            items.add(item);
                        }
                    }else{
                        items.add(item);
                    }

                } while (cursor.moveToNext());
            }
            db.close();
            return items;
        }
        public ArrayList<IncidentModel> getIncidents() {
            ArrayList<IncidentModel> items = new ArrayList<IncidentModel>();
            String selectQuery = "SELECT  * FROM " + TABLE_INCIDENTS;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    IncidentModel item = new IncidentModel();
                    item.setName(cursor.getString(0));
                    item.setNationalId(cursor.getString(1));
                    item.setCategory(cursor.getString(2));
                    item.setDescription(cursor.getString(3));
                    item.setDate(cursor.getString(4));
                    item.setOtherNames(cursor.getString(5));
                    item.setDob(cursor.getString(6));
                    item.setSex(cursor.getString(7));
                    item.setIdType(cursor.getString(8));
                   items.add(item);
                } while (cursor.moveToNext());
            }
            db.close();
            return items;
        }
       public void DeleteData(){
            String selectQuery1 = "DELETE FROM "+TABLE_HOUSES;
            String selectQuery2= "DELETE FROM "+TABLE_DRIVE_IN;
            String selectQuery3 = "DELETE FROM "+TABLE_RESIDENTS;
            String selectQuery4 = "DELETE FROM "+TABLE_VISITOR_TYPES;
            String selectQuery5 = "DELETE FROM "+TABLE_INCIDENT_TYPES;
            String selectQuery6 = "DELETE FROM "+TABLE_SERVICE_PROVIDERS;
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(selectQuery1);
            db.execSQL(selectQuery2);
            db.execSQL(selectQuery3);
            db.execSQL(selectQuery4);
            db.execSQL(selectQuery5);
            db.execSQL(selectQuery6);

        }
        public ArrayList<DriveIn> search(String search,String column) {

            ArrayList<DriveIn> alTasklist = new ArrayList<DriveIn>();
            SQLiteDatabase db = this.getWritableDatabase();

            try {
                Cursor cursor = db.query(true, TABLE_DRIVE_IN,
                        new String[] { name,national_id,visitorType,carNumber,image,entryTime,status,exitTime,recordType,otherNames,dob,sex,idType }, column + "='" + search
                                + "'", null, null, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        DriveIn item = new DriveIn();
                        item.setName(cursor.getString(0));
                        item.setNationalId(cursor.getString(1));
                        item.setVisitorType(cursor.getString(2));
                        item.setCarNumber(cursor.getString(3));
                        item.setImage(cursor.getString(4));
                        item.setEntryTime(cursor.getString(5));
                        item.setStatus(cursor.getString(6));
                        item.setExitTime(cursor.getString(7));
                        item.setRecordType(cursor.getString(8));
                        item.setOtherNames(cursor.getString(9));
                        item.setDob(cursor.getString(10));
                        item.setSex(cursor.getString(11));
                        item.setIdType(cursor.getString(12));
                        alTasklist.add(item);
                        cursor.moveToNext();
                    }
                    cursor.close();
                    db.close();
                    return alTasklist;
                }
                return alTasklist;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return alTasklist;
        }
        public void insertVisitorType(String id,String name){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(this.name,name);
            values.put(this.id,id);
            db.insert(TABLE_VISITOR_TYPES, null, values);
            db.close();
        }
        public void insertServiceProviderType(String id,String name,String description){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(this.name,name);
            values.put(this.id,id);
            values.put(this.description,description);
            db.insert(TABLE_SERVICE_PROVIDERS_TYPES, null, values);
            db.close();
        }
        public void insertIncidentTypes(String id,String description){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(this.description,description);
            values.put(this.id,id);
            db.insert(TABLE_INCIDENT_TYPES, null, values);
            db.close();
        }
        public void insertHouse(String houseId,String houseDescription,String hostId){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(this.id,houseId);
            values.put(this.description,houseDescription);
            values.put(this.hostId,hostId);
            db.insert(TABLE_HOUSES, null, values);
            db.close();
        }
        public ArrayList<TypeObject> getTypes(String type){
            ArrayList<TypeObject> hashMap=new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            if(type.equals("visitors")){
                if (!hashMap.isEmpty())
                    hashMap.clear();
                String selectQuery = "SELECT  * FROM " + TABLE_VISITOR_TYPES;
                Cursor cursor = db.rawQuery(selectQuery, null);
                if (cursor.moveToFirst()) {
                    do {
                        TypeObject  typeObject=new TypeObject();
                        typeObject.setId(cursor.getString(0));
                        typeObject.setName(cursor.getString(1));
                        hashMap.add(typeObject);
                    } while (cursor.moveToNext());
                }
                return hashMap;
            }else if(type.equals("incidents")){
                if (!hashMap.isEmpty())
                    hashMap.clear();
                String selectQuery = "SELECT  * FROM " + TABLE_INCIDENT_TYPES;
                Cursor cursor = db.rawQuery(selectQuery, null);
                if (cursor.moveToFirst()) {
                    do {
                        TypeObject  typeObject=new TypeObject();
                        typeObject.setId(cursor.getString(0));
                        typeObject.setName(cursor.getString(1));
                        hashMap.add(typeObject);
                    } while (cursor.moveToNext());
                }
                return hashMap;
            }else if(type.equals("service_providers")){
                if (!hashMap.isEmpty())
                    hashMap.clear();
                String selectQuery = "SELECT  * FROM " + TABLE_SERVICE_PROVIDERS_TYPES;
                Cursor cursor = db.rawQuery(selectQuery, null);
                if (cursor.moveToFirst()) {
                    do {
                        TypeObject  typeObject=new TypeObject();
                        typeObject.setId(cursor.getString(0));
                        typeObject.setName(cursor.getString(1));
                        hashMap.add(typeObject);
                    } while (cursor.moveToNext());
                }
                return hashMap;
            }else if(type.equals("houses")){
                if (!hashMap.isEmpty())
                    hashMap.clear();
                String selectQuery = "SELECT  * FROM " + TABLE_HOUSES;
                Cursor cursor = db.rawQuery(selectQuery, null);
                if (cursor.moveToFirst()) {
                    do {
                        TypeObject  typeObject=new TypeObject();
                        typeObject.setId(cursor.getString(0));
                        typeObject.setName(cursor.getString(1));
                        typeObject.setHostId(cursor.getString(2));
                        hashMap.add(typeObject);
                    } while (cursor.moveToNext());
                }
                return hashMap;
            }
            db.close();
            return null;
        }
}

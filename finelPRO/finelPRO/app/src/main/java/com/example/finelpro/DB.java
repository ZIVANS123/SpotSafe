package com.example.finelpro;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DB extends SQLiteOpenHelper {////

    private static final String DATABASE_NAME = "db1";
    private static final int DATABASE_VERSION = 1;
    ////////////////////////////Event TABLE///////////////////////////////
    private static final String EVENT_ID = "id";
    private static final String TYPE = "type";
    private static final String IMG = "img";
    private static final String DESCRIPTION = "description";
    private static final String ADRESS = "adress";
    private static final String AREA = "area";
    private static final String RISK_LEVEL = "riskLevel";
    private static final String USER = "user";
    private static final String DATE = "date";

    private static final String EVENTS_TABLE = "events";
    private static final String[] EVENTS_COLUMNS = {EVENT_ID, TYPE, IMG, DESCRIPTION, ADRESS, AREA, RISK_LEVEL, USER, DATE};


    ////////////////////////////Approval TABLE///////////////////////////////
    private static final String APPROVALS_TABLE = "approvals";
    private static final String APPROVAL_ID = "approvalId";
    private static final String EVENT_ID_FK = "eventId";
    private static final String USER_ID = "user";
    private static final String APPROVAL_ACTION = "act";
    private static final String[] APPROVALS_COLUMNS = {APPROVAL_ID, EVENT_ID_FK, USER_ID, APPROVAL_ACTION};


    ////////////////////////////Comment TABLE///////////////////////////////
    private static final String COMMENTS_TABLE = "comments";
    private static final String COMMENT_ID = "commentId";
    private static final String COMMENT = "comment";
    private static final String EVENT_ID_COMMENT = "eventIdComment";
    private static final String USER_ID_COMMENT = "userIdComment";
    private static final String[] COMMENTS_COLUMNS = {COMMENT_ID, COMMENT, EVENT_ID_COMMENT, USER_ID_COMMENT};


    private static DB instance;

    private Context context;
    private static SQLiteDatabase db;


    private DB(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create the events table
        String createPostsTblSql = "CREATE TABLE IF NOT EXISTS " + EVENTS_TABLE + " ("
                + EVENT_ID + " TEXT PRIMARY KEY , "
                + TYPE + " TEXT, "
                + IMG + " BLOB, "
                + DESCRIPTION + " TEXT, "
                + ADRESS + " TEXT, "
                + AREA + " TEXT, "
                + RISK_LEVEL + " TEXT, "
                + USER + " TEXT, "
                + DATE + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(createPostsTblSql);

        // Create approvals table
        String createApprovalsTableSql = "CREATE TABLE IF NOT EXISTS " + APPROVALS_TABLE + "("
                + APPROVAL_ID + " TEXT PRIMARY KEY , "
                + EVENT_ID_FK + " TEXT, "
                + USER_ID + " TEXT, "
                + APPROVAL_ACTION + " INTEGER, "
                + "FOREIGN KEY(" + EVENT_ID_FK + ") REFERENCES " + EVENTS_TABLE + "(" + EVENT_ID + "))";
        sqLiteDatabase.execSQL(createApprovalsTableSql);

        // Create the comments table
        String createCommentsTableSql = "CREATE TABLE IF NOT EXISTS " + COMMENTS_TABLE + " ("
                + COMMENT_ID + " TEXT PRIMARY KEY, "
                + COMMENT + " TEXT, "
                + EVENT_ID_COMMENT + " TEXT, "
                + USER_ID_COMMENT + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(createCommentsTableSql);

    }

    public void open() {
        db = getWritableDatabase();
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

    //synchronized ensures that only one thread can access the method at a time.
    public static synchronized DB getInstance(Context context) {
        if (instance == null) {
            instance = new DB(context.getApplicationContext());
        }
        return instance;
    }

    //method is used to insert a new event into the events table
    public boolean addNewEvent(Event event) {
        long result = 0;
        try {
            result = -1;
            ContentValues values = new ContentValues();
            values.put(EVENT_ID, event.getId().toString());

            values.put(TYPE, event.getType().toString());
            Bitmap image = event.getPhoto();
            if (image != null) {
                byte[] img = event.getImgAsByteArray(image);
                if (img != null && img.length > 0) {
                    values.put(IMG, img);
                }
            } else {
                values.putNull(IMG);
            }
            values.put(DESCRIPTION, event.getDescription());
            values.put(ADRESS, event.getAddress());

            values.put(AREA, event.getArea().toString());
            values.put(RISK_LEVEL, event.getRiskLevel().toString());
            values.put(USER, event.getUserName());
            values.put(DATE, event.getDate()); // Add the event's date

            result = db.insert(EVENTS_TABLE, null, values);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result != -1; // Return true if insertion was successful, false otherwise
    }

    //method is used to delete an event from the events table
    public boolean deleteEvent(Event event) {
        String[] arguments = {String.valueOf(event.getId())};
        int result = db.delete(EVENTS_TABLE, EVENT_ID + " = ?", arguments);
        if (result > 0) {
            return true;
        }
        return false;
    }

    //method is used to update an existing event in the events table
    public boolean updateEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TYPE, event.getType());
        values.put(ADRESS, event.getAddress());
        values.put(AREA, event.getArea());
        values.put(RISK_LEVEL, event.getRiskLevel());
        values.put(DESCRIPTION, event.getDescription());
        values.put(DATE, event.getDate());
        Bitmap image = event.getPhoto();
        if (image != null) {
            byte[] img = event.getImgAsByteArray(image);
            if (img != null && img.length > 0) {
                values.put(IMG, img);
            }
        } else {
            values.putNull(IMG);
        }

        String[] whereArgs = {String.valueOf(event.getId())};
        int rowsAffected = db.update(EVENTS_TABLE, values, EVENT_ID + "=?", whereArgs);
        db.close();

        return rowsAffected > 0;
    }

    //method retrieves all events from the events table
    public List<Event> getAllEvents() {
        List<Event> results = new ArrayList<Event>();
        Cursor cursor = null;
        try {
            cursor = db.query(EVENTS_TABLE, EVENTS_COLUMNS, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Event event = new Event();

                    event.setId((cursor.getString(0)));
                    event.setType(cursor.getString(1));
                    if (cursor.getBlob(2) != null) {
                        byte[] byteArray = cursor.getBlob(2);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        event.setPhoto(bitmap);
                    }

                    event.setDescription(cursor.getString(3));
                    event.setAddress(cursor.getString(4));
                    event.setArea(cursor.getString(5));
                    event.setRiskLevel(cursor.getString(6));
                    event.setUserName(cursor.getString(7));
                    event.setDate(cursor.getString(8));
                    results.add(event);
                    cursor.moveToNext();

                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return results;
    }

    //method retrieves events associated with a specific user from the events table.
    public List<Event> getAllmyEvents(String userName) {
        List<Event> results = new ArrayList<Event>();
        Cursor cursor = null;
        try {
            String[] whereArgs = {userName};
            String selection = USER + "=?";
            cursor = db.query(EVENTS_TABLE, EVENTS_COLUMNS, selection, whereArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Event event = new Event();
                    event.setId(cursor.getString(0));
                    event.setType(cursor.getString(1));
                    if (cursor.getBlob(2) != null) {
                        byte[] byteArray = cursor.getBlob(2);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        event.setPhoto(bitmap);
                    }
                    event.setDescription(cursor.getString(3));
                    event.setAddress(cursor.getString(4));
                    event.setArea(cursor.getString(5));
                    event.setRiskLevel(cursor.getString(6));
                    event.setUserName(cursor.getString(7));
                    event.setDate(cursor.getString(8));
                    results.add(event);
                    cursor.moveToNext();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return results;
    }

    //Returns the number of points for the user's reports,
    // uses a helper method that says whether the report meets the conditions for receiving points for the event
    public int getEventsApprovedByMajorityCountForUser(String userName) {
        int count = 0;
        List<Event> userEvents = getAllmyEvents(userName);

        for (Event event : userEvents) {
            if (isEventApprovedByMajority(event.getId())) {
                count++;
            }
        }

        return count * 10;
    }


    //A method that calculates whether the event is entitled to points,
    // according to the criterion that the event received at least 70 percent approvals from all actions
    public boolean isEventApprovedByMajority(String eventId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int totalApprovals = 0;
        int totalUsers = 0;

        try {
            String[] whereArgs = {eventId};
            String selection = EVENT_ID_FK + "=?";
            cursor = db.query(APPROVALS_TABLE, APPROVALS_COLUMNS, selection, whereArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int eventIndex = cursor.getColumnIndex(EVENT_ID_FK);
                int statusIndex = cursor.getColumnIndex(APPROVAL_ACTION);

                // Count the total number of approvals for the event and the total number of users who approved/rejected it
                do {
                    if (cursor.getString(eventIndex).equals(eventId)) {
                        totalUsers++;
                        if (cursor.getInt(statusIndex) == 1) {
                            totalApprovals++;
                        }
                    }
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Calculate the approval percentage
        double approvalPercentage = (double) totalApprovals / totalUsers * 100;
        // Check if the approval percentage is over 70%
        return approvalPercentage > 70.0;
    }

    //Returns all users who created an event in the system
    public List<String> getAllEventCreators() {
        List<String> eventCreators = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(true, EVENTS_TABLE, new String[]{USER}, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String creator = cursor.getString(cursor.getColumnIndex(USER));
                    eventCreators.add(creator);
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return eventCreators;
    }

    //Returns all users who have approved an event in the system
    public List<String> getAllUsersWithApprovals() {
        List<String> usersWithApprovals = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(true, APPROVALS_TABLE, new String[]{USER_ID}, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String user = cursor.getString(cursor.getColumnIndex(USER_ID));


                    if (!usersWithApprovals.contains(user)) {
                        usersWithApprovals.add(user);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return usersWithApprovals;
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //Returns the number of rejections the user made in the system
    public int getRejectedEventCountForUser(String userName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int rejectedCount = 0;

        try {
            String[] whereArgs = {userName, "0"}; // 0 for false (rejected)
            cursor = db.query(APPROVALS_TABLE, APPROVALS_COLUMNS, USER_ID + "=? AND " + APPROVAL_ACTION + "=?", whereArgs, null, null, null);
            if (cursor != null) {
                rejectedCount = cursor.getCount();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return rejectedCount;
    }

    //Returns the number of approvals the user made in the system
    public int getApprovedEventCountForUser(String userName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int approvedCount = 0;

        try {
            String[] whereArgs = {userName, "1"};
            cursor = db.query(APPROVALS_TABLE, APPROVALS_COLUMNS, USER_ID + "=? AND " + APPROVAL_ACTION + "=?", whereArgs, null, null, null);
            if (cursor != null) {
                approvedCount = cursor.getCount();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return approvedCount;
    }


    //Deletes the Approvals that are related to the received event
    public boolean deleteApprovalsByEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = EVENT_ID_FK + " = ?";
        String[] whereArgs = {event.getId()};
        int rowsAffected = db.delete(APPROVALS_TABLE, whereClause, whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    // Method to delete comments associated with the given event ID
    public boolean deleteCommentsByEventId(String eventId) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = EVENT_ID_COMMENT + " = ?";
        String[] whereArgs = {eventId};
        int rowsAffected = db.delete(COMMENTS_TABLE, whereClause, whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    //Returns a list of events that the resulting user has approved
    public List<Event> getApprovedEventsForUser(String userName) {
        List<Event> approvedEvents = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] whereArgs = {userName, "1"}; // 1 for true (approved)
            String selection = USER + "=? AND " + APPROVAL_ACTION + "=?";
            cursor = db.query(APPROVALS_TABLE, APPROVALS_COLUMNS, selection, whereArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String eventId = cursor.getString(cursor.getColumnIndex(EVENT_ID_FK));
                    // Get the event object by eventId
                    Event event = getEventById(eventId);
                    if (event != null) {
                        approvedEvents.add(event);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return approvedEvents;
    }

    //Returns the number of approvals for the received event
    public int getApprovalCountForEvent(String eventId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        int approvalCount = 0;

        try {
            String[] whereArgs = {eventId, "1"}; // 1 for true (approved)
            String selection = EVENT_ID_FK + "=? AND " + APPROVAL_ACTION + "=?";
            cursor = db.query(APPROVALS_TABLE, APPROVALS_COLUMNS, selection, whereArgs, null, null, null);

            if (cursor != null) {
                approvalCount = cursor.getCount();
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return approvalCount;
    }


    //A method that receives an Eventid and returns the full object
    public Event getEventById(String eventId) {
        Event event = null;
        Cursor cursor = null;

        try {
            String[] whereArgs = {String.valueOf(eventId)};
            String selection = EVENT_ID + "=?";
            cursor = db.query(EVENTS_TABLE, EVENTS_COLUMNS, selection, whereArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                event = new Event();
                event.setId(cursor.getString(0));
                event.setType(cursor.getString(1));
                if (cursor.getBlob(2) != null) {
                    byte[] byteArray = cursor.getBlob(2);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    event.setPhoto(bitmap);
                }
                event.setDescription(cursor.getString(3));
                event.setAddress(cursor.getString(4));
                event.setArea(cursor.getString(5));
                event.setRiskLevel(cursor.getString(6));
                event.setUserName(cursor.getString(7));
                event.setDate(cursor.getString(8));
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return event;
    }


    //Deleting the tables from the local database
    public void clearData() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(EVENTS_TABLE, null, null);
        db.delete(COMMENTS_TABLE, null, null);
        db.delete(APPROVALS_TABLE, null, null);

    }

    //Returns an image from the received event ID
    public Bitmap getEventImageById(String eventId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        Bitmap image = null;

        try {
            String[] whereArgs = {eventId};
            String selection = EVENT_ID + "=?";
            cursor = db.query(EVENTS_TABLE, new String[]{IMG}, selection, whereArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                byte[] imageData = cursor.getBlob(0);
                if (imageData != null) {
                    image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return image;
    }

    //Adding a new comment to the comments table
    public boolean addNewComment(Comment comment) {
        long result = -1;
        try {
            result = -1;
            ContentValues values = new ContentValues();
            values.put(EVENT_ID_COMMENT, comment.getEventId());
            values.put(USER_ID_COMMENT, comment.getEmail());
            values.put(COMMENT, comment.getComment());
            values.put(COMMENT_ID, comment.getCommentId());


            result = db.insert(COMMENTS_TABLE, null, values);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result != -1; // Return true if insertion was successful, false otherwise
    }

    //Deleting a comment from the comments table
    public boolean deleteComment(Comment comment) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COMMENT_ID + " = ?";
        String[] whereArgs = {comment.getCommentId()};
        int rowsAffected = db.delete(COMMENTS_TABLE, whereClause, whereArgs);
        db.close();
        return rowsAffected > 0;
    }

    //Returns a list of comments for the received event
    public List<Comment> getCommentsByEventId(String eventId) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] whereArgs = {eventId};
            String selection = EVENT_ID_COMMENT + "=?";
            cursor = db.query(COMMENTS_TABLE, COMMENTS_COLUMNS, selection, whereArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Comment comment = new Comment();
                    comment.setCommentId(cursor.getString(0));
                    comment.setComment(cursor.getString(1));
                    comment.setEventId(cursor.getString(2));
                    comment.setEmail(cursor.getString(3));
                    comments.add(comment);
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return comments;
    }

    //Update an existing comment in the received comment
    public boolean updateComment(Comment comment) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMMENT, comment.getComment());
        values.put(EVENT_ID_COMMENT, comment.getEventId());
        values.put(USER_ID_COMMENT, comment.getEmail());

        String whereClause = COMMENT_ID + " = ?";
        String[] whereArgs = {comment.getCommentId()};
        int rowsAffected = db.update(COMMENTS_TABLE, values, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }


    public List<Comment> getCommentsByUser(String userName) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] whereArgs = {userName};
            String selection = USER_ID_COMMENT + "=?";
            cursor = db.query(COMMENTS_TABLE, COMMENTS_COLUMNS, selection, whereArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Comment comment = new Comment();
                    comment.setCommentId(cursor.getString(0));
                    comment.setComment(cursor.getString(1));
                    comment.setEventId(cursor.getString(2));
                    comment.setEmail(cursor.getString(3));
                    comments.add(comment);
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return comments;
    }

    //Added a new Approval to the table
    public boolean addNewApproval(Approval approval) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_ID_FK, approval.getEventId());
        values.put(USER_ID, approval.getUserName());
        values.put(APPROVAL_ACTION, approval.getStatus());
        values.put(APPROVAL_ID, approval.getAprrovalId());

        String selection = EVENT_ID_FK + " = ? AND " + USER_ID + " = ?";
        String[] selectionArgs = {approval.getEventId(), approval.getUserName()};
        Cursor cursor = db.query(APPROVALS_TABLE, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            int approvalIdIndex = cursor.getColumnIndex(APPROVAL_ID);
            if (approvalIdIndex != -1) {
                int approvalId = cursor.getInt(approvalIdIndex);
                String whereClause = APPROVAL_ID + " = ?";
                String[] whereArgs = {String.valueOf(approvalId)};
                int rowsAffected = db.update(APPROVALS_TABLE, values, whereClause, whereArgs);
                cursor.close();

                return rowsAffected > 0;
            }
        }

        // Insert new approval entry
        long result = db.insert(APPROVALS_TABLE, null, values);
        cursor.close();

        return result != -1;
    }


    //Updating the status of a approval that exists in the system
    public boolean updateApprovalStatus(Approval approval) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(APPROVAL_ACTION, approval.getStatus());

        String whereClause = EVENT_ID_FK + " = ? AND " + USER_ID + " = ?";
        String[] whereArgs = {approval.getEventId(), approval.getUserName()};
        int rowsAffected = db.update(APPROVALS_TABLE, values, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }


}




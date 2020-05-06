package org.itoapp.strict;

public class Constants {
    public static final int BLUETOOTH_COMPANY_ID = 65535; // TODO get a real company ID!
    public static final int UUID_LENGTH = 16;
    public static final int HASH_LENGTH = 16;
    public static final int BROADCAST_LENGTH = HASH_LENGTH ;
    public static final int UUID_VALID_INTERVAL = 1000 * 60 * 30; //ms * sec * 30 min
    public static final int CHECK_SERVER_INTERVAL = 1000 * 10; //ms * 10 sec
    public static final int DISTANCE_SMOOTHING_MA_LENGTH = 7;
    public static final int CACHE_FLUSH_TIME = 1000 * 30; // 30 seconds timeout
    public static final long MIN_CONTACT_DURATION = 1000 * 60 * 1; //discard all contacts less than 1 minutes
    public static final float MIN_SCANNING_DISTANCE = 20;
    public static final long MIN_EXPOSURE_DURATION = 1000 * 60 * 2; // 2 minutes
}

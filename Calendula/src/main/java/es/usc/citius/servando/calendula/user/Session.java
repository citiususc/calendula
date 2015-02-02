package es.usc.citius.servando.calendula.user;

import android.content.Context;

/**
 * Created by joseangel.pineiro on 6/16/14.
 */
public class Session {

    private static String SESSION_FILENAME = "session.json";

    private static Session instance = new Session();

    private static boolean isOpen = false;
    
    private Session() {
    }

    public static Session instance() {
        return instance;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close(Context context) {
        isOpen = false;
    }
//
//    public boolean resume(Context context) throws Exception {
//
//        ApiResponse response = null;
//
//        try {
//            open(context);
//
//            response = new ApiRequestBuilder()
//                    .to("auth")
//                    .authorize(user.getToken())
//                    .expect(ApiResponse.class)
//                    .post();
//
//            Log.d("Session", "Resume session [" + response.success + ", " + response.status + "]");
//            return response.success;
//
//        } catch (Exception e) {
//            Log.e("Session", "Cannot resume user session [" + response + "]", e);
//            try {
//                // close(context);
//            } catch (Exception unhandled) {/* do nothintg */}
//        }
//
//        return false;
//    }


//    public void create(Context context, User user) throws Exception {
//        // open session file where user data is stored
//        final FileOutputStream out = context.openFileOutput(SESSION_FILENAME, Context.MODE_PRIVATE);
//        String json = new Gson().toJson(user);
//        out.write(json.getBytes());
//        out.close();
//
//        open(context);
//    }


//    public void save(Context context) throws Exception {
//        // open session file where user data is stored
//        final FileOutputStream out = context.openFileOutput(SESSION_FILENAME, Context.MODE_PRIVATE);
//        String json = new Gson().toJson(user);
//        out.write(json.getBytes());
//        out.close();
//    }

//    public boolean open(Context context) throws Exception {
//        // open session file where user data is stored
//        FileInputStream is = null;
//        try {
//            is = context.openFileInput(SESSION_FILENAME);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//            user = new Gson().fromJson(reader, User.class);
//            onCreateSession(context);
//            Log.d(Session.class.getName(), "Opening session for user [" + user.getEmail() + ", " + user.getToken() + "]");
//            return true;
//        } catch (FileNotFoundException e) {
//            Log.e(Session.class.getName(), "Error reading session data", e);
//            //throw new Exception("No user data file was found");
//
//        } finally {
//            try {
//                is.close();
//            } catch (Exception unhandled) {
//                //do nothing
//            }
//        }
//        return false;
//    }

    public boolean open(Context context) throws Exception {
        isOpen = true;
        return true;
    }
//
//    public Bitmap getUserProfileImage(Context context) {
//        String profileImagePath = user.getProfileImagePath();
//        if (profileImagePath != null) {
//            try {
//                InputStream is = context.openFileInput(profileImagePath);
//                Bitmap selectedImage = BitmapFactory.decodeStream(is);
//                return selectedImage;
//            } catch (Exception e) {
//                Log.e("Session", "Error loading profile image", e);
//            }
//        }
//        return null;
//    }
//
//    private void onCreateSession(Context context) throws Exception {
//
//    }
//
//    private String generateUserDirName(String username) {
//        return username.replace("@", "").replace(".", "-");
//    }

}

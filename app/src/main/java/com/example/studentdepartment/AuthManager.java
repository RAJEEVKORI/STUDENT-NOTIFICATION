package com.example.studentdepartment;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Handles login for both the Student and Department roles.
 *
 * There's no backend yet, so this checks against two simple demo accounts
 * and remembers the logged-in session on the device using SharedPreferences.
 * Passwords are never stored or compared as plain text -- they're hashed
 * with SHA-256 first.
 *
 * WHY IT'S BUILT LIKE THIS:
 * The method names below (signIn / signOut / isLoggedIn) are written to
 * match what a Firebase Authentication version would look like. That means
 * when Firebase gets connected later, only the code INSIDE signIn() needs
 * to change to call FirebaseAuth instead -- MainActivity, StudentActivity,
 * and DepartmentActivity can stay exactly as they are.
 */
public class AuthManager {

    private static final String PREFS_NAME = "admin_detail";
    private static final String KEY_LOGGED_IN = "Status";
    private static final String KEY_ROLE = "User";

    // --- Demo accounts (no backend yet) ---
    // TODO (Firebase): delete these once real accounts are set up in
    // Firebase Authentication, and replace the inside of signIn() with
    // FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).
    private static final String DEPARTMENT_USERNAME = "dept123";
    private static final String STUDENT_USERNAME = "std123";
    private static final String DEMO_PASSWORD_HASH = sha256("123456");

    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        // getApplicationContext() avoids holding a reference to an Activity,
        // which could otherwise leak memory.
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks the entered username/password for the given role.
     * If correct, saves the session so the user stays logged in.
     *
     * @param role     "Student" or "Department"
     * @param username entered username
     * @param password entered password (plain text -- hashed inside this method)
     * @return true if the login was successful
     */
    public boolean signIn(String role, String username, String password) {
        String expectedUsername = "Department".equals(role) ? DEPARTMENT_USERNAME : STUDENT_USERNAME;
        String enteredPasswordHash = sha256(password);

        boolean success = expectedUsername.equals(username) && DEMO_PASSWORD_HASH.equals(enteredPasswordHash);

        if (success) {
            prefs.edit()
                    .putBoolean(KEY_LOGGED_IN, true)
                    .putString(KEY_ROLE, role)
                    .apply();
        }
        return success;
    }

    /** True if someone is currently logged in on this device. */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    /** Returns "Student" or "Department" for whoever is logged in. */
    public String getLoggedInRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    /** Clears the saved session (for a future "Log out" button). */
    public void signOut() {
        prefs.edit().clear().apply();
    }

    /**
     * Turns a password into a one-way SHA-256 hash so the real password is
     * never stored or compared directly. SHA-256 ships with Java, so no
     * extra libraries are needed -- good enough for a student project.
     *
     * (A production app would also add a random per-user "salt" before
     * hashing, and ideally do this check on a server instead of on-device.
     * Worth mentioning in a viva as the "next step", but not required here.)
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to exist on every Android device, so
            // this branch should never actually run.
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

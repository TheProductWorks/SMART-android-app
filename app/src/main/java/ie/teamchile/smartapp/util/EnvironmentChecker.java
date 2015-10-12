package ie.teamchile.smartapp.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by user on 9/12/15.
 */
public class EnvironmentChecker {
    private static String cert = "";

    public static boolean isCertInvalid(Context context) {
        String fingerprintMD5 = "";
        Signature[] sigs = new Signature[0];
        try {
            sigs = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES).signatures;

            for (Signature sig : sigs) {
                byte[] hexBytes = sig.toByteArray();
                MessageDigest digest = MessageDigest.getInstance("MD5");

                byte[] md5digest = new byte[0];

                if (digest != null) {
                    md5digest = digest.digest(hexBytes);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < md5digest.length; ++i) {
                        sb.append((Integer.toHexString((md5digest[i] & 0xFF) | 0x100)).substring(1, 3));
                    }
                    fingerprintMD5 = sb.toString();
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return !fingerprintMD5.equals(cert);
    }

    public static boolean isDeviceRooted() {
        return (checkRootMethod1() || checkRootMethod2() || checkRootMethod3());
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
                "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    public static boolean isDebuggable(Context context){
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private static String getSystemProperty(String name) throws Exception {
        Class systemPropertyClass = Class.forName("android.os.SystemProperties");

        return (String) systemPropertyClass.getMethod("get", new Class[]{String.class})
                .invoke(systemPropertyClass, new Object[]{name});
    }

    public static boolean isEmulator() {
        boolean emulator = Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")
                        || Build.MANUFACTURER.contains("Genymotion");

        try {
            boolean goldfish = getSystemProperty("ro.hardware").contains("goldfish");
            boolean emu = getSystemProperty("ro.kernel.qemu").length() > 0;
            boolean sdk = getSystemProperty("ro.product.model").equals("sdk");
            if (emu || goldfish || sdk || emulator)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

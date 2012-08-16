
package pscp.restlet.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author iws
 */
public class MD5Sum {

    private final MessageDigest digest;
    private final byte[] buf;

    public MD5Sum() {
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        buf = new byte[8192];
    }

    public String md5sum(InputStream in) throws IOException {
        int r = 0;
        digest.reset();
        while ((r = in.read(buf)) > 0) {
            digest.update(buf, 0, r);
        }
        return new BigInteger(1,digest.digest()).toString(16);
    }

    public static void main(String[] args) throws Exception {
        MD5Sum summer = new MD5Sum();
        for (int i = 0; i < args.length; i++) {
            FileInputStream fin = new FileInputStream(args[i]);
            String sum = summer.md5sum(fin);
            fin.close();
            System.out.println(args[i] + " " + sum);
        }
    }
}

package pscp.restlet.util;

import com.noelios.restlet.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 *
 * @author iws
 */
public final class Passwords {

    private static Cipher encrypt;
    private static Cipher decrypt;

    private Passwords() {}

    private static void init(String passwd) throws Exception {
        if (encrypt == null) {
            if (passwd == null) {
                passwd = "xyz!1234";
            }
            // The magic number below is a randomly generated key - it is our "password"
            KeySpec skeySpec = new PBEKeySpec(
                    passwd.toCharArray());
            Key key = SecretKeyFactory.getInstance(TYPE).generateSecret(skeySpec);
            encrypt = javax.crypto.Cipher.getInstance(TYPE);
            encrypt.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec("rti12345".getBytes(), 1));
            decrypt = javax.crypto.Cipher.getInstance(TYPE);
            decrypt.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec("rti12345".getBytes(), 1));
        }
    }

    private static String base32(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        System.arraycopy(b, 0, b2, 1, b.length);
        BigInteger bi = new BigInteger(b);
        return bi.toString(32);
    }

    private static byte[] debase32(String s) {
        BigInteger bi = new BigInteger(s, 32);
        byte[] b = bi.toByteArray();
        byte[] b2 = new byte[b.length - 1];
        System.arraycopy(b, 1, b2, 0, b2.length);
        return b;
    }

    private static final String TYPE = "PBEWithMD5AndDES";

    public static synchronized String encode(String raw) throws Exception {
        init(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CipherOutputStream out = new CipherOutputStream(baos, encrypt);
        out.write(raw.getBytes("ASCII"));
        out.flush();
        out.close();
        return Base64.encode(baos.toByteArray(), false);
    }

    public static synchronized String decode(String encoded) throws Exception {
        init(null);
        CipherInputStream in = new CipherInputStream(new ByteArrayInputStream(Base64.decode(encoded)), decrypt);
        byte[] b = new byte[encoded.length()];
        StringBuilder sb = new StringBuilder();
        int r = 0;
        while ((r = in.read(b)) > 0) {
            sb.append(new String(b, 0, r, "ASCII"));
        }
        return sb.toString();
    }

    static String random(int len) {
        char[] ch = new char[len];
        for (int i = 0; i < ch.length; i++) {
            ch[i] = (char) (33 + (int) (Math.random() * 95));
        }
        return new String(ch);
    }

    static void test() throws Exception {
        int maxSize = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 1000; j++) {
                String p = random(i + 1);
                String enc = encode(p);
                maxSize = Math.max(maxSize, enc.length());
                if (!p.equals(decode(enc))) {
                    throw new RuntimeException("fail " + p + " : " + p.getBytes()[0]);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("random id : " + UUID.randomUUID());
        String encoded = encode(args[0]);
        String decoded = decode(encoded);
        System.out.println("input          : " + args[0]);
        System.out.println("decoded verify : " + decoded);
        System.out.println("encoded        : " + encoded);
    }
}
